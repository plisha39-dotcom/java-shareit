package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceIntegrationTest {
    @Autowired
    private BookingService bookings;
    @Autowired
    private ItemRepository items;
    @Autowired
    private UserRepository users;

    @Test
    void createConfirmReadAndFilterBookings() {
        User owner = users.save(TestData.user("Owner"));
        User booker = users.save(TestData.user("Booker"));
        Item item = items.save(TestData.item(owner, null));
        BookingCreateDto input = input(item.getId(), LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(6));

        BookingDto created = bookings.create(input, booker.getId());
        assertNotNull(created.getId());
        assertEquals(Status.WAITING, created.getStatus());
        assertEquals(booker.getId(), created.getBooker().getId());
        assertEquals(item.getId(), created.getItem().getId());
        assertEquals(created.getId(), bookings.findById(created.getId(), owner.getId()).getId());
        assertEquals(1, bookings.findByUserId(booker.getId(), BookingState.WAITING).size());
        assertEquals(1, bookings.findByOwner(owner.getId(), BookingState.WAITING).size());

        BookingDto approved = bookings.confirmation(created.getId(), owner.getId(), true);
        assertEquals(Status.APPROVED, approved.getStatus());
        assertTrue(bookings.findByUserId(booker.getId(), BookingState.WAITING).isEmpty());
        assertEquals(1, bookings.findByUserId(booker.getId(), BookingState.FUTURE).size());
        assertEquals(1, bookings.findByOwner(owner.getId(), BookingState.ALL).size());
        assertThrows(ValidationException.class,
                () -> bookings.confirmation(created.getId(), owner.getId(), false));
    }

    @Test
    void unauthorizedUserCannotConfirmOrViewBooking() {
        User owner = users.save(TestData.user("Owner"));
        User booker = users.save(TestData.user("Booker"));
        User stranger = users.save(TestData.user("Stranger"));
        Item item = items.save(TestData.item(owner, null));
        BookingDto created = bookings.create(input(item.getId(), LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4)), booker.getId());

        assertThrows(ForbiddenException.class,
                () -> bookings.confirmation(created.getId(), stranger.getId(), true));
        assertThrows(NotFoundException.class,
                () -> bookings.findById(created.getId(), stranger.getId()));
    }

    @Test
    void ownerCannotBookOwnItemAndUnavailableItemsAreRejected() {
        User owner = users.save(TestData.user("Owner"));
        User stranger = users.save(TestData.user("Stranger"));
        Item item = items.save(TestData.item(owner, null));
        BookingCreateDto request = input(item.getId(), LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(3));
        assertThrows(ValidationException.class, () -> bookings.create(request, owner.getId()));
        item.setAvailable(false);
        assertThrows(ValidationException.class, () -> bookings.create(request, stranger.getId()));
    }

    @Test
    void invalidDatesAreRejected() {
        User owner = users.save(TestData.user("Owner"));
        User booker = users.save(TestData.user("Booker"));
        Item item = items.save(TestData.item(owner, null));
        BookingCreateDto request = input(item.getId(), LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1));
        assertThrows(ValidationException.class, () -> bookings.create(request, booker.getId()));
    }

    private BookingCreateDto input(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);
        return dto;
    }
}
