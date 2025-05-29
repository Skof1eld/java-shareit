package ru.practicum.shareit.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super("Объект не найден");
    }

    public NotFoundException(Long id) {
        super(String.format("Объект c id - %d не найден", id));
    }

    public NotFoundException(String name, Long id) {
        super(String.format("%s c id - %d не найден", name, id));
    }
}
