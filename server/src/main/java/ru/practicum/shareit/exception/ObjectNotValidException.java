package ru.practicum.shareit.exception;

public class ObjectNotValidException extends RuntimeException {
    public ObjectNotValidException() {
        super("Введены невалидные данные");
    }

    public ObjectNotValidException(String msg) {
        super(msg);
    }
}
