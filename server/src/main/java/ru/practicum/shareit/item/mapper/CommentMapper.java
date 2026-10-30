package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class CommentMapper {
    public static CommentDto toDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setAuthorName(comment.getAuthor().getName());
        commentDto.setText(comment.getText());
        commentDto.setCreated(comment.getCreated());
        return commentDto;
    }

    public static Comment toComment(CommentCreateDto commentCreateDto, User user, Item item) {
        Comment comment = new Comment();
        comment.setText(commentCreateDto.getText());
        comment.setAuthor(user);
        comment.setItem(item);
        return comment;
    }
}
