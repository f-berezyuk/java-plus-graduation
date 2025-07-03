package ru.yandex.practicum.event.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.interaction.common.ConflictException;
import ru.practicum.interaction.common.NotFoundException;
import ru.practicum.interaction.common.PageableBuilder;
import ru.practicum.interaction.common.ValidationException;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.LocationDto;
import ru.practicum.interaction.dto.event.NewEventDto;
import ru.practicum.interaction.dto.event.ParticipationRequestDto;
import ru.practicum.interaction.dto.event.StateActionDto;
import ru.practicum.interaction.dto.event.UpdateEventAdminRequest;
import ru.practicum.interaction.dto.event.UpdateEventUserRequest;
import ru.practicum.interaction.dto.request.RequestDto;
import ru.practicum.interaction.dto.request.RequestStatusDto;
import ru.practicum.interaction.dto.request.req_rsp.RequestsSaveAllReq;
import ru.practicum.interaction.feign.client.RequestServiceClient;
import ru.practicum.stats.client.AnalyzerClient;
import ru.practicum.stats.client.CollectorClient;

import ru.yandex.practicum.categories.service.CategoriesService;
import ru.yandex.practicum.event.mapper.EventMapper;
import ru.yandex.practicum.event.mapper.ReqMapper;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.EventState;
import ru.yandex.practicum.event.model.Location;
import ru.yandex.practicum.event.repository.EventRepository;
import ru.yandex.practicum.event.repository.LocationRepository;

