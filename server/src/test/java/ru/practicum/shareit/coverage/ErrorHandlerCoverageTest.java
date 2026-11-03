package ru.practicum.shareit.coverage;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.exception.ErrorResponse;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorHandlerCoverageTest {
    private final ErrorHandler handler = new ErrorHandler();

    @Test
    void conflictProducesHttp409() {
        ResponseEntity<ErrorResponse> response = handler.handleConflictException(new ConflictException("duplicate"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("duplicate", response.getBody().getError());
    }

    @Test
    void missingEntityProducesHttp404() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFoundException(new NotFoundException("missing"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", response.getBody().getError());
    }

    @Test
    void invalidInputProducesHttp400() {
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(new ValidationException("invalid"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("invalid", response.getBody().getError());
    }

    @Test
    void forbiddenActionProducesHttp403() {
        ResponseEntity<ErrorResponse> response = handler.handleForbiddenException(new ForbiddenException("forbidden"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("forbidden", response.getBody().getError());
    }
}
