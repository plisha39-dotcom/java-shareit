package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDto {
    @Email
    @Pattern(regexp = ".*\\S.*")
    @Size(max = 512)
    private String email;
    @Pattern(regexp = ".*\\S.*")
    @Size(max = 255)
    private String name;
}
