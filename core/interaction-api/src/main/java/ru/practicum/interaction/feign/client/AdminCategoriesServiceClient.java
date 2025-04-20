package ru.practicum.interaction.feign.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "admin-categories-service", path = "/admin/categories")
public interface AdminCategoriesServiceClient {
}
