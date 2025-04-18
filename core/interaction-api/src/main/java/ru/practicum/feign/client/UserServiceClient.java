package ru.practicum.feign.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserServiceClient {
}
