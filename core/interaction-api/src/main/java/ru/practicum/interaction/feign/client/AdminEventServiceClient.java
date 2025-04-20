package ru.practicum.interaction.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Для административных эндпоинтов
@FeignClient(name = "event-service", contextId = "adminEventClient", path = "/admin/events")
public interface AdminEventServiceClient {
    @PostMapping("/{eventId}/confirmed-requests")
    void internalUpdateConfirmedRequests(@PathVariable Long eventId,
                                         @RequestParam int confirmedRequests);
}
