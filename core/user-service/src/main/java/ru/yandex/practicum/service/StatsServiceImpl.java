package ru.yandex.practicum.service;

import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.EWM.stats.dto.EndpointHit;
import ru.practicum.interaction.feign.client.StatsServiceClient;

@Service
@Slf4j
public class StatsServiceImpl implements StatsService {
    @Value("${spring.application.name}")
    private String APP_NAME;
    private final StatsServiceClient statsClient;

    public StatsServiceImpl(@Lazy StatsServiceClient statsClient) {
        this.statsClient = statsClient;
    }

    @Override
    public void createStats(String uri, String ip) {
        log.info("Create stats for URI: {}, IP: {}", uri, ip);
        EndpointHit hitDto = EndpointHit.builder()
                .uri(uri)
                .ip(ip)
                .app(APP_NAME)
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.saveHit(hitDto);
    }

    @Override
    public String getName() {
        return APP_NAME;
    }
}
