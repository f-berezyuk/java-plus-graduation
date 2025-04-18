package ru.practicum.feign.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "categories-service", path = "/categories")
public interface CategoriesServiceClient {
}