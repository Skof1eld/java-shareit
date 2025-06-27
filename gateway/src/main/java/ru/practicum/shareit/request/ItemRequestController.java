package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemRequestController {
    private final ItemRequestClient client;
    private static final String USER_HEADER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> addItemRequest(@RequestBody @Valid ItemRequestDto itemRequestDto,
                                                 @RequestHeader(USER_HEADER_ID) Long userId) {
        return client.addItemRequest(itemRequestDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> findAllUserRequests(@RequestHeader(USER_HEADER_ID) Long userId) {
        return client.findAllUserRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllRequests(@RequestHeader(USER_HEADER_ID) Long userId,
                                                  @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                  @Positive @RequestParam(defaultValue = "10") int size) {
        return client.findAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findRequestById(@RequestHeader(USER_HEADER_ID) Long userId,
                                                  @PathVariable Long requestId) {
        return client.findRequestById(userId, requestId);
    }
}
