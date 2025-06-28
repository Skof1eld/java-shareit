package ru.practicum.shareit.user;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(properties = "db.name=test", webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserServiceImplIntegrationTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private UserService service;

    @Test
    void testGetAllUsers() {
        UserDto userDtoOne = UserDto.builder()
                .name("Alex")
                .email("alex@test.ru")
                .build();
        UserDto userDtoTwo = UserDto.builder()
                .name("Serg")
                .email("serg@test.ru")
                .build();
        UserDto userDtoThree = UserDto.builder()
                .name("Vika")
                .email("vika@test.ru")
                .build();
        List<UserDto> sourceUsers = List.of(
                userDtoOne,
                userDtoTwo,
                userDtoThree
        );

        for (UserDto userDto : sourceUsers) {
            User user = UserMapper.mapUserDtoToUser(userDto);
            em.persist(user);
        }
        em.flush();

        List<UserDto> targetUsers = service.getAllUsers();

        assertThat(targetUsers, hasSize(sourceUsers.size()));
        for (UserDto sourceUser : sourceUsers) {
            assertThat(targetUsers, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceUser.getName())),
                    hasProperty("email", equalTo(sourceUser.getEmail()))
            )));
        }
    }
}
