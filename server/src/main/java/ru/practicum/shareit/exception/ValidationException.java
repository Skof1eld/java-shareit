package ru.practicum.shareit.exception;

public class ValidationException extends RuntimeException {
    public ValidationException() {
        super("Введены невалидные данные");
    }

    public ValidationException(String msg) {
        super(msg);
    }
}
