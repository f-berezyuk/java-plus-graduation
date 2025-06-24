package ru.yandex.practicum.user.service;

import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserRequestDto;
import ru.yandex.practicum.user.model.User;

import java.util.List;

public interface UserService {
    UserDto getUser(Long userId);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    UserDto registerUser(UserRequestDto userRequestDto);

    void delete(Long userId);

    /**
     * @noinspection unused
     */
    User getOrThrow(Long id);
}
