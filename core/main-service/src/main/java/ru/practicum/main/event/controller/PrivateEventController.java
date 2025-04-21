package ru.practicum.main.event.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.NewEventDto;
import ru.practicum.interaction.dto.event.UpdateEventUserRequest;
import ru.practicum.interaction.dto.request.RequestDto;
import ru.practicum.main.event.service.EventService;

@SuppressWarnings("SpringOmittedPathVariableParameterInspection")
@RequestMapping("/users/{userId}/events")
@RestController
@AllArgsConstructor
public class PrivateEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAllEvents(@PathVariable Long userId,
                                            @RequestParam(required = false, defaultValue = "0") int from,
                                            @RequestParam(required = false, defaultValue = "10") int size,
                                            HttpServletRequest httpServletRequest) {
        return eventService.getAllByUserId(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId,
                                 @RequestBody @Valid NewEventDto newEventDto,
                                 HttpServletRequest httpServletRequest) {
        return eventService.addEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable Long userId,
                                 @PathVariable Long eventId,
                                 HttpServletRequest httpServletRequest) {
        return eventService.getEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest request,
                                    HttpServletRequest httpServletRequest) {
        return eventService.updateEvent(userId, eventId, request);
    }

    @GetMapping("/{eventId}/requests")
    public List<RequestDto> getRequests(@PathVariable Long userId,
                                        @PathVariable Long eventId,
                                        HttpServletRequest httpServletRequest) {
        return eventService.getRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequests(@PathVariable Long userId,
                                                         @PathVariable Long eventId,
                                                         @RequestBody @Valid EventRequestStatusUpdateRequest request,
                                                         HttpServletRequest httpServletRequest) {
        return eventService.updateRequest(userId, eventId, request);
    }
}
