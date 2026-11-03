package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateDto {
    @Email
    @Pattern(regexp = ".*\\S.*")
    private String email;
    @Pattern(regexp = ".*\\S.*")
    private String name;
}
