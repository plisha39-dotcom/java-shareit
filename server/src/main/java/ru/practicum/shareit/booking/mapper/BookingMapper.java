package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingBookerDto;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {
    public static BookingDto toDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        BookingBookerDto bookerDto = new BookingBookerDto();
        BookingItemDto bookingItemDto = new BookingItemDto();
        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setStatus(booking.getStatus());
        bookerDto.setId(booking.getBooker().getId());
        bookingItemDto.setId(booking.getItem().getId());
        bookingItemDto.setName(booking.getItem().getName());
        bookingDto.setBooker(bookerDto);
        bookingDto.setItem(bookingItemDto);
        return bookingDto;
    }

    public static Booking toBooking(BookingCreateDto bookingCreateDto, User user, Item item, Status status) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStart(bookingCreateDto.getStart());
        booking.setEnd(bookingCreateDto.getEnd());
        booking.setStatus(status);
        return booking;
    }
}
