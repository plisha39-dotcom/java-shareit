package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestIntegrationTest {
    @Autowired
    private ItemRequestService requests;
    @Autowired
    private UserRepository users;
    @Autowired
    private ItemRepository items;

    @Test
    void createPersistsRequestWithTimestampAndNoAnswers() {
        User requester = users.save(TestData.user("Requester"));
        ItemRequestDto created = requests.create(description("Need a drill"), requester.getId());
        assertNotNull(created.getId());
        assertEquals("Need a drill", created.getDescription());
        assertNotNull(created.getCreated());
        assertTrue(created.getItems().isEmpty());
        assertEquals(created.getId(), requests.findByRequestId(created.getId()).getId());
    }

    @Test
    void ownRequestsIncludeItemAnswersAndExcludeOtherUsers() {
        User requester = users.save(TestData.user("Requester"));
        User other = users.save(TestData.user("Other"));
        ItemRequestDto created = requests.create(description("Need a drill"), requester.getId());
        requests.create(description("Need a saw"), other.getId());

        List<ItemRequestDto> own = requests.findAll(requester.getId());
        assertEquals(1, own.size());
        assertEquals(created.getId(), own.getFirst().getId());
        assertTrue(own.getFirst().getItems().isEmpty());
    }

    @Test
    void singleRequestAndListContainLinkedItemWithCorrectOwner() {
        User requester = users.save(TestData.user("Requester"));
        User offerer = users.save(TestData.user("Offerer"));
        ItemRequestDto created = requests.create(description("Need a drill"), requester.getId());
        ru.practicum.shareit.request.model.ItemRequest managed = requestEntity(created.getId());
        Item offer = items.save(TestData.item(offerer, managed));

        ItemRequestDto detail = requests.findByRequestId(created.getId());
        assertEquals(1, detail.getItems().size());
        assertEquals(offer.getId(), detail.getItems().getFirst().getId());
        assertEquals("Electric drill", detail.getItems().getFirst().getName());
        assertEquals(offerer.getId(), detail.getItems().getFirst().getOwnerId());
        assertEquals(offer.getId(), requests.findAll(requester.getId()).getFirst().getItems().getFirst().getId());
    }

    @Test
    void othersListExcludesRequestsOfTheCurrentUser() {
        User owner = users.save(TestData.user("Owner"));
        User visitor = users.save(TestData.user("Visitor"));
        ItemRequestDto first = requests.create(description("Need a camera"), owner.getId());
        requests.create(description("Need a bike"), visitor.getId());
        List<ItemRequestDto> result = requests.findAllByOthers(visitor.getId());
        assertEquals(1, result.size());
        assertEquals(first.getId(), result.getFirst().getId());
    }

    @Test
    void missingRequestAndRequesterFailClearly() {
        assertThrows(NotFoundException.class, () -> requests.findByRequestId(-999L));
        assertThrows(NotFoundException.class, () -> requests.create(description("Need a drill"), -999L));
        assertThrows(NotFoundException.class, () -> requests.findAll(-999L));
        assertThrows(NotFoundException.class, () -> requests.findAllByOthers(-999L));
    }

    private ItemRequestCreateDto description(String text) {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription(text);
        return dto;
    }

    @Autowired
    private ru.practicum.shareit.request.repository.ItemRequestRepository requestRepository;

    private ru.practicum.shareit.request.model.ItemRequest requestEntity(Long id) {
        return requestRepository.findById(id).orElseThrow();
    }
}
