package ru.practicum.main.categories.service;

import ru.practicum.main.categories.dto.CategoryDto;
import ru.practicum.main.categories.dto.NewCategoryDto;
import ru.practicum.main.categories.model.Category;

import java.util.List;

public interface CategoriesService {
    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(Long id, NewCategoryDto newCategoryDto);

    void deleteCategory(Long id);

    CategoryDto findBy(Long id);

    List<CategoryDto> findBy(int from, int size);

    Category getOrThrow(Long id);
}
