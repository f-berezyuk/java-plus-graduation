package ru.practicum.EWM.stats.server.service;

import java.time.LocalDateTime;
import java.util.List;

import ru.practicum.EWM.stats.dto.EndpointHit;
import ru.practicum.EWM.stats.dto.ViewStats;

public interface StatsService {
    List<ViewStats> getStats(LocalDateTime start,
                             LocalDateTime end,
                             List<String> uris,
                             boolean unique);

    EndpointHit saveHit(EndpointHit hit);
}
