package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ObjectNotValidException;
import ru.practicum.shareit.item.comments.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Controller
@RequiredArgsConstructor
@Validated
@RequestMapping("/items")
public class ItemController {
    private final ItemClient client;

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> findItemsByOwnerId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                     @Positive @RequestParam(defaultValue = "10") int size) {
        return client.findItemsByOwnerId(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItemsByPhrase(@RequestParam("text") String searchPhrase,
                                                      @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                      @Positive @RequestParam(defaultValue = "10") int size) {
        return client.searchItemsByPhrase(searchPhrase, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody @Valid ItemDto itemDto) {
        isValidForCreation(itemDto);
        return client.addItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    @Cacheable
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody @Valid CommentDto commentDto,
                                             @PathVariable long itemId) {
        return client.addComment(userId, commentDto, itemId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemDto itemDto) {
        isValidForUpdate(itemDto);
        return client.updateItem(itemId, userId, itemDto);
    }

    private void isValidForCreation(ItemDto itemDto) {
        if (itemDto.getName() == null
                || itemDto.getName().isEmpty()
                || itemDto.getDescription() == null
                || itemDto.getDescription().isEmpty()
                || itemDto.getAvailable() == null) {
            throw new ObjectNotValidException();
        }
    }

    private void isValidForUpdate(ItemDto itemDto) {
        if (itemDto.getName() != null
                && itemDto.getName().isEmpty()
                || itemDto.getDescription() != null
                && itemDto.getDescription().isEmpty()) {
            throw new ObjectNotValidException();
        }
    }
}
