package ru.practicum.shareit.exception;

public class ValidationException extends RuntimeException {
    public ValidationException() {
        super("Введены некорректные данные");
    }
}
