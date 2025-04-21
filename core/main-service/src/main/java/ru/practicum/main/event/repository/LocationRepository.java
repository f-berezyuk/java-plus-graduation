package ru.practicum.main.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.event.model.Location;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByLatAndLon(double lat, double lon);
}