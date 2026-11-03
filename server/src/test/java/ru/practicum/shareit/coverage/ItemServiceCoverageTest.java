package ru.practicum.shareit.coverage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceCoverageTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository requestRepository;
    @InjectMocks
    private ItemServiceImpl service;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void init() {
        owner = user(7L, "Owner");
        booker = user(8L, "Booker");
        item = new Item();
        item.setId(9L);
        item.setName("Drill");
        item.setDescription("Working drill");
        item.setOwner(owner);
        item.setAvailable(true);
    }

    @Test
    void createRejectsNullAndBlankNameDescriptionAndAvailability() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(owner));
        ItemDto input = dto("Drill", "A drill", true);

        input.setName(null);
        assertThrows(ValidationException.class, () -> service.create(input, 7L));
        input.setName("  ");
        assertThrows(ValidationException.class, () -> service.create(input, 7L));
        input.setName("Drill");

        input.setDescription(null);
        assertThrows(ValidationException.class, () -> service.create(input, 7L));
        input.setDescription(" ");
        assertThrows(ValidationException.class, () -> service.create(input, 7L));
        input.setDescription("A drill");

        input.setAvailable(null);
        assertThrows(ValidationException.class, () -> service.create(input, 7L));
    }

    @Test
    void updateRejectsBlankNameAndDescription() {
        when(itemRepository.findById(9L)).thenReturn(Optional.of(item));
        ItemDto changes = new ItemDto();
        changes.setName(" ");
        assertThrows(ValidationException.class, () -> service.update(changes, 9L, 7L));
        changes.setName(null);
        changes.setDescription("  ");
        assertThrows(ValidationException.class, () -> service.update(changes, 9L, 7L));
    }

    @Test
    void updateDescriptionAndAvailabilityWithoutChangingName() {
        when(itemRepository.findById(9L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ItemDto changes = new ItemDto();
        changes.setDescription("Updated description");
        changes.setAvailable(false);

        ItemDto result = service.update(changes, 9L, 7L);

        assertEquals("Drill", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void missingItemCannotBeReadOrUpdated() {
        assertThrows(NotFoundException.class, () -> service.findById(-1L));
        assertThrows(NotFoundException.class, () -> service.update(new ItemDto(), -1L, 7L));
    }

    @Test
    void ownerListingHandlesNoItemsAndMissingUser() {
        assertThrows(NotFoundException.class, () -> service.findAll(-1L));
        when(userRepository.findById(7L)).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner_Id(7L)).thenReturn(List.of());
        assertTrue(service.findAll(7L).isEmpty());
    }

    @Test
    void ownerListingIncludesPastFutureBookingsAndComments() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner_Id(7L)).thenReturn(List.of(item));
        Comment comment = comment("Great", booker, item);
        when(commentRepository.findByItem_IdIn(List.of(9L))).thenReturn(List.of(comment));
        Booking past = booking(10L, booker, item, LocalDateTime.now().minusDays(3));
        Booking future = booking(11L, booker, item, LocalDateTime.now().plusDays(3));
        when(bookingRepository.findByLastBooking(List.of(9L))).thenReturn(List.of(past));
        when(bookingRepository.findByNextBooking(List.of(9L))).thenReturn(List.of(future));

        ItemDto result = service.findAll(7L).getFirst();

        assertEquals(10L, result.getLastBooking().getId());
        assertEquals(8L, result.getLastBooking().getBookerId());
        assertEquals(11L, result.getNextBooking().getId());
        assertEquals(1, result.getComments().size());
        assertEquals("Great", result.getComments().getFirst().getText());
    }

    @Test
    void ownerListingWithoutBookingsOrCommentsHasEmptyFields() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner_Id(7L)).thenReturn(List.of(item));
        when(commentRepository.findByItem_IdIn(List.of(9L))).thenReturn(List.of());
        when(bookingRepository.findByLastBooking(List.of(9L))).thenReturn(List.of());
        when(bookingRepository.findByNextBooking(List.of(9L))).thenReturn(List.of());

        ItemDto result = service.findAll(7L).getFirst();

        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    void findByIdIncludesMappedComments() {
        when(itemRepository.findById(9L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItem_Id(9L)).thenReturn(List.of(comment("Great", booker, item)));
        ItemDto result = service.findById(9L);
        assertEquals(1, result.getComments().size());
        assertEquals("Booker", result.getComments().getFirst().getAuthorName());
    }

    @Test
    void nullSearchTextReturnsEmptyListWithoutRepositoryCall() {
        assertTrue(service.search(null).isEmpty());
        assertTrue(service.search("  ").isEmpty());
    }

    private static User user(long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        return user;
    }

    private static ItemDto dto(String name, String description, Boolean available) {
        ItemDto input = new ItemDto();
        input.setName(name);
        input.setDescription(description);
        input.setAvailable(available);
        return input;
    }

    private static Comment comment(String text, User author, Item item) {
        Comment comment = new Comment();
        comment.setId(17L);
        comment.setText(text);
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    private static Booking booking(Long id, User booker, Item item, LocalDateTime start) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(start);
        booking.setEnd(start.plusHours(2));
        booking.setStatus(Status.APPROVED);
        return booking;
    }
}
