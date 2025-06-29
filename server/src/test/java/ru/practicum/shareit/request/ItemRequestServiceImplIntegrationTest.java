package ru.practicum.shareit.request;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplIntegrationTest {
    private final UserService userService;
    private final ItemRequestService requestService;

    @Test
    void testFindAll() {
        UserDto userDtoOne = UserDto.builder()
                .name("Alex")
                .email("alex@test.ru")
                .build();

        UserDto userDtoTwo = UserDto.builder()
                .name("Serg")
                .email("serg@test.ru")
                .build();
        UserDto createdUserOne = userService.createUser(userDtoOne);
        UserDto createdUserTwo = userService.createUser(userDtoTwo);

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("Чем бы перемешать бетон?")
                .build();
        ItemRequestDto addedItemRequest = requestService.addRequest(itemRequestDto, createdUserOne.getId());

        List<ItemRequestDtoWithItems> itemRequests = requestService.findAll(createdUserTwo.getId(), 0, 5);

        assertThat(itemRequests, hasSize(1));
        assertThat(itemRequests, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("description", equalTo(addedItemRequest.getDescription()))
        )));
    }

    @Test
    void testFindAllWithoutPages() {
        UserDto userDtoOne = UserDto.builder()
                .name("Alex")
                .email("alex@test.ru")
                .build();

        UserDto userDtoTwo = UserDto.builder()
                .name("Serg")
                .email("serg@test.ru")
                .build();
        UserDto createdUserOne = userService.createUser(userDtoOne);
        userService.createUser(userDtoTwo);

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("Чем бы перемешать бетон?")
                .build();
        ItemRequestDto addedItemRequest = requestService.addRequest(itemRequestDto, createdUserOne.getId());

        List<ItemRequestDtoWithItems> itemRequests = requestService.findAll(createdUserOne.getId());

        assertThat(itemRequests, hasSize(1));
        assertThat(itemRequests, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("description", equalTo(addedItemRequest.getDescription()))
        )));
    }
}
