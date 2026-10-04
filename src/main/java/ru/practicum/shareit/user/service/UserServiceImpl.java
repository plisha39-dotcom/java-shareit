package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    public UserServiceImpl(UserRepository userRepository) {
        this.repository = userRepository;
    }

    @Override
    public UserDto create(UserDto userDto) {
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new ValidationException("Имя пользователя отсутствует");
        }
        if (userDto.getEmail() == null || userDto.getEmail().isBlank() || !userDto.getEmail().contains("@")) {
            throw new ValidationException("Email введен не корректно или отсутствует");
        }
        boolean emailExists = repository.findAll().stream()
                                        .anyMatch(u -> u.getEmail().equals(userDto.getEmail()));
        if (emailExists) {
            throw new ConflictException("Пользователь с таким Email уже существует");
        }
        User user = UserMapper.toUser(userDto);
        User savedUser = repository.save(user);
        return UserMapper.toDto(savedUser);
    }

    @Override
    public UserDto update(UserDto userDto, Long userId) {
        User user = repository.findById(userId)
                  .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));
        if (userDto.getEmail() != null) {
            if (userDto.getEmail().isBlank() || !userDto.getEmail().contains("@")) {
                throw new ValidationException("Email введен не корректно или отсутствует");
            }
            boolean emailExists = repository.findAll().stream()
                                            .anyMatch(u -> u.getEmail().equals(userDto.getEmail()) &&
                                                    !u.getId().equals(userId));
            if (emailExists) {
                throw new ConflictException("Пользователь с таким Email уже существует");
            }
        }
        if (userDto.getName() != null) {
            if (userDto.getName().isBlank()) {
                throw new ValidationException("Имя пользователя отсутствует");
            }
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        User savedUser = repository.save(user);
        return UserMapper.toDto(savedUser);
    }

    @Override
    public UserDto findById(Long userId) {
        User user = repository.findById(userId)
                              .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        return repository.findAll().stream()
                         .map(UserMapper::toDto)
                         .toList();
    }

    @Override
    public void delete(Long userId) {
        repository.findById(userId)
                              .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));
        repository.delete(userId);
    }
}
