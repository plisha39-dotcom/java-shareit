package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.ArrayList;
import java.util.List;

public class ItemRequestMapper {
    public static ItemRequestDto toDto(ItemRequest itemRequest, List<Item> items) {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        List<ItemRequestResponseDto> itemRequestResponseDtoList = new ArrayList<>();
        itemRequestDto.setId(itemRequest.getId());
        itemRequestDto.setDescription(itemRequest.getDescription());
        itemRequestDto.setCreated(itemRequest.getCreated());
        for (Item item : items) {
            ItemRequestResponseDto itemRequestResponseDto = new ItemRequestResponseDto();
            itemRequestResponseDto.setId(item.getId());
            itemRequestResponseDto.setName(item.getName());
            itemRequestResponseDto.setOwnerId(item.getOwner().getId());
            itemRequestResponseDtoList.add(itemRequestResponseDto);
        }
        itemRequestDto.setItems(itemRequestResponseDtoList);
        return itemRequestDto;
    }

    public static ItemRequest toRequest(ItemRequestCreateDto itemRequestCreateDto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestCreateDto.getDescription());
        return itemRequest;
    }
}
