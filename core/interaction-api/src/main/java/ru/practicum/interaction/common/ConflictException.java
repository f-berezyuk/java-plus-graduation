package ru.practicum.interaction.common;

public class ConflictException extends RuntimeException {
    public ConflictException(final String message) {
        super(message);
    }
}
