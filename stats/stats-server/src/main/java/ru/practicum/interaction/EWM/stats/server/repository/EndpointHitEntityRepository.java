package ru.practicum.interaction.EWM.stats.server.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.interaction.EWM.stats.dto.ViewStats;
import ru.practicum.interaction.EWM.stats.server.model.EndpointHitEntity;

@Repository
public interface EndpointHitEntityRepository extends JpaRepository<EndpointHitEntity, Long> {

    @Query("SELECT new ru.practicum.interaction.EWM.stats.dto.ViewStats(s.app, s.uri, COUNT(s.id))" +
           "FROM EndpointHitEntity s " +
           "WHERE s.timestamp BETWEEN :start AND :end " +
           "GROUP BY s.app, s.uri " +
           "ORDER BY COUNT(s.id) DESC")
    List<ViewStats> getStatsWithHits(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.interaction.EWM.stats.dto.ViewStats(s.app, s.uri, COUNT(s.id))" +
           "FROM EndpointHitEntity s " +
           "WHERE s.timestamp BETWEEN :start AND :end AND s.uri IN :uris " +
           "GROUP BY s.app, s.uri " +
           "ORDER BY COUNT(s.id) DESC")
    List<ViewStats> getStatsWithHitsAndUris(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end,
                                            @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.interaction.EWM.stats.dto.ViewStats(s.app, s.uri, COUNT(distinct s.ip))" +
           "FROM EndpointHitEntity s " +
           "WHERE s.timestamp BETWEEN :start AND :end " +
           "GROUP BY s.app, s.uri " +
           "ORDER BY COUNT(distinct s.ip) DESC")
    List<ViewStats> getUniqueStatsWithHits(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.interaction.EWM.stats.dto.ViewStats(s.app, s.uri, COUNT(distinct s.ip))" +
           "FROM EndpointHitEntity s " +
           "WHERE s.timestamp BETWEEN :start AND :end AND s.uri IN :uris " +
           "GROUP BY s.app, s.uri " +
           "ORDER BY COUNT(distinct s.ip) DESC")
    List<ViewStats> getUniqueStatsWithHitsAndUris(@Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end,
                                                  @Param("uris") List<String> uris);
}