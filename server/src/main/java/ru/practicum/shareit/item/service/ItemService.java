package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comments.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto getItemById(Long itemId, Long userId);

    List<ItemDto> findItemsByOwnerId(Long ownerId, int from, int size);

    List<ItemDto> searchItemsByPhrase(String searchPhrase, int from, int size);

    ItemDto addItem(Long ownerId, ItemDto itemDto);

    ItemDto updateItem(Long itemId, Long ownerId, ItemDto itemDto);

    CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);
}
