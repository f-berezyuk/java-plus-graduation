package ru.yandex.practicum.compilation.service;

import java.util.List;

import org.apache.coyote.BadRequestException;
import ru.practicum.interaction.dto.compilation.CompilationDto;
import ru.practicum.interaction.dto.compilation.NewCompilationDto;
import ru.practicum.interaction.dto.compilation.UpdateCompilationRequest;

public interface CompilationService {
    List<CompilationDto> getAll(boolean pinned, int from, int size);

    CompilationDto get(Long id);

    CompilationDto addNewCompilation(NewCompilationDto newCompilationDto) throws BadRequestException;

    void delete(Long id);

    CompilationDto update(Long id, UpdateCompilationRequest request) throws BadRequestException;
}
