package ru.practicum.interaction.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.interaction.event.model.Location;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByLatAndLon(double lat, double lon);
}