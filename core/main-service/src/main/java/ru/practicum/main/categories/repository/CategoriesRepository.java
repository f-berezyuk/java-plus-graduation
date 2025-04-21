package ru.practicum.main.categories.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.categories.model.Category;

public interface CategoriesRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}
