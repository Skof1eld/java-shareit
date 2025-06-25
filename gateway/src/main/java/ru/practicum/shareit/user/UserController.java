package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ObjectNotValidException;
import ru.practicum.shareit.user.dto.UserDto;

@Controller
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(path = "/users")
public class UserController {
    private final UserClient client;

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Get all users");
        return client.getAllUsers();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable Long userId) {
        log.info("Get user with userId={}", userId);
        return client.getUserById(userId);
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Add new user");
        return client.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable Long userId, @Valid @RequestBody UserDto userDto) {
        isValidForUpdate(userDto);
        log.info("Patch user with userId={}", userId);
        return client.updateUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUserById(@PathVariable Long userId) {
        log.info("Delete user with userId={}", userId);
        return client.deleteUserById(userId);
    }

    private void isValidForUpdate(UserDto userDto) {
        if (userDto.getEmail() != null
                && userDto.getEmail().isEmpty()
                || userDto.getName() != null
                && userDto.getName().isEmpty()) {
            throw new ObjectNotValidException();
        }
    }
}
