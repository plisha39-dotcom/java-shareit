package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.service.CommentService;

@RestController
@RequestMapping("/items")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto create(@PathVariable Long itemId,
                             @RequestHeader("X-Sharer-User-Id") Long userId,
                             @Valid @RequestBody CommentCreateDto commentCreateDto) {
        return commentService.create(commentCreateDto, userId, itemId);
    }
}
