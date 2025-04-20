package ru.practicum.interaction.statistic.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.ewm.stats.client.StatsClient;
import ru.practicum.interaction.EWM.stats.dto.EndpointHit;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private static final String APP_NAME = "ewm";
    private final StatsClient statsClient;
    private final DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void createStats(String uri, String ip) {
        log.info("Create stats for URI: {}, IP: {}", uri, ip);
        EndpointHit hitDto = EndpointHit.builder()
                .uri(uri)
                .ip(ip)
                .app(APP_NAME)
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.hit(hitDto);
    }
}
