package ru.practicum.shareit.exception;

public class NoRightsForUpdateException extends RuntimeException {
    public NoRightsForUpdateException() {
        super("Не достаточно прав для изменения!");
    }
}
