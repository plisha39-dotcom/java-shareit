package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final RequestRepository requestRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           BookingRepository bookingRepository, CommentRepository commentRepository,
                           RequestRepository requestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.requestRepository = requestRepository;
    }

    @Transactional
    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Имя не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус не может быть пустым");
        }
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        Long requestId = itemDto.getRequestId();
        if (requestId != null) {
            ItemRequest request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException("Запрос " + requestId + " не найден"));
            item.setRequest(request);
        }
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toDto(savedItem);
    }

    @Transactional
    @Override
    public ItemDto update(ItemDto itemDto, Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                                  .orElseThrow(() -> new NotFoundException("Вещь с ID: " + itemId + " не найдена"));
        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("У предмета не верный владелец");
        }
        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                throw new ValidationException("Имя не может быть пустым");
            }
        }
        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                throw new ValidationException("Описание не может быть пустым");
            }
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toDto(savedItem);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDto findById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                                  .orElseThrow(() -> new NotFoundException("Вещь с ID: " + itemId + " не найдена"));
        ItemDto itemDto = ItemMapper.toDto(item);
        List<CommentDto> commentDtos = findCommentDtosByItemId(itemId);
        itemDto.setComments(commentDtos);
        return itemDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> findAll(Long userId) {
        userRepository.findById(userId)
                      .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));
        List<Item> items = itemRepository.findByOwner_Id(userId);
        if (items.isEmpty()) {
            return List.of();
        }
        List<Long> itemIds = items.stream()
                                  .map(Item::getId)
                                  .toList();
        List<Comment> comments = commentRepository.findByItem_IdIn(itemIds);
        Map<Long, List<Comment>> commentsByItemId = comments.stream()
                                                            .collect(groupingBy(comment -> comment.getItem().getId()));
        Map<Long, Booking> lastBookingsByItemId = bookingRepository.findByLastBooking(itemIds).stream()
                                                                   .collect(Collectors.toMap(booking -> booking.getItem().getId(), Function.identity()));
        Map<Long, Booking> nextBookingsByItemId = bookingRepository.findByNextBooking(itemIds).stream()
                                                                   .collect(Collectors.toMap(booking -> booking.getItem().getId(), Function.identity()));
        return items.stream()
                    .map(item -> {
                        ItemDto itemDto = ItemMapper.toDto(item);
                        Long itemId = item.getId();
                        Booking lastBooking = lastBookingsByItemId.get(itemId);
                        ItemBookingDto lastBookingDto = Optional.ofNullable(lastBooking)
                                                                .map(ItemMapper::toItemBookingDto)
                                                                .orElse(null);
                        itemDto.setLastBooking(lastBookingDto);
                        Booking nextBooking = nextBookingsByItemId.get(itemId);
                        ItemBookingDto nextBookingDto = Optional.ofNullable(nextBooking)
                                                                .map(ItemMapper::toItemBookingDto)
                                                                .orElse(null);
                        itemDto.setNextBooking(nextBookingDto);
                        List<Comment> itemComments = commentsByItemId.getOrDefault(itemId, List.of());
                        List<CommentDto> commentDtos = itemComments.stream()
                                                                   .map(CommentMapper::toDto)
                                                                   .toList();
                        itemDto.setComments(commentDtos);
                        return itemDto;
                    })
                    .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String normalizedText = text.trim();
        return itemRepository.searchByText(normalizedText).stream()
                             .map(ItemMapper::toDto)
                             .toList();
    }

    private List<CommentDto> findCommentDtosByItemId(Long itemId) {
        List<Comment> comments = commentRepository.findByItem_Id(itemId);
        List<CommentDto> commentDtos = comments.stream()
                                               .map(CommentMapper::toDto)
                                               .toList();
        return commentDtos;
    }
}
