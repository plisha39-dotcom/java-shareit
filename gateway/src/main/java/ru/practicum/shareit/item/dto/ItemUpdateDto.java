package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemUpdateDto {
    @Pattern(regexp = ".*\\S.*")
    @Size(max = 255)
    private String name;
    @Pattern(regexp = ".*\\S.*")
    @Size(max = 512)
    private String description;
    private Boolean available;
}
