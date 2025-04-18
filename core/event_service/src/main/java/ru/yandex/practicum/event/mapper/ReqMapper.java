package ru.yandex.practicum.event.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.event.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestDto;

@Mapper(componentModel = "spring")
public interface ReqMapper {

    @Mapping(target = "ru/yandex/practicum/event", source = "ru/yandex/practicum/event")
    @Mapping(target = "created", source = "created")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "requester", source = "requester")
    @Mapping(target = "status", source = "status")
    ParticipationRequestDto toParticipationRequestDto(RequestDto requestDto);

    @InheritInverseConfiguration
    RequestDto toRequestDto(ParticipationRequestDto participationRequestDto);
}
