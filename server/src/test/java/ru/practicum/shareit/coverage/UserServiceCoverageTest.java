package ru.practicum.shareit.coverage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceCoverageTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @InjectMocks
    private UserServiceImpl service;

    @Test
    void creationRejectsMissingAndBlankFields() {
        UserDto input = dto("Alice", "alice@example.com");
        input.setName(null);
        assertThrows(ValidationException.class, () -> service.create(input));
        input.setName(" ");
        assertThrows(ValidationException.class, () -> service.create(input));
        input.setName("Alice");
        input.setEmail(null);
        assertThrows(ValidationException.class, () -> service.create(input));
        input.setEmail(" ");
        assertThrows(ValidationException.class, () -> service.create(input));
    }

    @Test
    void updateRejectsBlankEmailAndName() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        UserDto change = new UserDto();
        change.setEmail(" ");
        assertThrows(ValidationException.class, () -> service.update(change, 1L));
        change.setEmail(null);
        change.setName(" ");
        assertThrows(ValidationException.class, () -> service.update(change, 1L));
    }

    @Test
    void updateRejectsAnotherUsersEmail() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(userRepository.existsByEmailAndIdNot("taken@example.com", 1L)).thenReturn(true);
        UserDto change = new UserDto();
        change.setEmail("taken@example.com");
        assertThrows(ConflictException.class, () -> service.update(change, 1L));
    }

    @Test
    void updateSupportsEmailOnlyAndUnchangedName() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserDto change = new UserDto();
        change.setEmail("new@example.com");

        UserDto result = service.update(change, 1L);

        assertEquals("new@example.com", result.getEmail());
        assertEquals("Alice", result.getName());
    }

    @Test
    void updateWithNoFieldsPreservesUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserDto result = service.update(new UserDto(), 1L);
        assertEquals("Alice", result.getName());
        assertEquals("old@example.com", result.getEmail());
    }

    @Test
    void deletionIsBlockedWhenUserOwnsItems() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRepository.existsByOwner_Id(1L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.delete(1L));
        verify(userRepository, never()).deleteById(1L);
    }

    @Test
    void deletionIsBlockedWhenUserHasBookings() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(bookingRepository.existsByBooker_Id(1L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.delete(1L));
        verify(userRepository, never()).deleteById(1L);
    }

    @Test
    void deletionIsBlockedWhenUserHasComments() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(commentRepository.existsByAuthor_Id(1L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.delete(1L));
        verify(userRepository, never()).deleteById(1L);
    }

    @Test
    void missingUserCannotBeRead() {
        assertThrows(NotFoundException.class, () -> service.findById(-1L));
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("Alice");
        user.setEmail("old@example.com");
        return user;
    }

    private static UserDto dto(String name, String email) {
        UserDto input = new UserDto();
        input.setName(name);
        input.setEmail(email);
        return input;
    }
}
