package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService requestService;
    private static final String USER_HEADER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestDto addItemRequest(@RequestBody ItemRequestDto itemRequestDto,
                                         @RequestHeader(USER_HEADER_ID) Long userId) {
        return requestService.addRequest(itemRequestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDtoWithItems> findAllUserRequests(@RequestHeader(USER_HEADER_ID) Long userId) {
        return requestService.findAll(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDtoWithItems> findAllRequests(@RequestHeader(USER_HEADER_ID) Long userId,
                                                         @RequestParam(defaultValue = "0") int from,
                                                         @RequestParam(defaultValue = "10") int size) {
        return requestService.findAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto findRequestById(@RequestHeader(USER_HEADER_ID) Long userId, @PathVariable Long requestId) {
        return requestService.findById(userId, requestId);
    }
}
