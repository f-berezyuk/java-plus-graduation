package ru.practicum.interaction.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "event_service", path = "/admin/events")
public interface AdminEventServiceClient {
    @PatchMapping("/{eventId}/confirmed-requests")
    void updateConfirmedRequests(@PathVariable Long eventId, @RequestParam int confirmedRequests);
}
