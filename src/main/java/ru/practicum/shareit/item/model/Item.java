package ru.practicum.shareit.item.model;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.user.model.User;

/**
 * TODO Sprint add-controllers.
 */
@Getter
@Setter
public class Item {
    private Long id;
    private String name;
    private String description;
    private User owner;
    private boolean available;
}
