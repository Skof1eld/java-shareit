package ru.practicum.shareit.exception;

public class NoRightsForUpdateException extends RuntimeException {
    public NoRightsForUpdateException() {
        super("У вас нет прав изменять этот объект");
    }
}
