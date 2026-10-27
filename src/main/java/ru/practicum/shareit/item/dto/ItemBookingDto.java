package ru.practicum.shareit.item.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemBookingDto {
    private Long id;
    private Long bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
}
