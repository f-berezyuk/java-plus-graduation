package ru.practicum.EWM.stats.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.EWM.stats.dto.EndpointHit;
import ru.practicum.EWM.stats.server.model.EndpointHitEntity;

@Mapper(componentModel = "spring")
public interface EndpointHitMapper {
    EndpointHit toDto(EndpointHitEntity entity);

    @Mapping(target = "id", ignore = true)
    EndpointHitEntity toEntity(EndpointHit dto);
}
