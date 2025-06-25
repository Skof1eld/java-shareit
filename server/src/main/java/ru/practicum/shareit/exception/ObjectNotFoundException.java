package ru.practicum.shareit.exception;

public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException() {
        super("Объект не найден");
    }

    public ObjectNotFoundException(Long id) {
        super(String.format("Объект c id - %d не найден", id));
    }

    public ObjectNotFoundException(String objectName, Long id) {
        super(String.format("%s c id - %d не найден", objectName, id));
    }
}
