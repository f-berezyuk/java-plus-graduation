package ru.yandex.practicum.request.service;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.google.protobuf.Timestamp;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.interaction.common.ConflictException;
import ru.practicum.interaction.common.NotFoundException;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventStateDto;
import ru.practicum.interaction.dto.request.RequestDto;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.feign.client.AdminEventServiceClient;
import ru.practicum.interaction.feign.client.EventServiceClient;
import ru.practicum.interaction.feign.client.UserServiceClient;
import ru.practicum.stats.client.CollectorClient;

import ru.yandex.practicum.request.mapper.RequestMapper;
import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.request.model.RequestStatus;
import ru.yandex.practicum.request.repository.RequestRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final CollectorClient collectorClient;
    private final RequestRepository requestRepository;
    private final UserServiceClient userServiceClient;
    private final EventServiceClient eventServiceClient;
    private final RequestMapper requestMapper;
    private final AdminEventServiceClient adminEventServiceClient;

    @Override
    public List<RequestDto> getRequests(long userId) {
        UserDto user = findUserById(userId);
        return requestMapper.toDtos(requestRepository.findByRequesterId(user.getId()));
    }

    @Override
    public RequestDto createRequest(long userId, long eventId) {
        EventFullDto event = findEventById(eventId);
        UserDto user = findUserById(userId);

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

        Request eventRequest = new Request(null, LocalDateTime.now(), eventId, user.getId(), RequestStatus.PENDING);
        if (!event.isRequestModeration()) {
            eventRequest.setStatus(RequestStatus.CONFIRMED);
        }

        if (event.getParticipantLimit() == 0) {
            eventRequest.setStatus(RequestStatus.CONFIRMED);
        }

        if (eventRequest.getStatus() == RequestStatus.CONFIRMED) {
            int confirmedRequests = event.getConfirmedRequests() + 1;
            adminEventServiceClient.internalUpdateConfirmedRequests(event.getId(), confirmedRequests);
            collectorClient.sendUserAction(createUserAction(eventId, userId, ActionTypeProto.ACTION_REGISTER,
                    Instant.now()));
        }

        return requestMapper.toDto(requestRepository.save(eventRequest));
    }

    @SuppressWarnings("SameParameterValue")
    private UserActionProto createUserAction(Long eventId, Long userId, ActionTypeProto type, Instant timestamp) {
        return UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(type)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(timestamp.getEpochSecond())
                        .setNanos(timestamp.getNano())
                        .build())
                .build();
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

    @Override
    public boolean isUserTakePart(Long userId, Long eventId) {
        var request = requestRepository.findAllByEventId(eventId);
        return request.stream().anyMatch(r -> r.getRequesterId().equals(userId));
    }

    private UserDto findUserById(long userId) {
        return userServiceClient.getUser(userId);
    }

    private EventFullDto findEventById(long eventId) {
        try {
            return eventServiceClient.getEventInternal(eventId);
        } catch (FeignException.NotFound notFound) {
            throw new ConflictException(MessageFormat.format("Event with id={0} was not found", eventId));
        } catch (FeignException fe) {
            throw new RuntimeException(fe);
        }
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
        adminEventServiceClient.internalUpdateConfirmedRequests(eventId, confirmedRequests);
        List<Request> savedRequests = requestRepository.saveAllAndFlush(requestEntities);

        return savedRequests.stream().map(requestMapper::toDto).collect(Collectors.toList());
    }
}
