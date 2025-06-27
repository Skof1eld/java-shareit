package ru.practicum.shareit.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super("Объект не найден");
    }

    public NotFoundException(Long id) {
        super(String.format("Объект c id - %d не найден", id));
    }

    public NotFoundException(String objectName, Long id) {
        super(String.format("%s c id - %d не найден", objectName, id));
    }
}
