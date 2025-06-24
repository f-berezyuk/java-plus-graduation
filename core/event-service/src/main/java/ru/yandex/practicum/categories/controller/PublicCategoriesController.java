package ru.yandex.practicum.categories.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.interaction.dto.categories.CategoryDto;

import ru.yandex.practicum.categories.service.CategoriesService;

@Validated
@RequestMapping("/categories")
@RequiredArgsConstructor
@RestController
public class PublicCategoriesController {
    private final CategoriesService service;

    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") Integer from,
                                           @RequestParam(defaultValue = "10") Integer size) {
        return service.findBy(from, size);
    }

    //Получение категории по id
    @GetMapping("/{catId}")
    public CategoryDto getCategoryBy(@PathVariable Long catId) {
        return service.findBy(catId);
    }
}
