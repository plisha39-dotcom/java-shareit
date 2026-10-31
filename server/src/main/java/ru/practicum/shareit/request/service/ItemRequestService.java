package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(ItemRequestCreateDto itemRequestCreateDto, Long userId);

    List<ItemRequestDto> findAll(Long userId);

    List<ItemRequestDto> findAllByOthers(Long otherUserId);

    ItemRequestDto findByRequestId(Long requestId);
}
