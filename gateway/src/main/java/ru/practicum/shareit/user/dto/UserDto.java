package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    @Email
    @NotBlank
    @Size(max = 512)
    private String email;
    @NotBlank
    @Size(max = 255)
    private String name;
}
