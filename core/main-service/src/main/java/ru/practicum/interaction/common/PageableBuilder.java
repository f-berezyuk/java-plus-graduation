package ru.practicum.interaction.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PageableBuilder {
    private static int getPageNumber(int from, int size) {
        return from / size;
    }

    public static PageRequest getPageable(int from, int size) {
        return PageRequest.of(getPageNumber(from, size), size);
    }

    public static PageRequest getPageable(int from, int size, String sort) {
        Sort sorting = Sort.by(sort);
        return PageRequest.of(getPageNumber(from, size), size, sorting);
    }
}
