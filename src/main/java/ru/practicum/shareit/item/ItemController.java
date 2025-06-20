package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    public ItemDto getItemById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> findItemsByOwnerId(@RequestHeader(USER_HEADER_ID) Long userId) {
        return itemService.findItemsByOwnerId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItemsByPhrase(@RequestParam("text") String searchPhrase) {
        return itemService.searchItemsByPhrase(searchPhrase);
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader(USER_HEADER_ID) Long userId, @RequestBody ItemDto itemDto) {
        return itemService.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateUser(@PathVariable Long itemId, @RequestHeader(USER_HEADER_ID) Long userId, @RequestBody ItemDto itemDto) {
        return itemService.updateItem(itemId, userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody @Valid CommentDto commentDto,
                                 @PathVariable long itemId) {
        return itemService.addComment(itemId, userId, commentDto);
    }
}
