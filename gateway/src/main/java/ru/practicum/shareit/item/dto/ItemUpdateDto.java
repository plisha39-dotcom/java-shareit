package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ItemUpdateDto {
    @Pattern(regexp = ".*\\S.*")
    private String name;
    @Pattern(regexp = ".*\\S.*")
    private String description;
    private Boolean available;
}
