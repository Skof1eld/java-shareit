package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comments.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private static final String USER_HEADER_ID = "X-Sharer-User-Id";

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId, @RequestHeader(USER_HEADER_ID) Long userId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> findItemsByOwnerId(@RequestHeader(USER_HEADER_ID) Long userId,
                                            @RequestParam(defaultValue = "0") int from,
                                            @RequestParam(defaultValue = "10") int size) {
        return itemService.findItemsByOwnerId(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItemsByPhrase(@RequestParam("text") String searchPhrase,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        return itemService.searchItemsByPhrase(searchPhrase, from, size);
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody @Valid ItemDto itemDto) {
        return itemService.addItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody CommentDto commentDto,
                                 @PathVariable long itemId) {
        return itemService.addComment(itemId, userId, commentDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId, @RequestHeader(USER_HEADER_ID) Long userId, @RequestBody ItemDto itemDto) {
        return itemService.updateItem(itemId, userId, itemDto);
    }
}
