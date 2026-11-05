package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemCreateDto {
    @NotBlank
    @Size(max = 255)
    private String name;
    @NotBlank
    @Size(max = 512)
    private String description;
    @NotNull
    private Boolean available;
    private Long requestId;
}
