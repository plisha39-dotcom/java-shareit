package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
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
class ItemServiceIntegrationTest {
    @Autowired
    private ItemService items;
    @Autowired
    private ItemRequestService requests;
    @Autowired
    private UserRepository users;

    @Test
    void createItemWithoutRequestAndFindIt() {
        User owner = users.save(TestData.user("Owner"));
        ItemDto created = items.create(item("Electric drill", true), owner.getId());
        assertNotNull(created.getId());
        assertEquals("Electric drill", items.findById(created.getId()).getName());
        assertEquals(1, items.findAll(owner.getId()).size());
        assertEquals(null, created.getRequestId());
    }

    @Test
    void createItemInResponseToRequestAndRetrieveItFromRequest() {
        User requester = users.save(TestData.user("Requester"));
        User owner = users.save(TestData.user("Owner"));
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        requestDto.setDescription("Need a camera");
        ItemRequestDto request = requests.create(requestDto, requester.getId());

        ItemDto input = item("Camera", true);
        input.setRequestId(request.getId());
        ItemDto created = items.create(input, owner.getId());
        assertEquals(request.getId(), created.getRequestId());
        assertEquals(created.getId(), requests.findByRequestId(request.getId()).getItems().getFirst().getId());
    }

    @Test
    void updateEnforcesOwnershipAndSupportsPartialUpdates() {
        User owner = users.save(TestData.user("Owner"));
        User stranger = users.save(TestData.user("Stranger"));
        ItemDto created = items.create(item("Electric drill", true), owner.getId());

        ItemDto patch = new ItemDto();
        patch.setName("New drill");
        assertThrows(ForbiddenException.class,
                () -> items.update(patch, created.getId(), stranger.getId()));
        ItemDto updated = items.update(patch, created.getId(), owner.getId());
        assertEquals("New drill", updated.getName());
        assertEquals("Description of Electric drill", updated.getDescription());
    }

    @Test
    void searchIsCaseInsensitiveAndExcludesUnavailableItems() {
        User owner = users.save(TestData.user("Owner"));
        items.create(item("DRILL PRO", true), owner.getId());
        items.create(item("drill broken", false), owner.getId());
        items.create(item("Bike", true), owner.getId());

        List<ItemDto> found = items.search("dRiLl");
        assertEquals(1, found.size());
        assertEquals("DRILL PRO", found.getFirst().getName());
        assertTrue(items.search("   ").isEmpty());
    }

    @Test
    void missingOwnerAndRequestAreRejected() {
        assertThrows(NotFoundException.class, () -> items.create(item("Lamp", true), -999L));
        User owner = users.save(TestData.user("Owner"));
        ItemDto input = item("Lamp", true);
        input.setRequestId(-999L);
        assertThrows(NotFoundException.class, () -> items.create(input, owner.getId()));
    }

    private ItemDto item(String name, boolean available) {
        ItemDto dto = new ItemDto();
        dto.setName(name);
        dto.setDescription("Description of " + name);
        dto.setAvailable(available);
        return dto;
    }
}
