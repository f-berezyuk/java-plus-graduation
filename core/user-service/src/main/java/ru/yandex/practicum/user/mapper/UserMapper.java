package ru.yandex.practicum.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserRequestDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.yandex.practicum.user.model.User;

/**
 * @noinspection unused
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    User toEntity(UserRequestDto dto);

    UserDto toDto(User entity);

    User toEntity(UserDto dto);

    UserShortDto toShortDto(User entity);
}
