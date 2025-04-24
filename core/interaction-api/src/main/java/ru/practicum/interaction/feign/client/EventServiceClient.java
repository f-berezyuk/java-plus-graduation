package ru.practicum.interaction.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.interaction.dto.event.EventFullDto;

@FeignClient(name = "event_service", path = "/events")
public interface EventServiceClient {
    @GetMapping("/{id}")
    EventFullDto getById(@PathVariable Long id);
}
