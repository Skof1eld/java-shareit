package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    List<User> findAll();

    User getUserById(Long userId);

    User getUserByEmail(String email);

    User create(User user);

    User save(User user);

    void deleteUserById(Long userId);
}
