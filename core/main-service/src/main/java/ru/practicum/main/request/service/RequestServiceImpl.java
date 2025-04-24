package ru.practicum.main.request.service;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.common.ConflictException;
import ru.practicum.interaction.common.NotFoundException;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventStateDto;
import ru.practicum.interaction.dto.request.RequestDto;
import ru.practicum.interaction.feign.client.AdminEventServiceClient;
import ru.practicum.interaction.feign.client.EventServiceClient;
import ru.practicum.main.request.mapper.RequestMapper;
import ru.practicum.main.request.model.Request;
import ru.practicum.main.request.model.RequestStatus;
import ru.practicum.main.request.repository.RequestRepository;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventServiceClient eventServiceClient;
    private final AdminEventServiceClient adminEventServiceClient;
    private final RequestMapper requestMapper;

    @Override
    public List<RequestDto> getRequests(long userId) {
        User user = findUserById(userId);
        return requestMapper.toDtos(requestRepository.findByRequesterId(user.getId()));
    }

    @Override
    public RequestDto createRequest(long userId, long eventId) {
        EventFullDto event = findEventById(eventId);
        User user = findUserById(userId);

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Request already exist");
        }

        if (event.getInitiator().getId().equals(user.getId())) {
            throw new ConflictException("Request can't be created by initiator");
        }

        if (event.getState() != EventStateDto.PUBLISHED) {
            throw new ConflictException("Event not yet published");
        }

        int requestsSize = requestRepository.findAllByEventId(eventId).size();
        if (event.getParticipantLimit() > 0 && !event.isRequestModeration() && event.getParticipantLimit() <= requestsSize) {
            throw new ConflictException("Participant limit exceeded");
        }

        Request eventRequest = new Request(null, LocalDateTime.now(), eventId, user, RequestStatus.PENDING);
        if (!event.isRequestModeration()) {
            eventRequest.setStatus(RequestStatus.CONFIRMED);
        }

        if (event.getParticipantLimit() == 0) {
            eventRequest.setStatus(RequestStatus.CONFIRMED);
        }

        if (eventRequest.getStatus() == RequestStatus.CONFIRMED) {
            int confirmedRequests = event.getConfirmedRequests() + 1;
            adminEventServiceClient.updateConfirmedRequests(event.getId(), confirmedRequests);
        }

        return requestMapper.toDto(requestRepository.save(eventRequest));
    }

    @Override
    public RequestDto cancelRequest(long userId, long requestId) {
        Request request =
                requestRepository.findByIdAndRequesterId(requestId, userId).orElseThrow(() -> new NotFoundException(MessageFormat.format("Request with id={0} was not found", requestId)));
        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<Request> getConfirmedRequests(Long eventId, RequestStatus status) {
        return requestRepository.findAllByEventIdAndStatus(eventId, status);
    }

    @Override
    public List<RequestDto> getEventRequests(Long eventId) {
        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    private User findUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException(MessageFormat.format("User " +
                                                                                                            "with " +
                                                                                                            "id={0} " +
                                                                                                            "was not " +
                                                                                                            "found",
                userId)));
    }

    private EventFullDto findEventById(long eventId) {
        return eventServiceClient.getById(eventId);
    }

    @Override
    public List<RequestDto> getRequests(long userId, long eventId) {
        EventFullDto event = findEventById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User is not the owner of the event");
        }

        List<Request> requests = requestRepository.findAllByEventId(eventId);

        return requests.stream().map(requestMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<RequestDto> getRequestsByUserIdAndEventIdAndRequestIdIn(long userId, long eventId,
                                                                        List<Long> requestIds) {
        EventFullDto event = findEventById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User is not the owner of the event");
        }

        List<Request> requests = requestRepository.findAllById(requestIds);

        for (Request request : requests) {
            if (!request.getEventId().equals(eventId)) {
                throw new NotFoundException("Request does not belong to the specified event");
            }
        }

        return requests.stream().map(requestMapper::toDto).collect(Collectors.toList());
    }

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    @Override
    public List<RequestDto> saveAll(List<RequestDto> requests) {
        List<Request> requestEntities = requests.stream().map(requestMapper::toEntity).collect(Collectors.toList());
        Long eventId = requestEntities.get(0).getEventId();
        EventFullDto event = findEventById(eventId);
        int currentConfirmedRequests = event.getConfirmedRequests();
        int confirmedReq = (int) requestEntities.stream().filter(r -> r.getStatus() == RequestStatus.CONFIRMED).count();
        int notConfirmedReq = requestEntities.size() - confirmedReq;
        int confirmedRequests = currentConfirmedRequests + confirmedReq - notConfirmedReq;
        adminEventServiceClient.updateConfirmedRequests(eventId, confirmedRequests);
        List<Request> savedRequests = requestRepository.saveAllAndFlush(requestEntities);

        return savedRequests.stream().map(requestMapper::toDto).collect(Collectors.toList());
    }
}
