package ru.yandex.practicum.request.service;

public interface StatsService {
    void createStats(String uri, String ip);

    String getName();
}
