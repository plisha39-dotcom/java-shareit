package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public List<User> findAll() {
        return List.copyOf(users.values());
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(nextId);
            nextId++;
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }
}
