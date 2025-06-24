package ru.practicum.interaction.feign.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.practicum.interaction.dto.request.RequestDto;
import ru.practicum.interaction.dto.request.req_rsp.RequestsSaveAllReq;

@FeignClient(name = "request-service", path = "/requests")
public interface RequestServiceClient {
    @GetMapping("/user/{userId}/event/{eventId}")
    List<RequestDto> getRequests(@PathVariable Long eventId, @PathVariable Long userId);
    @GetMapping("/event/{eventId}")
    List<RequestDto> getRequests(@PathVariable Long eventId);
    @PostMapping("/save-all")
    void saveAll(RequestsSaveAllReq request);
}
