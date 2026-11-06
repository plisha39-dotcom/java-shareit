package ru.practicum.shareit.coverage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceCoverageTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private BookingServiceImpl service;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void init() {
        owner = user(1L);
        booker = user(2L);
        item = new Item();
        item.setId(3L);
        item.setName("Drill");
        item.setOwner(owner);
        item.setAvailable(true);
        booking = new Booking();
        booking.setId(4L);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().plusDays(2));
        booking.setEnd(LocalDateTime.now().plusDays(3));
        booking.setStatus(Status.WAITING);
    }

    @Test
    void bookerFilteringSelectsCorrectRepositoryQueryForEachState() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findByBooker_IdOrderByStartDesc(2L)).thenReturn(List.of(booking));
        when(bookingRepository.findByBooker_IdAndStatusOrderByStartDesc(2L, Status.WAITING))
                .thenReturn(List.of(booking));
        when(bookingRepository.findByBooker_IdAndStatusOrderByStartDesc(2L, Status.REJECTED))
                .thenReturn(List.of(booking));
        when(bookingRepository.findByBooker_IdAndEndBeforeOrderByStartDesc(eq(2L), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findByBooker_IdAndStartAfterOrderByStartDesc(eq(2L), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                eq(2L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        for (BookingState state : BookingState.values()) {
            List<BookingDto> result = service.findByUserId(2L, state);
            assertEquals(1, result.size());
            assertEquals(4L, result.getFirst().getId());
        }
        verify(bookingRepository).findByBooker_IdOrderByStartDesc(2L);
        verify(bookingRepository).findByBooker_IdAndStatusOrderByStartDesc(2L, Status.REJECTED);
    }

    @Test
    void ownerFilteringSelectsCorrectRepositoryQueryForEachState() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByItem_Owner_IdOrderByStartDesc(1L)).thenReturn(List.of(booking));
        when(bookingRepository.findByItem_Owner_IdAndStatusOrderByStartDesc(1L, Status.WAITING))
                .thenReturn(List.of(booking));
        when(bookingRepository.findByItem_Owner_IdAndStatusOrderByStartDesc(1L, Status.REJECTED))
                .thenReturn(List.of(booking));
        when(bookingRepository.findByItem_Owner_IdAndEndBeforeOrderByStartDesc(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findByItem_Owner_IdAndStartAfterOrderByStartDesc(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findByItem_Owner_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        for (BookingState state : BookingState.values()) {
            List<BookingDto> result = service.findByOwner(1L, state);
            assertEquals(1, result.size());
            assertEquals(4L, result.getFirst().getId());
        }
        verify(bookingRepository).findByItem_Owner_IdOrderByStartDesc(1L);
        verify(bookingRepository).findByItem_Owner_IdAndStatusOrderByStartDesc(1L, Status.REJECTED);
    }

    @Test
    void filteringUnknownUserFailsForBothEndpoints() {
        assertThrows(NotFoundException.class, () -> service.findByUserId(-1L, BookingState.ALL));
        assertThrows(NotFoundException.class, () -> service.findByOwner(-1L, BookingState.ALL));
    }

    @Test
    void ownerCanRejectWaitingBooking() {
        when(bookingRepository.findById(4L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingDto result = service.confirmation(4L, 1L, false);

        assertEquals(Status.REJECTED, result.getStatus());
        assertEquals(Status.REJECTED, booking.getStatus());
    }

    @Test
    void missingBookingCannotBeReadOrConfirmed() {
        assertThrows(NotFoundException.class, () -> service.findById(-1L, 1L));
        assertThrows(NotFoundException.class, () -> service.confirmation(-1L, 1L, true));
    }

    @Test
    void bookerCanReadOwnBooking() {
        when(bookingRepository.findById(4L)).thenReturn(Optional.of(booking));
        assertEquals(4L, service.findById(4L, 2L).getId());
    }

    @Test
    void missingBookerOrItemPreventsCreation() {
        BookingCreateDto dto = request(3L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        assertThrows(NotFoundException.class, () -> service.create(dto, -1L));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        assertThrows(NotFoundException.class, () -> service.create(dto, 2L));
    }

    @Test
    void endMustBeLaterThanStart() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(3L)).thenReturn(Optional.of(item));
        LocalDateTime future = LocalDateTime.now().plusDays(4);
        BookingCreateDto dto = request(3L, future, future.minusMinutes(1));
        assertThrows(ValidationException.class, () -> service.create(dto, 2L));
    }

    private static User user(long id) {
        User result = new User();
        result.setId(id);
        result.setName("User " + id);
        return result;
    }

    private static BookingCreateDto request(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);
        return dto;
    }
}
