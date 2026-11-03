package ru.practicum.shareit.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class, ItemController.class,
        ItemRequestController.class, BookingController.class})
class GatewayControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private UserClient users;
    @MockBean
    private ItemClient items;
    @MockBean
    private ItemRequestClient requests;
    @MockBean
    private BookingClient bookings;

    @Test
    void usersList() throws Exception {
        given(users.findAll()).willReturn(ok());
        mvc.perform(get("/users")).andExpect(status().isOk());
        verify(users).findAll();
    }

    @Test
    void userById() throws Exception {
        given(users.findById(12L)).willReturn(ok());
        mvc.perform(get("/users/12")).andExpect(status().isOk());
        verify(users).findById(12L);
    }

    @Test
    void userCreate() throws Exception {
        given(users.create(any(UserDto.class))).willReturn(ok());
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                                  .content("{\"name\":\"Alice\",\"email\":\"alice@example.com\"}"))
           .andExpect(status().isOk());
        verify(users).create(any(UserDto.class));
    }

    @Test
    void userUpdate() throws Exception {
        given(users.update(any(UserUpdateDto.class), eq(12L))).willReturn(ok());
        mvc.perform(patch("/users/12").contentType(MediaType.APPLICATION_JSON)
                                      .content("{\"name\":\"Alice Two\"}"))
           .andExpect(status().isOk());
        verify(users).update(any(UserUpdateDto.class), eq(12L));
    }

    @Test
    void userDelete() throws Exception {
        given(users.delete(12L)).willReturn(ResponseEntity.noContent().build());
        mvc.perform(delete("/users/12")).andExpect(status().isNoContent());
        verify(users).delete(12L);
    }

    @Test
    void invalidUserIsRejectedAtGateway() throws Exception {
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                                  .content("{\"name\":\"\",\"email\":\"not-an-email\"}"))
           .andExpect(status().isBadRequest());
        verifyNoInteractions(users);
    }

    @Test
    void invalidUserUpdateRejectedAtGateway() throws Exception {
        mvc.perform(patch("/users/12").contentType(MediaType.APPLICATION_JSON)
                                      .content("{\"name\":\"   \"}"))
           .andExpect(status().isBadRequest());
        verifyNoInteractions(users);
    }

    @Test
    void itemCreate() throws Exception {
        given(items.create(any(ItemCreateDto.class), eq(5L))).willReturn(ok());
        mvc.perform(post("/items").header("X-Sharer-User-Id", 5).contentType(MediaType.APPLICATION_JSON)
                                  .content("{\"name\":\"Drill\",\"description\":\"Hand drill\",\"available\":true,\"requestId\":3}"))
           .andExpect(status().isOk());
        verify(items).create(any(ItemCreateDto.class), eq(5L));
    }

    @Test
    void missingItemAvailabilityRejectedAtGateway() throws Exception {
        mvc.perform(post("/items").header("X-Sharer-User-Id", 5).contentType(MediaType.APPLICATION_JSON)
                                  .content("{\"name\":\"Drill\",\"description\":\"Tool\"}"))
           .andExpect(status().isBadRequest());
        verifyNoInteractions(items);
    }

    @Test
    void itemUpdate() throws Exception {
        given(items.update(any(ItemUpdateDto.class), eq(9L), eq(5L))).willReturn(ok());
        mvc.perform(patch("/items/9").header("X-Sharer-User-Id", 5)
                                     .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"New name\"}"))
           .andExpect(status().isOk());
        verify(items).update(any(ItemUpdateDto.class), eq(9L), eq(5L));
    }

    @Test
    void blankItemUpdateRejectedAtGateway() throws Exception {
        mvc.perform(patch("/items/9").header("X-Sharer-User-Id", 5)
                                     .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \"}"))
           .andExpect(status().isBadRequest());
        verifyNoInteractions(items);
    }

    @Test
    void itemSearch() throws Exception {
        given(items.search("drill")).willReturn(ok());
        mvc.perform(get("/items/search").param("text", "drill")).andExpect(status().isOk());
        verify(items).search("drill");
    }

    @Test
    void itemsByOwner() throws Exception {
        given(items.findAll(5L)).willReturn(ok());
        mvc.perform(get("/items").header("X-Sharer-User-Id", 5)).andExpect(status().isOk());
        verify(items).findAll(5L);
    }

    @Test
    void itemById() throws Exception {
        given(items.findById(9L)).willReturn(ok());
        mvc.perform(get("/items/9")).andExpect(status().isOk());
        verify(items).findById(9L);
    }

    @Test
    void commentCreate() throws Exception {
        given(items.createComment(any(CommentDto.class), eq(5L), eq(9L))).willReturn(ok());
        mvc.perform(post("/items/9/comment").header("X-Sharer-User-Id", 5)
                                            .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"Nice\"}"))
           .andExpect(status().isOk());
        verify(items).createComment(any(CommentDto.class), eq(5L), eq(9L));
    }

    @Test
    void blankCommentRejectedAtGateway() throws Exception {
        mvc.perform(post("/items/9/comment").header("X-Sharer-User-Id", 5)
                                            .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"  \"}"))
           .andExpect(status().isBadRequest());
        verifyNoInteractions(items);
    }

    @Test
    void requestCreate() throws Exception {
        given(requests.create(any(ItemRequestCreateDto.class), eq(5L))).willReturn(ok());
        mvc.perform(post("/requests").header("X-Sharer-User-Id", 5).contentType(MediaType.APPLICATION_JSON)
                                     .content("{\"description\":\"Need a drill\"}"))
           .andExpect(status().isOk());
        verify(requests).create(any(ItemRequestCreateDto.class), eq(5L));
    }

    @Test
    void blankRequestRejectedAtGateway() throws Exception {
        mvc.perform(post("/requests").header("X-Sharer-User-Id", 5).contentType(MediaType.APPLICATION_JSON)
                                     .content("{\"description\":\"\"}"))
           .andExpect(status().isBadRequest());
        verifyNoInteractions(requests);
    }

    @Test
    void requestsByRequester() throws Exception {
        given(requests.findAll(5L)).willReturn(ok());
        mvc.perform(get("/requests").header("X-Sharer-User-Id", 5)).andExpect(status().isOk());
        verify(requests).findAll(5L);
    }

    @Test
    void requestsByOthers() throws Exception {
        given(requests.findAllByOthers(5L)).willReturn(ok());
        mvc.perform(get("/requests/all").header("X-Sharer-User-Id", 5)).andExpect(status().isOk());
        verify(requests).findAllByOthers(5L);
    }

    @Test
    void requestById() throws Exception {
        given(requests.findByRequestId(3L)).willReturn(ok());
        mvc.perform(get("/requests/3")).andExpect(status().isOk());
        verify(requests).findByRequestId(3L);
    }

    @Test
    void bookingCreate() throws Exception {
        given(bookings.bookItem(eq(5L), any(BookItemRequestDto.class))).willReturn(ok());
        mvc.perform(post("/bookings").header("X-Sharer-User-Id", 5).contentType(MediaType.APPLICATION_JSON)
                                     .content("{\"itemId\":9,\"start\":\"2030-01-01T12:00:00\",\"end\":\"2030-01-02T12:00:00\"}"))
           .andExpect(status().isOk());
        verify(bookings).bookItem(eq(5L), any(BookItemRequestDto.class));
    }

    @Test
    void bookingById() throws Exception {
        given(bookings.getBooking(5L, 8L)).willReturn(ok());
        mvc.perform(get("/bookings/8").header("X-Sharer-User-Id", 5)).andExpect(status().isOk());
        verify(bookings).getBooking(5L, 8L);
    }

    @Test
    void bookingsByBookerStateCaseInsensitive() throws Exception {
        given(bookings.getBookings(5L, BookingState.WAITING, 0, 10)).willReturn(ok());
        mvc.perform(get("/bookings").header("X-Sharer-User-Id", 5).param("state", "waiting"))
           .andExpect(status().isOk());
        verify(bookings).getBookings(5L, BookingState.WAITING, 0, 10);
    }

    @Test
    void bookingConfirm() throws Exception {
        given(bookings.confirmation(8L, 5L, true)).willReturn(ok());
        mvc.perform(patch("/bookings/8").header("X-Sharer-User-Id", 5).param("approved", "true"))
           .andExpect(status().isOk());
        verify(bookings).confirmation(8L, 5L, true);
    }

    @Test
    void bookingsByOwnerStateCaseInsensitive() throws Exception {
        given(bookings.findByOwnerId(5L, BookingState.REJECTED)).willReturn(ok());
        mvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", 5).param("state", "rejected"))
           .andExpect(status().isOk());
        verify(bookings).findByOwnerId(5L, BookingState.REJECTED);
    }

    @Test
    void invalidBookingDatesRejectedAtGateway() throws Exception {
        mvc.perform(post("/bookings").header("X-Sharer-User-Id", 5).contentType(MediaType.APPLICATION_JSON)
                                     .content("{\"itemId\":9,\"start\":\"2020-01-01T12:00:00\",\"end\":\"2020-01-02T12:00:00\"}"))
           .andExpect(status().isBadRequest());
        verifyNoInteractions(bookings);
    }

    @Test
    void missingUserHeaderRejectedAtGateway() throws Exception {
        mvc.perform(get("/bookings")).andExpect(status().isBadRequest());
        verifyNoInteractions(bookings);
    }

    private ResponseEntity<Object> ok() {
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
