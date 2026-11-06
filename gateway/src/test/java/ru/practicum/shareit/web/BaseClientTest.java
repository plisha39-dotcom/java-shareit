package ru.practicum.shareit.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BaseClientTest {
    private RestTemplate rest;
    private TestClient client;

    @BeforeEach
    void setup() {
        rest = mock(RestTemplate.class);
        client = new TestClient(rest);
    }

    @Test
    void getWithoutUserIdAddsJsonHeadersButNoUserHeader() {
        successWithoutParameters();

        assertEquals(HttpStatus.OK, client.callGet("/health").getStatusCode());

        org.mockito.ArgumentCaptor<HttpEntity> captured = org.mockito.ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq("/health"), eq(HttpMethod.GET), captured.capture(), eq(Object.class));
        HttpEntity<?> sent = captured.getValue();
        assertNull(sent.getBody());
        assertEquals(MediaType.APPLICATION_JSON, sent.getHeaders().getContentType());
        assertEquals(MediaType.APPLICATION_JSON, sent.getHeaders().getAccept().get(0));
        assertFalse(sent.getHeaders().containsKey("X-Sharer-User-Id"));
    }

    @Test
    void directOverloadsCoverGetPostPutPatchAndDelete() {
        successWithoutParameters();

        client.callGetWithUser("/owned", 5L);
        client.callPost("/users", "user");
        client.callPostWithUser("/items", 5L, "item");
        client.callPutWithUser("/items/1", 5L, "updated");
        client.callPatch("/users/1", "updated");
        client.callPatchNoBody("/items/1", 5L);
        client.callPatchWithUser("/items/1", 5L, "updated");
        client.callDelete("/users/1");
        client.callDeleteWithUser("/items/1", 5L);

        verify(rest).exchange(eq("/owned"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class));
        verify(rest).exchange(eq("/users"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class));
        verify(rest).exchange(eq("/items"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class));
        verify(rest).exchange(eq("/items/1"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Object.class));
        verify(rest).exchange(eq("/users/1"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class));
        verify(rest, times(2)).exchange(eq("/items/1"), eq(HttpMethod.PATCH),
                any(HttpEntity.class), eq(Object.class));
        verify(rest).exchange(eq("/users/1"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class));
        verify(rest).exchange(eq("/items/1"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void userIdHeaderAndBodyArePreserved() {
        successWithoutParameters();
        client.callPostWithUser("/items", 5L, "item payload");

        org.mockito.ArgumentCaptor<HttpEntity> captured = org.mockito.ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq("/items"), eq(HttpMethod.POST), captured.capture(), eq(Object.class));
        assertEquals("5", captured.getValue().getHeaders().getFirst("X-Sharer-User-Id"));
        assertEquals("item payload", captured.getValue().getBody());
    }

    @Test
    void parameterOverloadsPassUrlTemplateVariables() {
        when(rest.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(Object.class), anyMap())).thenReturn(ResponseEntity.ok("ok"));
        Map<String, Object> query = Map.of("state", "WAITING", "approved", true);

        client.callGetWithParameters("/bookings?state={state}", 5L, query);
        client.callPostWithParameters("/items?state={state}", 5L, query, "body");
        client.callPutWithParameters("/items?state={state}", 5L, query, "body");
        client.callPatchWithParameters("/bookings?approved={approved}", 5L, query, null);
        client.callDeleteWithParameters("/items?state={state}", 5L, query);

        verify(rest).exchange(eq("/bookings?state={state}"), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(Object.class), eq(query));
        verify(rest).exchange(eq("/items?state={state}"), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(Object.class), eq(query));
        verify(rest).exchange(eq("/items?state={state}"), eq(HttpMethod.PUT),
                any(HttpEntity.class), eq(Object.class), eq(query));
        verify(rest).exchange(eq("/bookings?approved={approved}"), eq(HttpMethod.PATCH),
                any(HttpEntity.class), eq(Object.class), eq(query));
        verify(rest).exchange(eq("/items?state={state}"), eq(HttpMethod.DELETE),
                any(HttpEntity.class), eq(Object.class), eq(query));
    }

    @Test
    void nonSuccessResponseWithBodyIsRelayed() {
        when(rest.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(new ResponseEntity<>("problem", HttpStatus.BAD_REQUEST));

        ResponseEntity<Object> response = client.callGet("/health");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("problem", response.getBody());
    }

    @Test
    void nonSuccessResponseWithoutBodyIsRelayed() {
        when(rest.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        ResponseEntity<Object> response = client.callGet("/health");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.hasBody());
    }

    @Test
    void unsuccessfulHttpExceptionIsConvertedToResponse() {
        String error = "{\"error\":\"bad request\"}";
        when(rest.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request",
                        HttpHeaders.EMPTY, error.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        ResponseEntity<Object> response = client.callGet("/health");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertArrayEquals(error.getBytes(StandardCharsets.UTF_8), (byte[]) response.getBody());
    }

    private void successWithoutParameters() {
        when(rest.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));
    }

    private static class TestClient extends BaseClient {
        TestClient(RestTemplate rest) {
            super(rest);
        }

        ResponseEntity<Object> callGet(String path) {
            return get(path);
        }

        ResponseEntity<Object> callGetWithUser(String path, long userId) {
            return get(path, userId);
        }

        ResponseEntity<Object> callGetWithParameters(String path, Long userId, Map<String, Object> params) {
            return get(path, userId, params);
        }

        ResponseEntity<Object> callPost(String path, Object body) {
            return post(path, body);
        }

        ResponseEntity<Object> callPostWithUser(String path, long userId, Object body) {
            return post(path, userId, body);
        }

        ResponseEntity<Object> callPostWithParameters(String path, Long userId,
                                                      Map<String, Object> params, Object body) {
            return post(path, userId, params, body);
        }

        ResponseEntity<Object> callPutWithUser(String path, long userId, Object body) {
            return put(path, userId, body);
        }

        ResponseEntity<Object> callPutWithParameters(String path, long userId,
                                                     Map<String, Object> params, Object body) {
            return put(path, userId, params, body);
        }

        ResponseEntity<Object> callPatch(String path, Object body) {
            return patch(path, body);
        }

        ResponseEntity<Object> callPatchNoBody(String path, long userId) {
            return patch(path, userId);
        }

        ResponseEntity<Object> callPatchWithUser(String path, long userId, Object body) {
            return patch(path, userId, body);
        }

        ResponseEntity<Object> callPatchWithParameters(String path, Long userId,
                                                       Map<String, Object> params, Object body) {
            return patch(path, userId, params, body);
        }

        ResponseEntity<Object> callDelete(String path) {
            return delete(path);
        }

        ResponseEntity<Object> callDeleteWithUser(String path, long userId) {
            return delete(path, userId);
        }

        ResponseEntity<Object> callDeleteWithParameters(String path, Long userId, Map<String, Object> params) {
            return delete(path, userId, params);
        }
    }
}
