package ru.yandex.practicum.event.service;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.request.RequestDto;

import ru.yandex.practicum.event.model.Event;

public interface EventService {
    List<EventShortDto> getAllByUserId(Long userId, int from, int size);

    EventFullDto addEvent(Long userId, @Valid NewEventDto newEventDto);

    EventFullDto getEvent(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, @Valid UpdateEventUserRequest request);

    List<RequestDto> getRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Boolean onlyAvailable, String sort, int from, int size,
                                        HttpServletRequest request);

    EventFullDto getPublicEvent(Long id, HttpServletRequest request);

    List<EventFullDto> getAllEvents(List<Long> users, List<String> states, List<Long> categories,
                                    LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);

    Event getOrThrow(Long eventId);
}
