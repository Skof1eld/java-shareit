package ru.practicum.shareit.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anySet;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplUnitTest {
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    private ItemRequestService service;

    @BeforeEach
    public void makeItemRequestService() {
        this.service = new ItemRequestServiceImpl(requestRepository, userRepository, itemRepository);
    }

    @Test
    void testAddRequestWrongUser() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("Чем бы смешать цемент")
                .build();

        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.addRequest(itemRequestDto, 1L));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(requestRepository);
    }

    @Test
    void testFindAllWrongUser() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.findAll(1L));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(requestRepository);
    }

    @Test
    void testFindAllWithPaginationWrongUser() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.findAll(1L, 0, 5));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(requestRepository);
    }

    @Test
    void testFindAllWithoutPaginationWrongUser() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.findAll(1L));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(requestRepository);
    }

    @Test
    void testFindByIdWrongUser() {
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.findById(1L, 1L));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(requestRepository);
        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void testFindByIdItemRequestNotFound() {
        User user = new User(1L, "Alex", "alex@test.com");
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito
                .when(requestRepository.findById(1L))
                .thenReturn(Optional.empty());

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.findById(1L, 1L));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void testFindByIdItemRequestOk() {
        User user = new User(1L, "Alex", "alex@test.com");
        User itemOwner = new User(2L, "Vika", "vika@test.com");
        ItemRequest itemRequest = new ItemRequest(1L, user, "Нужна переноска", LocalDateTime.now());
        Item item = new Item(1L, itemOwner, itemRequest, "Сумка", "переносить животное", true);
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito
                .when(requestRepository.findById(1L))
                .thenReturn(Optional.of(itemRequest));
        Mockito
                .when(itemRepository.findByRequestIdIn(anySet()))
                .thenReturn(List.of(item));

        service.findById(1L, 1L);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .findByRequestIdIn(anySet());
    }
}
