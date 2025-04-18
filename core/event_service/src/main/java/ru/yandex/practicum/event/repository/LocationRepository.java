package ru.yandex.practicum.event.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.yandex.practicum.event.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByLatAndLon(double lat, double lon);
}