import static ru.practicum.ewm.stats.proto.ActionTypeProto.ACTION_VIEW;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final RequestServiceClient requestServiceClient;
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final EventMapper mapper;
    private final ReqMapper requestMapper;
    private final CategoriesService categoriesService;
    private final AnalyzerClient analyzerClient;
    private final CollectorClient collectorClient;
    @SuppressWarnings("unused")
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAllByUserId(Long userId, int from, int size) {
        var pageable = PageableBuilder.getPageable(from, size, "id");
        return eventRepository.findAllByUserId(userId, pageable).getContent().stream().map(mapper::toShortDto).toList();
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        Event event = mapper.toEntity(newEventDto);

        final Location requestLocation = event.getLocation();
        Location mayBeExistingLocation = null;
        if (requestLocation.getId() == null) {
            mayBeExistingLocation = locationRepository
                    .findByLatAndLon(requestLocation.getLat(), requestLocation.getLon())
                    .orElseGet(() -> locationRepository.save(requestLocation));
        }

        event.setLocation(mayBeExistingLocation);
        event.setUserId(userId);
        event = eventRepository.save(event);

        return mapper.toFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndUserId(eventId, userId).orElseThrow(() -> new NotFoundException(
                "Event with id " + eventId + " and user id " + userId + " was not found"));
        return mapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDateTime.now().plusHours(2L))) {
            throw new IllegalArgumentException("Not valid time. Should not be less than now + 2 hours.");
        }
        Event event = eventRepository.findByIdAndUserId(eventId, userId).orElseThrow(() -> new NotFoundException(
                "Event with id " + eventId + " was not found"));

        var updateBuilder = event.toBuilder();

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (request.getStateAction() != null) {
            Map<StateActionDto, EventState> map = Map.of(
                    StateActionDto.SEND_TO_REVIEW, EventState.PENDING,
                    StateActionDto.CANCEL_REVIEW, EventState.CANCELED
            );

            updateBuilder.state(map.get(request.getStateAction()));
        }

        if (request.getLocation() != null) {
            final Location newLocation = mapper.toEntity(request.getLocation());
            Location updatedLocation = null;
            if (newLocation.getId() == null) {
                updatedLocation =
                        locationRepository
                                .findByLatAndLon(newLocation.getLat(), newLocation.getLon())
                                .orElseGet(() -> locationRepository.save(newLocation));
            }

            updateBuilder.location(updatedLocation);
        }

        Optional.ofNullable(request.getAnnotation()).ifPresent(updateBuilder::annotation);
        Optional.ofNullable(request.getCategory()).ifPresent(catId ->
                updateBuilder.category(categoriesService.getOrThrow(catId)));
        Optional.ofNullable(request.getDescription()).ifPresent(updateBuilder::description);
        Optional.ofNullable(request.getEventDate()).ifPresent(updateBuilder::eventDate);
        Optional.ofNullable(request.getPaid()).ifPresent(updateBuilder::paid);
        Optional.ofNullable(request.getParticipantLimit()).ifPresent(updateBuilder::participantLimit);
        Optional.ofNullable(request.getRequestModeration()).ifPresent(updateBuilder::requestModeration);
        Optional.ofNullable(request.getTitle()).ifPresent(updateBuilder::title);

        event = updateBuilder.build();

        return mapper.toFullDto(eventRepository.saveAndFlush(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getRequests(Long userId, Long eventId) {
        return requestServiceClient.getRequests(userId, eventId);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequest(Long userId, Long eventId,
                                                        EventRequestStatusUpdateRequest updateRequest) {
        Set<Long> requestIds = new HashSet<>(updateRequest.getRequestIds());
        List<RequestDto> requests = requestServiceClient.getRequests(userId, eventId).stream().filter(requestDto ->
                requestIds.contains(requestDto.getId())).toList();
        long confirmedRequestCount = requestServiceClient.getRequests(eventId)
                .stream().filter(RequestDto::isConfirmed).count();

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        Event event = getOrThrow(eventId);
        int size = requestIds.size();
        int confirmedSize = updateRequest.getStatus() == RequestStatusDto.CONFIRMED ? size : 0;
        if (event.getParticipantLimit() - confirmedRequestCount < confirmedSize) {
            throw new ConflictException("Event limit exceed. Only " +
                                        (event.getParticipantLimit() - confirmedRequestCount) + " places left.");
        }

        for (RequestDto request : requests) {
            if (updateRequest.getStatus() == RequestStatusDto.CONFIRMED) {
                request.setStatus(RequestStatusDto.CONFIRMED);
                confirmedRequests.add(requestMapper.toParticipationRequestDto(request));
            } else if (updateRequest.getStatus() == RequestStatusDto.REJECTED) {
                if (request.getStatus() == RequestStatusDto.CONFIRMED) {
                    throw new ConflictException("Forbidden to reject confirmed request.");
                }
                request.setStatus(RequestStatusDto.REJECTED);
                rejectedRequests.add(requestMapper.toParticipationRequestDto(request));
            }
        }

        RequestsSaveAllReq build = RequestsSaveAllReq.builder().requests(requests).build();
        requestServiceClient.saveAll(build);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    @Override
    @Transactional
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort, int from, int size,
                                               HttpServletRequest request) {
        assertDataValid(rangeStart, rangeEnd);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> cq = cb.createQuery(Event.class);
        Root<Event> event = cq.from(Event.class);

        Predicate predicate = cb.conjunction();

        // Фильтр по состоянию
        predicate = cb.and(predicate, cb.equal(event.get("state"), "PUBLISHED"));

        // Фильтр для текста поиска
        if (text != null && !text.isEmpty()) {
            String likePattern = "%" + text.toLowerCase() + "%";
            Predicate textPredicate = cb.or(
                    cb.like(cb.lower(event.get("annotation")), likePattern),
                    cb.like(cb.lower(event.get("description")), likePattern)
            );
            predicate = cb.and(predicate, textPredicate);
        }

        // Фильтр по категориям
        if (categories != null && !categories.isEmpty()) {
            predicate = cb.and(predicate, event.get("category").get("id").in(categories));
        }

        // Фильтр по оплаченным статусам
        if (paid != null) {
            predicate = cb.and(predicate, cb.equal(event.get("paid"), paid));
        }

        // Фильтр по дате
        if (rangeStart != null && rangeEnd != null) {
            predicate = cb.and(predicate, cb.between(event.get("eventDate"), rangeStart, rangeEnd));
        } else if (rangeStart != null) {
            predicate = cb.and(predicate, cb.greaterThanOrEqualTo(event.get("eventDate"), rangeStart));
        } else if (rangeEnd != null) {
            predicate = cb.and(predicate, cb.lessThanOrEqualTo(event.get("eventDate"), rangeEnd));
        }

        // Фильтр по доступности
        if (onlyAvailable != null && onlyAvailable) {
            Predicate availabilityPredicate = cb.or(
                    cb.equal(event.get("participantLimit"), 0),
                    cb.lessThan(event.get("confirmedRequests"), event.get("participantLimit"))
            );
            predicate = cb.and(predicate, availabilityPredicate);
        }

        // Применение предиката
        cq.where(predicate);

        // Определение сортировки
        if ("EVENT_DATE".equals(sort)) {
            cq.orderBy(cb.asc(event.get("eventDate")));
        } else if ("VIEWS".equals(sort)) {
            cq.orderBy(cb.desc(event.get("rating")));
        }

        // Создание запроса
        TypedQuery<Event> query = entityManager.createQuery(cq);

        // Установка параметров для пагинации
        query.setFirstResult(from * size);
        query.setMaxResults(size);

        // Выполнение запроса
        List<Event> resultList = query.getResultList();
        var eventsRatings =
                analyzerClient.getInteractionsCount(resultList.stream().map(Event::getId).toList())
                        .collect(Collectors.toMap(RecommendedEventProto::getEventId,
                                RecommendedEventProto::getScore));
        resultList.forEach(e -> e.setRating(eventsRatings.get(e.getId())));
        eventRepository.saveAll(resultList);
        return resultList.stream().map(mapper::toShortDto).toList();
    }

    private void assertDataValid(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart == null || rangeEnd == null) {
            return;
        }
        if (rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("start after end.");
        }
    }

    @Override
    @Transactional
    public EventFullDto getPublicEvent(Long id, Long userId, HttpServletRequest request) {
        Event event =
                eventRepository.findByIdAndState(id, EventState.PUBLISHED).orElseThrow(() -> new NotFoundException(
                        "Event with id " + id + " not found or not published"));
        collectorClient.collectUserAction(id, userId, ACTION_VIEW, Instant.now());
        List<RecommendedEventProto> proto = analyzerClient.getInteractionsCount(List.of(id)).toList();
        Double rating = proto.isEmpty() ? 0.0 : proto.getFirst().getScore();
        event.setRating(rating);
        eventRepository.saveAndFlush(event);
        return mapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto getPublicEvent(Long id) {
        Event event =
                eventRepository.findByIdAndState(id, EventState.PUBLISHED).orElseThrow(() -> new NotFoundException(
                        "Event with id " + id + " not found or not published"));
        eventRepository.saveAndFlush(event);
        return mapper.toFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAllEvents(List<Long> users, List<String> states, List<Long> categories,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        var page = from / size;
        assertDataValid(rangeStart, rangeEnd);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> cq = cb.createQuery(Event.class);
        Root<Event> event = cq.from(Event.class);

        Predicate predicate = cb.conjunction();

        if (users != null && !users.isEmpty()) {
            predicate = cb.and(predicate, event.get("userId").in(users));
        }

        if (states != null && !states.isEmpty()) {
            predicate = cb.and(predicate, event.get("state").in(states));
        }

        if (categories != null && !categories.isEmpty()) {
            predicate = cb.and(predicate, event.get("category").get("id").in(categories));
        }

        if (rangeStart != null && rangeEnd != null) {
            predicate = cb.and(predicate, cb.between(event.get("eventDate"), rangeStart, rangeEnd));
        }

        cq.where(predicate);
        TypedQuery<Event> query = entityManager.createQuery(cq);

        // setting pagination parameters
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        return query.getResultList().stream().map(mapper::toFullDto).toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " was not found"));

        validateEventStateForAdminUpdate(event, request.getStateAction());

        Location location = null;
        if (request.getLocation() != null) {
            location = processLocation(request.getLocation());
        }

        mapper.updateFromAdminRequest(request, event);

        // Устанавливаем обработанную локацию (управляемую или новую)
        if (location != null) {
            event.setLocation(location);
        }

        event.setState(request.getStateAction() == StateActionDto.PUBLISH_EVENT
                ? EventState.PUBLISHED
                : EventState.CANCELED);

        Event updatedEvent = eventRepository.save(event);
        return mapper.toFullDto(updatedEvent);
    }

    private Location processLocation(LocationDto locationDto) {
        Location location = mapper.toEntity(locationDto);

        if (location.getId() == null) {
            return locationRepository.save(location); // Сохраняем новую локацию
        }

        // Для существующей локации: загружаем объект из БД и обновляем поля
        Location existingLocation = locationRepository.findById(location.getId())
                .orElseThrow(() -> new NotFoundException("Location not found with id: " + location.getId()));

        // Обновляем данные существующей локации
        existingLocation.setLat(location.getLat());
        existingLocation.setLon(location.getLon());
        // Другие поля при необходимости

        return existingLocation; // Возвращаем управляемый объект
    }

    @Override
    public Event getOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " was not found"));
    }

    @Override
    public void updateConfirmedRequests(Long eventId, int confirmedRequests) {
        Event event = getOrThrow(eventId);
        event.setConfirmedRequests(confirmedRequests);
        event = eventRepository.saveAndFlush(event);
        mapper.toFullDto(event);
    }

    @Override
    public List<EventFullDto> getRecommendations(Long userId) {
        log.info("Вывоз метода клиента: analyzerClient.getRecommendationsForUser with params userId = {}, maxResult",
                userId);
        List<Long> recommendationsForUser = analyzerClient.getRecommendationsForUser(userId, 10)
                .map(RecommendedEventProto::getEventId).collect(Collectors.toList());
        log.info("вывоз метода клиента analyzerClient.getRecommendationsForUser вернул данные: {}",
                StringUtils.join(recommendationsForUser, ','));

        List<Event> events = eventRepository.findAllById(recommendationsForUser);

        return events.stream().map(mapper::toFullDto).toList();
    }

    @Override
    public void likeEvent(Long eventId, Long userId) {
        if (!requestServiceClient.isUserTakePart(userId, eventId)) {
            throw new ValidationException("Пользователь " + userId + " не принимал участи в событии " + eventId);
        }
        collectorClient.collectUserAction(eventId, userId, ActionTypeProto.ACTION_LIKE, Instant.now());
    }

    private void validateEventStateForAdminUpdate(Event event, StateActionDto stateActionDto) {
        if (stateActionDto == StateActionDto.PUBLISH_EVENT && event.getState() != EventState.PENDING) {
            throw new ConflictException("Cannot publish the event because it's not in the right state");
        }
        if (stateActionDto == StateActionDto.REJECT_EVENT && event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Cannot cancel the event because it has been published");
        }
    }
}
