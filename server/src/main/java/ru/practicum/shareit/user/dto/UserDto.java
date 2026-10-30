package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    @Email
    private String email;
    private String name;
}
