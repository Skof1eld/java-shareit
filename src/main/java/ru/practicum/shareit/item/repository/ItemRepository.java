package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    List<Item> findAll();

    Item getItemById(Long itemId);

    List<Item> findItemsByOwnerId(Long ownerId);

    List<Item> findItemsByNameOrDescription(String searchPhrase);

    Item add(Item item);

    Item save(Item item);
}
