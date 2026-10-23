package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public CommentServiceImpl(CommentRepository commentRepository, ItemRepository itemRepository,
                              UserRepository userRepository, BookingRepository bookingRepository) {
        this.commentRepository = commentRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public CommentDto create(CommentCreateDto commentCreateDto, Long userId, Long itemId) {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));
        Item item = itemRepository.findById(itemId)
                                  .orElseThrow(() -> new NotFoundException("Вещь с ID: " + itemId + " не найдена"));
        LocalDateTime now = LocalDateTime.now();
        boolean validComment = bookingRepository.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
                itemId, userId, Status.APPROVED, now);
        if (!validComment) {
            throw new ValidationException("Пользователь " + userId + " не может оставить комментарий");
        }
        Comment comment = CommentMapper.toComment(commentCreateDto, user, item);
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toDto(savedComment);
    }
}
