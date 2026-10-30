package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.Status;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private BookingBookerDto booker;
    private BookingItemDto item;
    private Long id;
    private Status status;
    private LocalDateTime start;
    private LocalDateTime end;
}
