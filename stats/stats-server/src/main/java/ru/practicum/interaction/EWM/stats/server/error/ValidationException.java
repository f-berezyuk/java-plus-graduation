package ru.practicum.interaction.EWM.stats.server.error;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
