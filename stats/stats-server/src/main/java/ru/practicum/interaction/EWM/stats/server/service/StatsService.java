package ru.practicum.interaction.EWM.stats.server.service;

import java.time.LocalDateTime;
import java.util.List;

import ru.practicum.interaction.EWM.stats.dto.EndpointHit;
import ru.practicum.interaction.EWM.stats.dto.ViewStats;

public interface StatsService {
    List<ViewStats> getStats(LocalDateTime start,
                             LocalDateTime end,
                             List<String> uris,
                             boolean unique);

    EndpointHit saveHit(EndpointHit hit);
}
