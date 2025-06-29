package ru.practicum.shareit.item;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@DataJpaTest(properties = "db.name=test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRepositoryDataJpaTest {
    @Autowired
    TestEntityManager em;
    @Autowired
    ItemRepository itemRepository;

    @Test
    void testFindByNameOrDescription() {
        User user = new User();
        user.setName("Alex");
        user.setEmail("alex@test.ru");
        em.persist(user);
        em.flush();

        Item item = new Item();
        item.setName("Колесо");
        item.setDescription("Велосипедное колесо");
        item.setOwner(user);
        item.setAvailable(true);
        em.persist(item);
        em.flush();

        Page<Item> items = itemRepository.findByNameOrDescription("колесо", Pageable.unpaged());
        assertThat(items.getContent(), hasSize(1));
        assertThat(items, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("owner", equalTo(item.getOwner())),
                hasProperty("name", equalTo(item.getName())),
                hasProperty("description", equalTo(item.getDescription())),
                hasProperty("available", equalTo(item.getAvailable()))
        )));
    }
}
