package ru.practicum.interaction.categories.service;

import ru.practicum.interaction.categories.dto.CategoryDto;
import ru.practicum.interaction.categories.dto.NewCategoryDto;
import ru.practicum.interaction.categories.model.Category;

import java.util.List;

public interface CategoriesService {
    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(Long id, NewCategoryDto newCategoryDto);

    void deleteCategory(Long id);

    CategoryDto findBy(Long id);

    List<CategoryDto> findBy(int from, int size);

    Category getOrThrow(Long id);
}
