package ru.practicum.interaction.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.interaction.dto.user.UserDto;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserServiceClient {
    @GetMapping("/{userId}")
    UserDto getUser(@PathVariable(name = "userId") Long userId);
}
