package ru.yandex.practicum.event.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.LocationDto;
import ru.practicum.interaction.dto.event.NewEventDto;
import ru.practicum.interaction.dto.event.UpdateEventAdminRequest;
import ru.practicum.interaction.dto.event.UpdateEventUserRequest;

import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.model.Location;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "eventDate", expression = "java(event.getEventDate())")
    @Mapping(target = "rating", expression = "java(event.getRating() == null ? 0.0 : event.getRating())")
    EventShortDto toShortDto(Event event);

    @Mapping(target = "initiator.id", source = "userId")
    @Mapping(target = "rating", expression = "java(event.getRating() == null ? 0.0 : event.getRating())")
    EventFullDto toFullDto(Event event);

    @Mapping(target = "category.id", source = "category")
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "state", expression = "java(EventState.PENDING)")
    @Mapping(target = "participantLimit", source = "participantLimit", defaultValue = "0")
    Event toEntity(NewEventDto newEventDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category.id", source = "category")
    void updateFromAdminRequest(UpdateEventAdminRequest updateRequest, @MappingTarget Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category.id", source = "category")
    void updateFromUserRequest(UpdateEventUserRequest updateRequest, @MappingTarget Event event);

    Location toEntity(LocationDto location);

    LocationDto toDto(Location location);
}
