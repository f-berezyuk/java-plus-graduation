package ru.practicum.interaction.feign.client;

import java.util.List;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.interaction.dto.user.UserDto;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserServiceClient {
    @GetMapping("/{userId}")
    UserDto getUser(@PathVariable(name = "userId") Long userId);

    @GetMapping
    List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                           @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                           @Positive @RequestParam(defaultValue = "10") Integer size);
}
