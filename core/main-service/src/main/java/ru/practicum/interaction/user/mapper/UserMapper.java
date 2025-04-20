package ru.practicum.interaction.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.interaction.user.dto.UserDto;
import ru.practicum.interaction.user.dto.UserRequestDto;
import ru.practicum.interaction.user.dto.UserShortDto;
import ru.practicum.interaction.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    User toEntity(UserRequestDto dto);

    UserDto toDto(User entity);

    User toEntity(UserDto dto);

    UserShortDto toShortDto(User entity);
}
