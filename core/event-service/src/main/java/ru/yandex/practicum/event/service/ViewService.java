package ru.yandex.practicum.event.service;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import ru.yandex.practicum.event.model.Event;

public interface ViewService {
    void registerAll(List<Event> events, HttpServletRequest request);

    void register(Event event, HttpServletRequest request);
}
