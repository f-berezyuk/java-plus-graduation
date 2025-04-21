package ru.practicum.main.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.main.user.dto.UserDto;
import ru.practicum.main.user.dto.UserRequestDto;
import ru.practicum.main.user.dto.UserShortDto;
import ru.practicum.main.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    User toEntity(UserRequestDto dto);

    UserDto toDto(User entity);

    User toEntity(UserDto dto);

    UserShortDto toShortDto(User entity);
}
