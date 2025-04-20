package ru.practicum.interaction.compilation.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.interaction.compilation.dto.CompilationDto;
import ru.practicum.interaction.compilation.model.Compilation;
import ru.practicum.interaction.event.mapper.EventMapper;

@Component
@AllArgsConstructor
public class CompilationMapper {
    private final EventMapper eventMapper;

    public CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(compilation.getEvents().stream().map(eventMapper::toShortDto).toList())
                .build();
    }
}