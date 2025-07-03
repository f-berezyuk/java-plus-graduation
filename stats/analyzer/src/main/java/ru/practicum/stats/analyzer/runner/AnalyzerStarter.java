package ru.practicum.stats.analyzer.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.stats.analyzer.service.EventSimilarityService;
import ru.practicum.stats.analyzer.service.UserActionService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerStarter implements CommandLineRunner {
    private final UserActionService userActionService;
    private final EventSimilarityService eventSimilarityService;

    @Override
    public void run(String... args) {
        Thread userActionThread = new Thread(userActionService);
        userActionThread.setName("userActionHandlerThread");
        userActionThread.start();

        log.info("Запуск userActionHandlerThread");
        eventSimilarityService.run();
    }
}
