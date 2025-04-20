package ru.yandex.practicum.compilation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.compilation.model.Compilation;

@Repository
public interface CompilationsRepository extends JpaRepository<Compilation, Long> {
    Page<Compilation> findByPinned(boolean pinned, Pageable pageable);
}
