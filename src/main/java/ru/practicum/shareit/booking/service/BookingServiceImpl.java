package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, UserRepository userRepository, ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public BookingDto create(BookingCreateDto bookingCreateDto, Long userId) {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));
        Item item = itemRepository.findById(bookingCreateDto.getItemId())
                                  .orElseThrow(() -> new NotFoundException("Вещь с ID: " + bookingCreateDto.getItemId() + " не найдена"));
        if (!item.isAvailable()) {
            throw new ValidationException("Вещь с ID:" + bookingCreateDto.getItemId() + " не доступна для бронирования");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = bookingCreateDto.getStart();
        LocalDateTime end = bookingCreateDto.getEnd();
        if (!start.isAfter(now) || !end.isAfter(start)) {
            throw new ValidationException("Некорректное время начала или окончания бронирования");
        }
        if (userId.equals(item.getOwner().getId())) {
            throw new ValidationException("Пользователь не может бронировать собственную вещь");
        }
        Booking booking = BookingMapper.toBooking(bookingCreateDto, user, item, Status.WAITING);
        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toDto(savedBooking);
    }

    @Override
    public BookingDto confirmation(Long bookingId, Long userId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                                           .orElseThrow(() -> new NotFoundException("Бронь с ID: " + bookingId + " не найдена"));
        Item item = booking.getItem();
        User owner = item.getOwner();
        Long ownerId = owner.getId();
        if (!userId.equals(ownerId)) {
            throw new ForbiddenException("Подтверждать бронирование может только владелец вещи");
        }
        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Неверный статус бронирования");
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else  {
            booking.setStatus(Status.REJECTED);
        }
        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toDto(savedBooking);
    }

    @Override
    public BookingDto findById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                                           .orElseThrow(() -> new NotFoundException("Бронь с ID: " + bookingId + " не найдена"));
        Long bookerId = booking.getBooker().getId();
        Item item = booking.getItem();
        User owner = item.getOwner();
        Long ownerId = owner.getId();
        if (!userId.equals(ownerId) && !userId.equals(bookerId)) {
            throw new NotFoundException("Пользователь не может просматривать бронирование");
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> findByUserId(Long userId, BookingState state) {
        if (!userRepository.existsById(userId)) {
            throw  new NotFoundException("Пользователь с ID: " + userId + " не найден");
        }
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = List.of();
        switch (state) {
            case ALL -> bookings = bookingRepository.findByBooker_IdOrderByStartDesc(userId);
            case WAITING -> bookings = bookingRepository.findByBooker_IdAndStatusOrderByStartDesc(userId, Status.WAITING);
            case REJECTED -> bookings = bookingRepository.findByBooker_IdAndStatusOrderByStartDesc(userId, Status.REJECTED);
            case PAST -> bookings = bookingRepository.findByBooker_IdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookings = bookingRepository.findByBooker_IdAndStartAfterOrderByStartDesc(userId, now);
            case CURRENT -> bookings = bookingRepository
                    .findByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(userId, now, now);
        }
        return bookings.stream()
                .map(BookingMapper::toDto)
                .toList();
    }

    @Override
    public List<BookingDto> findByOwner(Long userId, BookingState state) {
        if (!userRepository.existsById(userId)) {
            throw  new NotFoundException("Пользователь с ID: " + userId + " не найден");
        }
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = List.of();
        switch (state) {
            case ALL -> bookings = bookingRepository.findByItem_Owner_IdOrderByStartDesc(userId);
            case WAITING -> bookings = bookingRepository.findByItem_Owner_IdAndStatusOrderByStartDesc(userId, Status.WAITING);
            case REJECTED -> bookings = bookingRepository.findByItem_Owner_IdAndStatusOrderByStartDesc(userId, Status.REJECTED);
            case PAST -> bookings = bookingRepository.findByItem_Owner_IdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookings = bookingRepository.findByItem_Owner_IdAndStartAfterOrderByStartDesc(userId, now);
            case CURRENT -> bookings = bookingRepository.findByItem_Owner_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(userId, now, now);
        }
        return  bookings.stream()
                .map(BookingMapper::toDto)
                .toList();
    }
}