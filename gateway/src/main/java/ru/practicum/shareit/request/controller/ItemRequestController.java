package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody ItemRequestCreateDto requestDto,
                                         @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Creating request {}, userId={}", requestDto, userId);
        return itemRequestClient.create(requestDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> findAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get all requests userId={}", userId);
        return itemRequestClient.findAll(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllByOthers(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get requests userId={}", userId);
        return itemRequestClient.findAllByOthers(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findByRequestId(@PathVariable("requestId") Long requestId) {
        log.info("Get requestId={}", requestId);
        return itemRequestClient.findByRequestId(requestId);
    }
}
