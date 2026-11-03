package ru.practicum.shareit.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GatewayClientTest {
    private static final String SERVER = "http://localhost:9090";
    private UserClient users;
    private ItemClient items;
    private ItemRequestClient requests;
    private BookingClient bookings;

    @BeforeEach
    void createClients() {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        users = new UserClient(SERVER, builder);
        items = new ItemClient(SERVER, builder);
        requests = new ItemRequestClient(SERVER, builder);
        bookings = new BookingClient(SERVER, builder);
    }

    @Test
    void findAllUsers() {
        MockRestServiceServer server = serverFor(users);
        server.expect(requestTo(SERVER + "/users"))
              .andExpect(method(HttpMethod.GET))
              .andExpect(headerDoesNotExist("X-Sharer-User-Id"))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertEquals(HttpStatus.OK, users.findAll().getStatusCode());
        server.verify();
    }

    @Test
    void findUserById() {
        MockRestServiceServer server = serverFor(users);
        server.expect(requestTo(SERVER + "/users/12"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(ok());

        assertEquals(HttpStatus.OK, users.findById(12).getStatusCode());
        server.verify();
    }

    @Test
    void createUserSendsJsonBody() {
        MockRestServiceServer server = serverFor(users);
        UserDto dto = new UserDto();
        dto.setName("Alice");
        dto.setEmail("alice@example.com");
        server.expect(requestTo(SERVER + "/users"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(content().json("{\"name\":\"Alice\",\"email\":\"alice@example.com\"}"))
              .andRespond(ok());

        assertEquals(HttpStatus.OK, users.create(dto).getStatusCode());
        server.verify();
    }

    @Test
    void updateUserUsesPatch() {
        MockRestServiceServer server = serverFor(users);
        UserUpdateDto dto = new UserUpdateDto();
        dto.setName("Updated");
        server.expect(requestTo(SERVER + "/users/12"))
              .andExpect(method(HttpMethod.PATCH))
              .andExpect(content().json("{\"name\":\"Updated\"}"))
              .andRespond(ok());

        assertEquals(HttpStatus.OK, users.update(dto, 12L).getStatusCode());
        server.verify();
    }

    @Test
    void deleteUserUsesDelete() {
        MockRestServiceServer server = serverFor(users);
        server.expect(requestTo(SERVER + "/users/12"))
              .andExpect(method(HttpMethod.DELETE))
              .andRespond(withStatus(HttpStatus.NO_CONTENT));

        assertEquals(HttpStatus.NO_CONTENT, users.delete(12).getStatusCode());
        server.verify();
    }

    @Test
    void createItemIncludesOwnerHeaderAndRequestId() {
        MockRestServiceServer server = serverFor(items);
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("Drill");
        dto.setDescription("Electric drill");
        dto.setAvailable(true);
        dto.setRequestId(3L);
        server.expect(requestTo(SERVER + "/items"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header("X-Sharer-User-Id", "5"))
              .andExpect(content().json("{\"name\":\"Drill\",\"available\":true,\"requestId\":3}"))
              .andRespond(ok());

        assertEquals(HttpStatus.OK, items.create(dto, 5L).getStatusCode());
        server.verify();
    }

    @Test
    void updateItemUsesPatch() {
        MockRestServiceServer server = serverFor(items);
        ItemUpdateDto dto = new ItemUpdateDto();
        dto.setName("New drill");
        server.expect(requestTo(SERVER + "/items/9"))
              .andExpect(method(HttpMethod.PATCH))
              .andExpect(header("X-Sharer-User-Id", "5"))
              .andExpect(content().json("{\"name\":\"New drill\"}"))
              .andRespond(ok());

        assertEquals(HttpStatus.OK, items.update(dto, 9L, 5L).getStatusCode());
        server.verify();
    }

    @Test
    void searchItemSubstitutesQueryParameters() {
        MockRestServiceServer server = serverFor(items);
        server.expect(requestTo(SERVER + "/items/search?text=drill"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertEquals(HttpStatus.OK, items.search("drill").getStatusCode());
        server.verify();
    }

    @Test
    void findAllOwnerItems() {
        MockRestServiceServer server = serverFor(items);
        server.expect(requestTo(SERVER + "/items"))
              .andExpect(method(HttpMethod.GET))
              .andExpect(header("X-Sharer-User-Id", "5"))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertEquals(HttpStatus.OK, items.findAll(5L).getStatusCode());
        server.verify();
    }

    @Test
    void findItemById() {
        MockRestServiceServer server = serverFor(items);
        server.expect(requestTo(SERVER + "/items/9"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(ok());

        assertEquals(HttpStatus.OK, items.findById(9L).getStatusCode());
        server.verify();
    }

    @Test
    void createCommentSendsTextAndOwnerHeader() {
        MockRestServiceServer server = serverFor(items);
        CommentDto dto = new CommentDto();
        dto.setText("Great drill");
        server.expect(requestTo(SERVER + "/items/9/comment"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header("X-Sharer-User-Id", "5"))
              .andExpect(content().json("{\"text\":\"Great drill\"}"))
              .andRespond(ok());

        assertEquals(HttpStatus.OK, items.createComment(dto, 5L, 9L).getStatusCode());
        server.verify();
    }

    @Test
    void createItemRequest() {
        MockRestServiceServer server = serverFor(requests);
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("Need a drill");
        server.expect(requestTo(SERVER + "/requests"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header("X-Sharer-User-Id", "5"))
              .andExpect(content().json("{\"description\":\"Need a drill\"}"))
              .andRespond(ok());

        assertEquals(HttpStatus.OK, requests.create(dto, 5L).getStatusCode());
        server.verify();
    }

    @Test
    void findOwnRequests() {
        MockRestServiceServer server = serverFor(requests);
        server.expect(requestTo(SERVER + "/requests"))
              .andExpect(method(HttpMethod.GET))
              .andExpect(header("X-Sharer-User-Id", "5"))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertEquals(HttpStatus.OK, requests.findAll(5L).getStatusCode());
        server.verify();
    }

    @Test
    void findOtherRequests() {
        MockRestServiceServer server = serverFor(requests);
        server.expect(requestTo(SERVER + "/requests/all"))
              .andExpect(method(HttpMethod.GET))
              .andExpect(header("X-Sharer-User-Id", "5"))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertEquals(HttpStatus.OK, requests.findAllByOthers(5L).getStatusCode());
        server.verify();
    }

    @Test
    void findRequestById() {
        MockRestServiceServer server = serverFor(requests);
        server.expect(requestTo(SERVER + "/requests/3"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(ok());

        assertEquals(HttpStatus.OK, requests.findByRequestId(3L).getStatusCode());
        server.verify();
    }

    @Test
    void bookingListPassesPaginationAndState() {
        MockRestServiceServer server = serverFor(bookings);
        server.expect(requestTo(SERVER + "/bookings?state=WAITING&from=10&size=5"))
              .andExpect(method(HttpMethod.GET))
              .andExpect(header("X-Sharer-User-Id", "5"))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertEquals(HttpStatus.OK, bookings.getBookings(5L, BookingState.WAITING, 10, 5).getStatusCode());
        server.verify();
    }

    @Test
    void createBookingSendsBodyAndBooker() {
        MockRestServiceServer server = serverFor(bookings);
        BookItemRequestDto dto = new BookItemRequestDto(9L,
                LocalDateTime.of(2030, 1, 1, 12, 0),
                LocalDateTime.of(2030, 1, 2, 12, 0));
        server.expect(requestTo(SERVER + "/bookings"))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header("X-Sharer-User-Id", "5"))
              .andExpect(content().json("{\"itemId\":9}"))
              .andRespond(ok());

        assertEquals(HttpStatus.OK, bookings.bookItem(5L, dto).getStatusCode());
        server.verify();
    }

    @Test
    void bookingById() {
        MockRestServiceServer server = serverFor(bookings);
        server.expect(requestTo(SERVER + "/bookings/8"))
              .andExpect(method(HttpMethod.GET))
              .andExpect(header("X-Sharer-User-Id", "5"))
              .andRespond(ok());

        assertEquals(HttpStatus.OK, bookings.getBooking(5L, 8L).getStatusCode());
        server.verify();
    }

    @Test
    void confirmBookingSendsApprovedInQueryNotBody() {
        MockRestServiceServer server = serverFor(bookings);
        server.expect(requestTo(SERVER + "/bookings/8?approved=true"))
              .andExpect(method(HttpMethod.PATCH))
              .andExpect(header("X-Sharer-User-Id", "5"))
              .andExpect(content().string(""))
              .andRespond(ok());

        assertEquals(HttpStatus.OK, bookings.confirmation(8L, 5L, true).getStatusCode());
        server.verify();
    }

    @Test
    void findOwnerBookingsWithState() {
        MockRestServiceServer server = serverFor(bookings);
        server.expect(requestTo(SERVER + "/bookings/owner?state=REJECTED"))
              .andExpect(method(HttpMethod.GET))
              .andExpect(header("X-Sharer-User-Id", "5"))
              .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertEquals(HttpStatus.OK, bookings.findByOwnerId(5L, BookingState.REJECTED).getStatusCode());
        server.verify();
    }

    @Test
    void serverErrorStatusAndBodyArePropagated() {
        MockRestServiceServer server = serverFor(users);
        String error = "{\"error\":\"User not found\"}";
        server.expect(requestTo(SERVER + "/users/999"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withStatus(HttpStatus.NOT_FOUND)
                      .contentType(MediaType.APPLICATION_JSON).body(error));

        ResponseEntity<Object> result = users.findById(999);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertArrayEquals(error.getBytes(StandardCharsets.UTF_8), (byte[]) result.getBody());
        server.verify();
    }

    @Test
    void bookingStateConversionAcceptsDifferentCaseAndRejectsUnknown() {
        assertEquals(BookingState.CURRENT, BookingState.from("current").orElseThrow());
        assertEquals(BookingState.ALL, BookingState.from("All").orElseThrow());
        assertEquals(BookingState.WAITING, BookingState.from("WAITING").orElseThrow());
        assertFalse(BookingState.from("unrecognized").isPresent());
        assertTrue(BookingState.from("REJECTED").isPresent());
    }

    private MockRestServiceServer serverFor(BaseClient client) {
        RestTemplate rest = (RestTemplate) ReflectionTestUtils.getField(client, "rest");
        return MockRestServiceServer.createServer(rest);
    }

    private org.springframework.test.web.client.ResponseCreator ok() {
        return withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON);
    }
}
