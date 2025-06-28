package ru.practicum.interaction.feign.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.practicum.interaction.common.ValidationException;
import ru.practicum.interaction.dto.event.EventFullDto;

@FeignClient(name = "event-service", contextId = "publicEventClient", path = "/events")
public interface EventServiceClient {
    @GetMapping("/internal/{id}")
    EventFullDto getEventInternal(@PathVariable Long id);

    @GetMapping("/recommendation")
    List<EventFullDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") Long userId);

    @PutMapping("/{eventId}/like")
    void likeEvent(@PathVariable Long eventId, @RequestHeader("X-EWM-USER-ID") Long userId) throws ValidationException;
}
