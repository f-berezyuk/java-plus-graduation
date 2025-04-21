package ru.practicum.main.categories.mapper;

import org.mapstruct.*;
import ru.practicum.main.categories.dto.CategoryDto;
import ru.practicum.main.categories.dto.NewCategoryDto;
import ru.practicum.main.categories.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "id", ignore = true)
    Category toEntity(NewCategoryDto newCategoryDto);

    CategoryDto toDto(Category category);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Category update(@MappingTarget Category category, NewCategoryDto updateCategoryDto);
}
