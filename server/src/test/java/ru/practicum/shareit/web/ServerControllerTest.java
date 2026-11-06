package ru.practicum.shareit.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.controller.CommentController;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class, ItemController.class, CommentController.class,
        BookingController.class, ItemRequestController.class})
class ServerControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @MockBean
    private UserService users;
    @MockBean
    private ItemService items;
    @MockBean
    private CommentService comments;
    @MockBean
    private BookingService bookings;
    @MockBean
    private ItemRequestService requests;

    @Test
    void usersList() throws Exception {
        given(users.findAll()).willReturn(List.of(new UserDto()));
        mvc.perform(get("/users")).andExpect(status().isOk()).andExpect(jsonPath("$[0]").exists());
        verify(users).findAll();
    }

    @Test
    void userById() throws Exception {
        UserDto dto = new UserDto();
        dto.setId(12L);
        given(users.findById(12L)).willReturn(dto);
        mvc.perform(get("/users/12")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(12));
        verify(users).findById(12L);
    }

    @Test
    void userCreate() throws Exception {
        UserDto dto = user("New user", "new@example.com");
        given(users.create(any(UserDto.class))).willReturn(dto);
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(dto)))
           .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("new@example.com"));
        verify(users).create(any(UserDto.class));
    }

    @Test
    void userUpdate() throws Exception {
        UserDto dto = user("Updated", "updated@example.com");
        given(users.update(any(UserDto.class), eq(12L))).willReturn(dto);
        mvc.perform(patch("/users/12").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(dto)))
           .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated"));
        verify(users).update(any(UserDto.class), eq(12L));
    }

    @Test
    void userDelete() throws Exception {
        mvc.perform(delete("/users/12")).andExpect(status().isOk());
        verify(users).delete(12L);
    }

    @Test
    void itemCreate() throws Exception {
        ItemDto dto = item();
        given(items.create(any(ItemDto.class), eq(5L))).willReturn(dto);
        mvc.perform(post("/items").header("X-Sharer-User-Id", 5)
                                  .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(dto)))
           .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Drill"));
        verify(items).create(any(ItemDto.class), eq(5L));
    }

    @Test
    void itemUpdate() throws Exception {
        ItemDto dto = item();
        given(items.update(any(ItemDto.class), eq(9L), eq(5L))).willReturn(dto);
        mvc.perform(patch("/items/9").header("X-Sharer-User-Id", 5)
                                     .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(dto)))
           .andExpect(status().isOk());
        verify(items).update(any(ItemDto.class), eq(9L), eq(5L));
    }

    @Test
    void itemById() throws Exception {
        given(items.findById(9L)).willReturn(item());
        mvc.perform(get("/items/9")).andExpect(status().isOk()).andExpect(jsonPath("$.available").value(true));
        verify(items).findById(9L);
    }

    @Test
    void itemsByOwner() throws Exception {
        given(items.findAll(5L)).willReturn(List.of(item()));
        mvc.perform(get("/items").header("X-Sharer-User-Id", 5))
           .andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Drill"));
        verify(items).findAll(5L);
    }

    @Test
    void itemsSearch() throws Exception {
        given(items.search("drill")).willReturn(List.of(item()));
        mvc.perform(get("/items/search").param("text", "drill"))
           .andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Drill"));
        verify(items).search("drill");
    }

    @Test
    void commentCreate() throws Exception {
        CommentDto dto = new CommentDto();
        dto.setText("Nice");
        given(comments.create(any(CommentCreateDto.class), eq(5L), eq(9L))).willReturn(dto);
        mvc.perform(post("/items/9/comment").header("X-Sharer-User-Id", 5)
                                            .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"Nice\"}"))
           .andExpect(status().isOk()).andExpect(jsonPath("$.text").value("Nice"));
        verify(comments).create(any(CommentCreateDto.class), eq(5L), eq(9L));
    }

    @Test
    void bookingCreate() throws Exception {
        BookingDto dto = new BookingDto();
        dto.setId(8L);
        given(bookings.create(any(BookingCreateDto.class), eq(5L))).willReturn(dto);
        mvc.perform(post("/bookings").header("X-Sharer-User-Id", 5).contentType(MediaType.APPLICATION_JSON)
                                     .content("{\"itemId\":9,\"start\":\"2030-01-01T12:00:00\",\"end\":\"2030-01-02T12:00:00\"}"))
           .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(8));
        verify(bookings).create(any(BookingCreateDto.class), eq(5L));
    }

    @Test
    void bookingConfirm() throws Exception {
        BookingDto dto = new BookingDto();
        dto.setId(8L);
        given(bookings.confirmation(8L, 5L, true)).willReturn(dto);
        mvc.perform(patch("/bookings/8").header("X-Sharer-User-Id", 5).param("approved", "true"))
           .andExpect(status().isOk());
        verify(bookings).confirmation(8L, 5L, true);
    }

    @Test
    void bookingById() throws Exception {
        given(bookings.findById(8L, 5L)).willReturn(new BookingDto());
        mvc.perform(get("/bookings/8").header("X-Sharer-User-Id", 5)).andExpect(status().isOk());
        verify(bookings).findById(8L, 5L);
    }

    @Test
    void bookingsByBooker() throws Exception {
        given(bookings.findByUserId(5L, BookingState.WAITING)).willReturn(List.of());
        mvc.perform(get("/bookings").header("X-Sharer-User-Id", 5).param("state", "WAITING"))
           .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
        verify(bookings).findByUserId(5L, BookingState.WAITING);
    }

    @Test
    void bookingsByOwner() throws Exception {
        given(bookings.findByOwner(5L, BookingState.ALL)).willReturn(List.of());
        mvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", 5))
           .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
        verify(bookings).findByOwner(5L, BookingState.ALL);
    }

    @Test
    void requestCreate() throws Exception {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(3L);
        given(requests.create(any(ItemRequestCreateDto.class), eq(5L))).willReturn(dto);
        mvc.perform(post("/requests").header("X-Sharer-User-Id", 5)
                                     .contentType(MediaType.APPLICATION_JSON).content("{\"description\":\"Need a drill\"}"))
           .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(3));
        verify(requests).create(any(ItemRequestCreateDto.class), eq(5L));
    }

    @Test
    void requestsByRequester() throws Exception {
        given(requests.findAll(5L)).willReturn(List.of());
        mvc.perform(get("/requests").header("X-Sharer-User-Id", 5))
           .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
        verify(requests).findAll(5L);
    }

    @Test
    void requestsByOthers() throws Exception {
        given(requests.findAllByOthers(5L)).willReturn(List.of());
        mvc.perform(get("/requests/all").header("X-Sharer-User-Id", 5))
           .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
        verify(requests).findAllByOthers(5L);
    }

    @Test
    void requestById() throws Exception {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(3L);
        given(requests.findByRequestId(3L)).willReturn(dto);
        mvc.perform(get("/requests/3")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(3));
        verify(requests).findByRequestId(3L);
    }

    @Test
    void missingOwnerHeaderIsBadRequest() throws Exception {
        mvc.perform(get("/items")).andExpect(status().isBadRequest());
    }

    @Test
    void missingBookingItemIdIsBadRequest() throws Exception {
        mvc.perform(post("/bookings").header("X-Sharer-User-Id", 5).contentType(MediaType.APPLICATION_JSON)
                                     .content("{\"start\":\"2030-01-01T12:00:00\",\"end\":\"2030-01-02T12:00:00\"}"))
           .andExpect(status().isBadRequest());
    }

    private UserDto user(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    private ItemDto item() {
        ItemDto dto = new ItemDto();
        dto.setName("Drill");
        dto.setDescription("Electric drill");
        dto.setAvailable(true);
        return dto;
    }
}
