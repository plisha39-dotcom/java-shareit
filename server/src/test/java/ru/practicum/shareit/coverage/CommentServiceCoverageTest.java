package ru.practicum.shareit.coverage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.CommentServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceCoverageTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @InjectMocks
    private CommentServiceImpl service;

    @Test
    void unknownAuthorCannotCreateComment() {
        CommentCreateDto input = new CommentCreateDto();
        input.setText("Good drill");
        assertThrows(NotFoundException.class, () -> service.create(input, -1L, 5L));
    }

    @Test
    void unknownItemCannotBeCommentedOn() {
        CommentCreateDto input = new CommentCreateDto();
        input.setText("Good drill");
        User author = new User();
        author.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        assertThrows(NotFoundException.class, () -> service.create(input, 1L, -1L));
    }
}
