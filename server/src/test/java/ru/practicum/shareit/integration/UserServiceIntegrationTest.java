package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {
    @Autowired
    private UserService users;

    @Test
    void createFindUpdateAndDeletePersistThroughJpa() {
        UserDto created = users.create(user("Alice", "alice.integration@example.com"));
        assertNotNull(created.getId());
        assertEquals("Alice", users.findById(created.getId()).getName());
        assertTrue(users.findAll().stream().anyMatch(u -> u.getId().equals(created.getId())));

        UserDto changes = new UserDto();
        changes.setName("Alice Updated");
        UserDto updated = users.update(changes, created.getId());
        assertEquals("Alice Updated", updated.getName());
        assertEquals(created.getEmail(), updated.getEmail());

        users.delete(created.getId());
        assertThrows(NotFoundException.class, () -> users.findById(created.getId()));
        assertFalse(users.findAll().stream().anyMatch(u -> u.getId().equals(created.getId())));
    }

    @Test
    void duplicateEmailsAreRejected() {
        users.create(user("One", "duplicate.integration@example.com"));
        assertThrows(ConflictException.class,
                () -> users.create(user("Two", "duplicate.integration@example.com")));
    }

    @Test
    void missingUserCannotBeUpdatedOrDeleted() {
        assertThrows(NotFoundException.class, () -> users.update(new UserDto(), -999L));
        assertThrows(NotFoundException.class, () -> users.delete(-999L));
    }

    private UserDto user(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }
}
