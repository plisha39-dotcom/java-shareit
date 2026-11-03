package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentServiceIntegrationTest {
    @Autowired
    private CommentService comments;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private BookingRepository bookings;
    @Autowired
    private ItemRepository items;
    @Autowired
    private UserRepository users;

    @Test
    void onlyUserWithCompletedApprovedBookingCanComment() {
        User owner = users.save(TestData.user("Owner"));
        User booker = users.save(TestData.user("Booker"));
        User stranger = users.save(TestData.user("Stranger"));
        Item item = items.save(TestData.item(owner, null));
        bookings.save(TestData.booking(booker, item, Status.APPROVED,
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2)));

        CommentCreateDto input = new CommentCreateDto();
        input.setText("Good drill");
        CommentDto created = comments.create(input, booker.getId(), item.getId());
        assertNotNull(created.getId());
        assertEquals("Good drill", created.getText());
        assertEquals("Booker", created.getAuthorName());
        assertEquals(1, commentRepository.findByItem_Id(item.getId()).size());

        assertThrows(ValidationException.class,
                () -> comments.create(input, stranger.getId(), item.getId()));
    }
}
