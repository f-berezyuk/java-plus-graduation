package ru.yandex.practicum.request.service;

import java.util.List;

import ru.practicum.interaction.dto.request.RequestDto;

import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.request.model.RequestStatus;

public interface RequestService {
    List<RequestDto> getRequests(long userId, long eventId);

    List<RequestDto> getRequests(long userId);

    List<RequestDto> getRequestsByUserIdAndEventIdAndRequestIdIn(long userId, long eventId, List<Long> requestIds);

    RequestDto createRequest(long userId, long eventId);

    @SuppressWarnings("UnusedReturnValue")
    List<RequestDto> saveAll(List<RequestDto> requests);

    RequestDto cancelRequest(long userId, long requestId);

    List<Request> getConfirmedRequests(Long eventId, RequestStatus status);

    List<RequestDto> getEventRequests(Long eventId);

    boolean isUserTakePart(Long userId, Long eventId);
}
