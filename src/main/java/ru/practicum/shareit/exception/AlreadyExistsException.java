package ru.practicum.shareit.exception;

public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException() {
        super("Объект уже существует");
    }

    public AlreadyExistsException(String fieldName, String value) {
        super(value == null || value.isEmpty()
                ? String.format("Объект с таким значением '%s' уже существует", fieldName)
                : String.format("Объект с таким значением '%s'(%s) уже существует", fieldName, value));
    }
}
