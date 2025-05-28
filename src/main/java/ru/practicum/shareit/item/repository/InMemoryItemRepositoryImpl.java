package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private Long itemId = 1L;

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public Item getItemById(Long itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> findItemsByOwnerId(Long ownerId) {
        return items.values()
                .stream()
                .filter(item -> Objects.equals(item.getOwner().getId(), ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findItemsByNameOrDescription(String searchPhrase) {
        String lowerSearchPhrase = searchPhrase.toLowerCase();
        return items.values()
                .stream()
                .filter(item -> item.getAvailable()
                        && (item.getName().toLowerCase().contains(lowerSearchPhrase)
                        || item.getDescription().toLowerCase().contains(lowerSearchPhrase)))
                .collect(Collectors.toList());
    }

    @Override
    public Item add(Item item) {
        item.setId(getId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item save(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    private long getId() {
        return this.itemId++;
    }
}
