package ru.practicum.shareit.coverage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceCoverageTest {
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private ItemRequestServiceImpl service;

    @Test
    void emptyOwnRequestsDoNotQueryItems() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(requestRepository.findByRequester_IdOrderByCreatedDesc(1L)).thenReturn(List.of());
        assertTrue(service.findAll(1L).isEmpty());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void emptyOtherRequestsDoNotQueryItems() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(requestRepository.findByRequester_IdNotOrderByCreatedDesc(1L)).thenReturn(List.of());
        assertTrue(service.findAllByOthers(1L).isEmpty());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void requestWithoutOfferAndRequestWithOfferMapIndependently() {
        User requester = user(1L);
        User owner = user(2L);
        ItemRequest first = request(10L, requester);
        ItemRequest second = request(11L, requester);
        Item item = new Item();
        item.setId(15L);
        item.setName("Camera");
        item.setOwner(owner);
        item.setRequest(first);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(requestRepository.findByRequester_IdOrderByCreatedDesc(1L)).thenReturn(List.of(first, second));
        when(itemRepository.findByRequest_idIn(List.of(10L, 11L))).thenReturn(List.of(item));

        List<ItemRequestDto> result = service.findAll(1L);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getItems().size());
        assertEquals(2L, result.get(0).getItems().getFirst().getOwnerId());
        assertTrue(result.get(1).getItems().isEmpty());
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        return user;
    }

    private static ItemRequest request(Long id, User requester) {
        ItemRequest request = new ItemRequest();
        request.setId(id);
        request.setRequester(requester);
        request.setDescription("Need something");
        request.setCreated(LocalDateTime.now());
        return request;
    }
}
