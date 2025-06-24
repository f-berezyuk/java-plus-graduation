package ru.yandex.practicum.request.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.interaction.dto.request.RequestDto;

import ru.yandex.practicum.request.model.Request;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "requester", source = "requesterId")
    RequestDto toDto(Request request);

    List<RequestDto> toDtos(List<Request> requests);

    @Mapping(target = "eventId", source = "event")
    @Mapping(target = "requesterId", source = "requester")
    Request toEntity(RequestDto requestDto);

    /**
     * @noinspection unused
     */
    List<Request> toEntities(List<RequestDto> requestDtos);
}
