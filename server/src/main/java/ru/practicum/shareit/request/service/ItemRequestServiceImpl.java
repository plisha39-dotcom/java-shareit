package ru.practicum.shareit.request.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository, UserRepository userRepository,
                                  ItemRepository itemRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    @Override
    public ItemRequestDto create(ItemRequestCreateDto itemRequestCreateDto, Long userId) {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));
        ItemRequest itemRequest = ItemRequestMapper.toRequest(itemRequestCreateDto);
        itemRequest.setRequester(user);
        ItemRequest saveItemRequest = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toDto(saveItemRequest, List.of());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestDto> findAll(Long userId) {
        userRepository.findById(userId)
                      .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));
        List<ItemRequest> requests = itemRequestRepository.findByRequester_IdOrderByCreatedDesc(userId);
        if (requests.isEmpty()) {
            return List.of();
        }
        return buildRequestDtos(requests);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestDto> findAllByOthers(Long requesterId) {
        userRepository.findById(requesterId)
                      .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + requesterId + " не найден"));
        List<ItemRequest> requests = itemRequestRepository.findByRequester_IdNotOrderByCreatedDesc(requesterId);
        if (requests.isEmpty()) {
            return List.of();
        }
        return buildRequestDtos(requests);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemRequestDto findByRequestId(Long requestId) {
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос " + requestId + " не найден"));
        List<Item> items = itemRepository.findByRequest_Id(request.getId());
        return ItemRequestMapper.toDto(request, items);
    }

    private List<ItemRequestDto> buildRequestDtos(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                                        .map(ItemRequest::getId)
                                        .toList();
        List<Item> items = itemRepository.findByRequest_idIn(requestIds);
        Map<Long, List<Item>> itemsByRequestId = items.stream()
                                                      .collect(groupingBy(item -> item.getRequest().getId()));
        return requests.stream()
                       .map(request -> {
                           Long requestId = request.getId();
                           List<Item> itemsByRequest = itemsByRequestId.getOrDefault(requestId, List.of());
                           return ItemRequestMapper.toDto(request, itemsByRequest);
                       })
                       .toList();
    }
}
