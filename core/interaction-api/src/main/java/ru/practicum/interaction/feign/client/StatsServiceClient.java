package ru.practicum.interaction.feign.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.interaction.EWM.stats.dto.EndpointHit;

@FeignClient(name = "stats-server")
public interface StatsServiceClient {
    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    void saveHit(@RequestBody @Valid EndpointHit hit);
}
