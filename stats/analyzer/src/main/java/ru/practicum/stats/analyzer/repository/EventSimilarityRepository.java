package ru.practicum.stats.analyzer.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.stats.analyzer.model.EventSimilarity;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    List<EventSimilarity> findAllByEventAIn(Set<Long> eventIds, PageRequest pageRequest);

    List<EventSimilarity> findAllByEventBIn(Set<Long> eventIds, PageRequest pageRequest);

    List<EventSimilarity> findAllByEventA(Long eventId, PageRequest pageRequest);

    List<EventSimilarity> findAllByEventB(Long eventId, PageRequest pageRequest);

    boolean existsByEventAAndEventB(Long eventA, Long eventB);

    EventSimilarity findByEventAAndEventB(Long eventA, Long eventB);
}