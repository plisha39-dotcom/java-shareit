package ru.practicum.shareit.item.dto;

import lombok.Data;

import java.util.List;

@Data
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private ItemBookingDto lastBooking;
    private ItemBookingDto nextBooking;
    private List<CommentDto> comments;
    private Long requestId;
}
