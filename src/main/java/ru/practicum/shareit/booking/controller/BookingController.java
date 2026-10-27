package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @Valid @RequestBody BookingCreateDto bookingCreateDto) {
        return bookingService.create(bookingCreateDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto confirmation(@PathVariable Long bookingId,
                                   @RequestHeader("X-Sharer-User-Id") Long userId,
                                   @RequestParam boolean approved) {
        return bookingService.confirmation(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findById(@PathVariable Long bookingId,
                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.findById(bookingId, userId);
    }

    @GetMapping()
    public List<BookingDto> findByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.findByUserId(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> findByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.findByOwner(userId, state);
    }
}
