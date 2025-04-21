package ru.practicum.main.common;

public class ConflictException extends RuntimeException {
    public ConflictException(final String message) {
        super(message);
    }
}
