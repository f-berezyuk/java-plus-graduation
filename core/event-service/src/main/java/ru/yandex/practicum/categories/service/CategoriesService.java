package ru.yandex.practicum.categories.service;

import java.util.List;

import ru.practicum.interaction.dto.categories.CategoryDto;
import ru.practicum.interaction.dto.categories.NewCategoryDto;

import ru.yandex.practicum.categories.model.Category;

public interface CategoriesService {
    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(Long id, NewCategoryDto newCategoryDto);

    void deleteCategory(Long id);

    CategoryDto findBy(Long id);

    List<CategoryDto> findBy(int from, int size);

    Category getOrThrow(Long id);
}
