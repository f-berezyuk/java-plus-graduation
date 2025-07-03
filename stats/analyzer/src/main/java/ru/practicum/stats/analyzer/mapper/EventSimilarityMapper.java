package ru.practicum.stats.analyzer.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.analyzer.model.EventSimilarity;

@UtilityClass
public class EventSimilarityMapper {

    public EventSimilarity mapToEventSimilarity(EventSimilarityAvro similarityAvro) {
        return EventSimilarity.builder()
                .eventA(similarityAvro.getEventA())
                .eventB(similarityAvro.getEventB())
                .score(similarityAvro.getScore())
                .timestamp(similarityAvro.getTimestamp())
                .build();
    }
}
