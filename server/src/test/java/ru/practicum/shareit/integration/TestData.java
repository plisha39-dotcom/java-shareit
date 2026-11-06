package ru.practicum.shareit.integration;

import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

final class TestData {
    private TestData() {
    }

    static User user(String name) {
        User user = new User();
        user.setName(name);
        user.setEmail(UUID.randomUUID() + "@example.com");
        return user;
    }

    static Item item(User owner, ItemRequest request) {
        Item item = new Item();
        item.setName("Electric drill");
        item.setDescription("Drill with case");
        item.setOwner(owner);
        item.setAvailable(true);
        item.setRequest(request);
        return item;
    }

    static Booking booking(User booker, Item item, Status status, LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(status);
        booking.setStart(start);
        booking.setEnd(end);
        return booking;
    }
}
