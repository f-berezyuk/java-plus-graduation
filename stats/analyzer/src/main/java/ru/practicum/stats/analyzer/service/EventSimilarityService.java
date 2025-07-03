package ru.practicum.stats.analyzer.service;

import java.time.Duration;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.analyzer.handler.EventSimilarityHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityService implements Runnable {

    private final Consumer<Long, EventSimilarityAvro> consumer;
    private final EventSimilarityHandler eventSimilarityHandler;

    @Value("${analyzer.topic.events-similarity}")
    private String topicEventSimilarity;
    @Value("${spring.kafka.consumer.poll-timeout}")
    private int pollTimeout;

    public void run() {
        try {
            consumer.subscribe(List.of(topicEventSimilarity));
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            //noinspection InfiniteLoopStatement
            while (true) {
                ConsumerRecords<Long, EventSimilarityAvro> records = consumer.poll(Duration.ofMillis(pollTimeout));

                for (ConsumerRecord<Long, EventSimilarityAvro> record : records) {
                    EventSimilarityAvro eventSimilarity = record.value();
                    log.info("Получили коэффициент схожести: {}", eventSimilarity);

                    eventSimilarityHandler.handle(eventSimilarity);
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка чтения данных из топика {}", topicEventSimilarity);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
            }
        }
    }
}
