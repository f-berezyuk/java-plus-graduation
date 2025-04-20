package ru.practicum.interaction.EWM.stats.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.interaction.EWM.stats.dto.EndpointHit;
import ru.practicum.interaction.EWM.stats.server.model.EndpointHitEntity;

@Mapper(componentModel = "spring")
public interface EndpointHitMapper {
    EndpointHit toDto(EndpointHitEntity entity);

    @Mapping(target = "id", ignore = true)
    EndpointHitEntity toEntity(EndpointHit dto);
}
