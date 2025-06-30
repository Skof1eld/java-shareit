package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

@Repository
public interface ItemRepository extends CrudRepository<Item, Long> {

    Item getItemById(Long itemId);

    Page<Item> findByOwnerIdOrderById(Long ownerId, Pageable page);

    @Query("select i " +
            "from Item as i " +
            "where i.available = true and" +
            " (UPPER(i.name) like UPPER(concat('%', ?1,'%'))" +
            " or UPPER(i.description) like UPPER(concat('%', ?1, '%')))")
    Page<Item> findByNameOrDescription(String searchPhrase, Pageable page);

    List<Item> findByRequestIdIn(Collection<Long> requestIds);
}
