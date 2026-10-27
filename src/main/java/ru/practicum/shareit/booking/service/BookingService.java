package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingCreateDto bookingCreateDto, Long userId);

    BookingDto confirmation(Long bookingId, Long userId, boolean approved);

    BookingDto findById(Long bookingId, Long userId);

    List<BookingDto> findByUserId(Long userId, BookingState state);

    List<BookingDto> findByOwner(Long userId, BookingState state);
}
