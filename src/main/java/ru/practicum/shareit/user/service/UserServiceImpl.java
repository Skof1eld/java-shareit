package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь", userId);
        }
        return UserMapper.mapToDto(user.get());
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        isValidForCreation(userDto);
        isUserWithEmailExist(userDto.getEmail());
        User user = UserMapper.mapToEntity(userDto);
        userDto = UserMapper.mapToDto(userRepository.save(user));
        log.info("Пользователь с идентификатором {} и почтой {} был создан", user.getId(), user.getEmail());
        return userDto;
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        isValidForUpdate(userDto);
        isUserWithEmailExist(userDto.getEmail(), userId);
        userDto.setId(userId);
        Optional<User> user = userRepository.findById(userDto.getId());
        if (userDto.getId() == null || user == null) {
            throw new NotFoundException("Пользователь", userDto.getId());
        }
        UserMapper.updateEntityFromDto(userDto, user.get());
        userDto = UserMapper.mapToDto(userRepository.save(user.get()));
        log.info("Данные пользователя с идентификатором {} были обновлены", user.get().getId());
        return userDto;
    }

    @Override
    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

    private void isValidForCreation(UserDto userDto) {
        if (userDto.getEmail() == null
                || userDto.getEmail().isEmpty()
                || userDto.getName() == null
                || userDto.getName().isEmpty()) {
            throw new ValidationException();
        }
    }

    private void isValidForUpdate(UserDto userDto) {
        if (userDto.getEmail() != null
                && userDto.getEmail().isEmpty()
                || userDto.getName() != null
                && userDto.getName().isEmpty()) {
            throw new ValidationException();
        }
    }

    private void isUserWithEmailExist(String email) {
        isUserWithEmailExist(email, -1L);
    }

    private void isUserWithEmailExist(String email, Long userId) {
        if (email != null) {
            User existingUser = userRepository.findByEmail(email);
            if (existingUser != null && !Objects.equals(existingUser.getId(), userId)) {
                throw new AlreadyExistsException();
            }
        }
    }
}
