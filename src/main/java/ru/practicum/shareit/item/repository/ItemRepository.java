package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends CrudRepository<Item, Long> {

    Item getItemById(Long itemId);

    List<Item> findByOwnerIdOrderById(Long ownerId);

    @Query("select i " +
            "from Item as i " +
            "where i.available = true and" +
            " (UPPER(i.name) like UPPER(concat('%', ?1,'%'))" +
            " or UPPER(i.description) like UPPER(concat('%', ?1, '%')))")
    List<Item> findByNameOrDescription(String searchPhrase);
}
