package ru.practicum.interaction.request.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.interaction.dto.request.RequestDto;
import ru.practicum.interaction.dto.request.req_rsp.RequestsSaveAllReq;
import ru.practicum.interaction.feign.client.RequestServiceClient;
import ru.practicum.interaction.request.service.RequestService;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class InternalRequestController implements RequestServiceClient {
    private final RequestService requestService;

    @Override
    @GetMapping("/user/{userId}/event/{eventId}")
    public List<RequestDto> getRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        return requestService.getRequests(eventId, userId);
    }

    @Override
    @GetMapping("/event/{eventId}")
    public List<RequestDto> getRequests(@PathVariable Long eventId) {
        return requestService.getEventRequests(eventId);
    }

    @Override
    @PostMapping("/save-all")
    public void saveAll(RequestsSaveAllReq request) {
        requestService.saveAll(request.getRequests());
    }
}
