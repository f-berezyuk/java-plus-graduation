package ru.yandex.practicum.service;

public interface StatsService {
    void createStats(String uri, String ip);

    String getName();
}
