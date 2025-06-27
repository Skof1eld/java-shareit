package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NoRightsForUpdateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comments.Comment;
import ru.practicum.shareit.item.comments.CommentDto;
import ru.practicum.shareit.item.comments.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplUnitTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository requestRepository;

    @Test
    void testGetItemByIdOkByUser() {
        ItemService service = makeItemService();

        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(new Item(1L,
                        new User(1L, "Alex", "alex@test.com"),
                        null, "Бетономешалка", "Мешает бетон", true));
        Mockito
                .when(commentRepository.findByItemIdInOrderByCreated(Mockito.anyCollection()))
                .thenReturn(Collections.emptyList());

        ItemDto itemDto = service.getItemById(1L, 2L);

        assertThat(itemDto.getId(), equalTo(1L));
        assertThat(itemDto.getOwnerId(), equalTo(1L));
        assertThat(itemDto.getRequestId(), nullValue());
        assertThat(itemDto.getName(), equalTo("Бетономешалка"));
        assertThat(itemDto.getDescription(), equalTo("Мешает бетон"));
        assertThat(itemDto.getAvailable(), equalTo(true));

        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verify(commentRepository, Mockito.times(1))
                .findByItemIdInOrderByCreated(Mockito.anyCollection());
    }

    @Test
    void testGetItemByIdOkByUserWithOneBookingLast() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Alex", "alex@test.ru");
        User booker = new User(2L, "Serg", "serg@test.ru");
        Item item = new Item(1L, owner, null, "Бетономешалка", "Мешает бетон", true);
        Booking lastBooking = new Booking(1L, booker, item, LocalDateTime.now().minusDays(4),
                LocalDateTime.now().minusDays(3), BookingStatus.APPROVED);

        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(new Item(1L,
                        new User(1L, "Alex", "alex@test.com"),
                        null, "Бетономешалка", "Мешает бетон", true));
        Mockito
                .when(commentRepository.findByItemIdInOrderByCreated(Mockito.anyCollection()))
                .thenReturn(Collections.emptyList());
        Mockito
                .when(bookingRepository.findLastAndNearFutureBookingsByItemIn(anySet(), Mockito.any(LocalDateTime.class), Mockito.any(Sort.class)))
                .thenReturn(List.of(lastBooking));

        ItemDto itemDto = service.getItemById(1L, 1L);

        assertThat(itemDto.getId(), equalTo(1L));
        assertThat(itemDto.getOwnerId(), equalTo(1L));
        assertThat(itemDto.getRequestId(), nullValue());
        assertThat(itemDto.getName(), equalTo("Бетономешалка"));
        assertThat(itemDto.getDescription(), equalTo("Мешает бетон"));
        assertThat(itemDto.getAvailable(), equalTo(true));

        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findLastAndNearFutureBookingsByItemIn(anySet(), Mockito.any(LocalDateTime.class), Mockito.any(Sort.class));
        Mockito.verify(commentRepository, Mockito.times(1))
                .findByItemIdInOrderByCreated(Mockito.anyCollection());
    }

    @Test
    void testGetItemByIdOkByUserWithOneBookingFuture() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Serg", "serg@test.ru");
        User booker = new User(2L, "Vika", "vika@test.ru");
        Item item = new Item(1L, owner, null, "Бетономешалка", "Мешает бетон", true);
        Booking nextBooking = new Booking(1L, booker, item, LocalDateTime.now().plusDays(4),
                LocalDateTime.now().plusDays(6), BookingStatus.APPROVED);

        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(new Item(1L,
                        new User(1L, "Alex", "alex@test.com"),
                        null, "Бетономешалка", "Мешает бетон", true));
        Mockito
                .when(commentRepository.findByItemIdInOrderByCreated(Mockito.anyCollection()))
                .thenReturn(Collections.emptyList());
        Mockito
                .when(bookingRepository.findLastAndNearFutureBookingsByItemIn(anySet(), Mockito.any(LocalDateTime.class), Mockito.any(Sort.class)))
                .thenReturn(List.of(nextBooking));

        ItemDto itemDto = service.getItemById(1L, 1L);

        assertThat(itemDto.getId(), equalTo(1L));
        assertThat(itemDto.getOwnerId(), equalTo(1L));
        assertThat(itemDto.getRequestId(), nullValue());
        assertThat(itemDto.getName(), equalTo("Бетономешалка"));
        assertThat(itemDto.getDescription(), equalTo("Мешает бетон"));
        assertThat(itemDto.getAvailable(), equalTo(true));

        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findLastAndNearFutureBookingsByItemIn(anySet(), Mockito.any(LocalDateTime.class), Mockito.any(Sort.class));
        Mockito.verify(commentRepository, Mockito.times(1))
                .findByItemIdInOrderByCreated(Mockito.anyCollection());
    }

    @Test
    void testGetItemByIdOkByUserWithTwoBooking() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Serg", "serg@test.ru");
        User booker = new User(2L, "Vika", "vika@test.ru");
        Item item = new Item(1L, owner, null, "Пила", "Пилит", true);
        Booking lastBooking = new Booking(1L, booker, item, LocalDateTime.now().minusDays(4),
                LocalDateTime.now().minusDays(3), BookingStatus.APPROVED);
        Booking nextBooking = new Booking(1L, booker, item, LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(6), BookingStatus.APPROVED);

        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(new Item(1L,
                        new User(1L, "Alex", "alex@test.com"),
                        null, "Бетономешалка", "Мешает бетон", true));
        Mockito
                .when(commentRepository.findByItemIdInOrderByCreated(Mockito.anyCollection()))
                .thenReturn(Collections.emptyList());
        Mockito
                .when(bookingRepository.findLastAndNearFutureBookingsByItemIn(anySet(), Mockito.any(LocalDateTime.class), Mockito.any(Sort.class)))
                .thenReturn(List.of(lastBooking, nextBooking));


        ItemDto itemDto = service.getItemById(1L, 1L);

        assertThat(itemDto.getId(), equalTo(1L));
        assertThat(itemDto.getOwnerId(), equalTo(1L));
        assertThat(itemDto.getRequestId(), nullValue());
        assertThat(itemDto.getName(), equalTo("Бетономешалка"));
        assertThat(itemDto.getDescription(), equalTo("Мешает бетон"));
        assertThat(itemDto.getAvailable(), equalTo(true));

        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findLastAndNearFutureBookingsByItemIn(anySet(), Mockito.any(LocalDateTime.class), Mockito.any(Sort.class));
        Mockito.verify(commentRepository, Mockito.times(1))
                .findByItemIdInOrderByCreated(Mockito.anyCollection());
    }

    @Test
    void testGetItemByIdOkByOwner() {
        ItemService service = makeItemService();

        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(new Item(1L,
                        new User(1L, "Alex", "alex@test.com"),
                        null, "Бетономешалка", "Мешает бетон", true));
        Mockito
                .when(bookingRepository.findLastAndNearFutureBookingsByItemIn(Mockito.anyCollection(),
                        Mockito.any(LocalDateTime.class), Mockito.any(Sort.class)))
                .thenReturn(Collections.emptyList());
        Mockito
                .when(commentRepository.findByItemIdInOrderByCreated(Mockito.anyCollection()))
                .thenReturn(Collections.emptyList());

        ItemDtoWithBookings itemDto = (ItemDtoWithBookings) service.getItemById(1L, 1L);

        assertThat(itemDto.getId(), equalTo(1L));
        assertThat(itemDto.getOwnerId(), equalTo(1L));
        assertThat(itemDto.getRequestId(), nullValue());
        assertThat(itemDto.getName(), equalTo("Бетономешалка"));
        assertThat(itemDto.getDescription(), equalTo("Мешает бетон"));
        assertThat(itemDto.getAvailable(), equalTo(true));
        assertThat(itemDto.getLastBooking(), nullValue());
        assertThat(itemDto.getNextBooking(), nullValue());

        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findLastAndNearFutureBookingsByItemIn(Mockito.anyCollection(),
                        Mockito.any(LocalDateTime.class), Mockito.any(Sort.class));
        Mockito.verify(commentRepository, Mockito.times(1))
                .findByItemIdInOrderByCreated(Mockito.anyCollection());
    }

    @Test
    void testGetItemByIdWithWrongItemId() {
        ItemService service = makeItemService();

        Mockito
                .when(itemRepository.getItemById(Mockito.anyLong()))
                .thenReturn(null);


        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.getItemById(99L, 2L));

        assertThat(exception.getMessage(), equalTo("Предмет c id - 99 не найден"));
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(99L);
        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verifyNoInteractions(commentRepository);
    }

    @Test
    void testAddItemOk() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Alex", "alex@test.ru");
        Item newItem = new Item(null, owner, null, "Бетономешалка", "Мешает бетон", true);
        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.save(newItem))
                .thenReturn(new Item(1L, owner, null, "Бетономешалка", "Мешает бетон", true));

        ItemDto itemDto = ItemDto.builder()
                .name("Бетономешалка")
                .description("Мешает бетон")
                .available(true)
                .build();

        ItemDto itemDtoAdd = service.addItem(1L, itemDto);
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(requestRepository);
        Mockito.verify(itemRepository, Mockito.times(1))
                .save(newItem);

        assertThat(itemDtoAdd.getId(), equalTo(1L));
        assertThat(itemDtoAdd.getAvailable(), equalTo(true));
        assertThat(itemDtoAdd.getOwnerId(), equalTo(1L));
    }

    @Test
    void testAddItemUserNotFound() {
        ItemService service = makeItemService();
        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        ItemDto itemDto = ItemDto.builder()
                .name("Пила")
                .description("Пилит")
                .available(true)
                .build();

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.addItem(1L, itemDto));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(requestRepository);
        Mockito.verifyNoInteractions(itemRepository);

        assertThat(exception.getMessage(), equalTo("Пользователь c id - 1 не найден"));
    }

    @Test
    void testAddItemRequestNotFound() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Alex", "alex@test.ru");

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));
        Mockito.when(requestRepository.findById(1L))
                .thenReturn(Optional.empty());

        ItemDto itemDto = ItemDto.builder()
                .name("Бетономешалка")
                .description("Мешает бетон")
                .available(true)
                .requestId(1L)
                .build();

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.addItem(1L, itemDto));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(requestRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(itemRepository);

        assertThat(exception.getMessage(), equalTo("Объект не найден"));
    }

    @Test
    void testUpdateItemOwnerNotFound() {
        ItemService service = makeItemService();
        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        ItemDto itemDto = ItemDto.builder()
                .name("Бетономешалка")
                .description("Мешает бетон")
                .available(true)
                .build();

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.updateItem(1L, 1L, itemDto));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(itemRepository);

        assertThat(exception.getMessage(), equalTo("Пользователь c id - 1 не найден"));
    }

    @Test
    void testUpdateItemNotFound() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Alex", "alex@test.ru");

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.getItemById(1L))
                .thenReturn(null);

        ItemDto itemDto = ItemDto.builder()
                .name("Бетономешалка")
                .description("Мешает бетон")
                .available(true)
                .build();

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.updateItem(1L, 1L, itemDto));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);

        assertThat(exception.getMessage(), equalTo("Предмет c id - 1 не найден"));
    }

    @Test
    void testUpdateItemNoRightsForUpdate() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Alex", "alex@test.ru");
        User user = new User(2L, "Vika", "vika@test.ru");
        Item item = new Item(1L, owner, null, "Бетономешалка", "Мешает бетон", true);
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.getItemById(1L))
                .thenReturn(item);

        ItemDto itemDto = ItemDto.builder()
                .name("Бетономешалка")
                .description("Мешает бетон")
                .available(true)
                .build();

        final NoRightsForUpdateException exception = Assertions.assertThrows(NoRightsForUpdateException.class,
                () -> service.updateItem(1L, 2L, itemDto));


        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);

        assertThat(exception.getMessage(), equalTo("Не достаточно прав для изменения!"));
    }

    @Test
    void testUpdateItemNewOwnerNotFound() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Alex", "alex@test.ru");
        Item item = new Item(1L, owner, null, "Бетономешалка", "Мешает бетон", true);
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));
        Mockito.when(itemRepository.getItemById(1L))
                .thenReturn(item);
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.empty());

        ItemDto itemDto = ItemDto.builder()
                .name("Бетономешалка")
                .description("Мешает бетон")
                .available(true)
                .ownerId(2L)
                .build();

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.updateItem(1L, 1L, itemDto));


        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);

        assertThat(exception.getMessage(), equalTo("Пользователь c id - 1 не найден"));
    }

    @Test
    void testUpdateItemOk() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Alex", "alex@test.ru");
        User newOwner = new User(2L, "Vika", "vika@test.ru");
        Item item = new Item(1L, owner, null, "Бетономешалка", "Мешает бетон", true);
        Item updatedItem = new Item(1L, newOwner, null, "Перфоратор", "Отличный", false);
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(newOwner));
        Mockito.when(itemRepository.getItemById(1L))
                .thenReturn(item);
        Mockito.when(itemRepository.save(updatedItem))
                .thenReturn(updatedItem);
        ItemDto itemDto = ItemDto.builder()
                .name("Перфоратор")
                .description("Отличный")
                .available(false)
                .ownerId(2L)
                .build();

        ItemDto updatedItemDto = service.updateItem(1L, 1L, itemDto);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .save(updatedItem);

        assertThat(updatedItemDto.getDescription(), equalTo("Отличный"));
    }

    @Test
    void searchItemsByPhraseEmptyPhrase() {
        ItemService service = makeItemService();


        List<ItemDto> items = service.searchItemsByPhrase("", 0, 1);
        assertThat(items, hasSize(0));

        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void searchItemsByPhraseOk() {
        ItemService service = makeItemService();
        User owner = new User(1L, "Alex", "alex@test.ru");
        Item item = new Item(1L, owner, null, "Бетономешалка", "Мешает бетон", true);
        Mockito.when(itemRepository.findByNameOrDescription(anyString(), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(item)));

        List<ItemDto> items = service.searchItemsByPhrase("Мешает бетон", 0, 1);
        assertThat(items, hasSize(1));
        assertThat(items, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("ownerId", equalTo(item.getOwner().getId())),
                hasProperty("name", equalTo(item.getName())),
                hasProperty("description", equalTo(item.getDescription())),
                hasProperty("available", equalTo(item.getAvailable()))
        )));

        Mockito.verify(itemRepository, Mockito.times(1))
                .findByNameOrDescription(anyString(), Mockito.any(PageRequest.class));
    }

    @Test
    void addCommentItemNotFound() {
        ItemService service = makeItemService();

        Mockito.when(itemRepository.getItemById(1L))
                .thenReturn(null);

        CommentDto commentDto = CommentDto.builder()
                .text("Точно отличный")
                .build();
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.addComment(1L, 1L, commentDto));

        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verifyNoInteractions(commentRepository);
        Mockito.verifyNoInteractions(userRepository);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);

        assertThat(exception.getMessage(), equalTo("Объект не найден"));
    }

    @Test
    void addCommentUserNotFound() {
        ItemService service = makeItemService();

        User owner = new User(1L, "Alex", "alex@test.ru");
        Item item = new Item(1L, owner, null, "Бетономешалка", "Мешает бетон", true);
        Mockito.when(itemRepository.getItemById(1L))
                .thenReturn(item);
        Mockito.when(userRepository.findById(3L))
                .thenReturn(Optional.empty());
        CommentDto commentDto = CommentDto.builder()
                .text("Точно отличный")
                .build();
        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.addComment(1L, 3L, commentDto));

        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verifyNoInteractions(commentRepository);
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(3L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);

        assertThat(exception.getMessage(), equalTo("Объект не найден"));
    }

    @Test
    void addCommentBookingNotFound() {
        ItemService service = makeItemService();

        User owner = new User(1L, "Alex", "alex@test.ru");
        Item item = new Item(1L, owner, null, "Бетономешалка", "Мешает бетон", true);
        User commentator = new User(2L, "SuperCom", "sc@saw.ru");

        Mockito.when(itemRepository.getItemById(1L))
                .thenReturn(item);
        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(commentator));
        Mockito.when(bookingRepository.findByItemIdAndBookerIdAndStatusNotAndEndBefore(Mockito.anyLong(), Mockito.anyLong(),
                        Mockito.any(BookingStatus.class), Mockito.any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        CommentDto commentDto = CommentDto.builder()
                .text("Хорошо мешает}")
                .build();

        final BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                () -> service.addComment(1L, 2L, commentDto));

        Mockito.verifyNoInteractions(commentRepository);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByItemIdAndBookerIdAndStatusNotAndEndBefore(Mockito.anyLong(), Mockito.anyLong(),
                        Mockito.any(BookingStatus.class), Mockito.any(LocalDateTime.class));
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);

        assertThat(exception.getMessage(), equalTo("Нельзя оставить комментарий без бронирования"));
    }

    @Test
    void addCommentBookingOk() {
        ItemService service = makeItemService();

        User owner = new User(1L, "Alex", "alex@test.ru");
        User booker = new User(2L, "Vika", "vika@test.ru");
        Item item = new Item(1L, owner, null, "Бетономешалка", "Мешает бетон", true);
        User commentator = new User(2L, "SuperCom", "sc@saw.ru");
        Booking booking = new Booking(1L, booker, item, LocalDateTime.now().minusDays(4),
                LocalDateTime.now().minusDays(3), BookingStatus.APPROVED);
        Comment comment = new Comment(1L, item, booker, "Супер", LocalDateTime.now());

        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(item);
        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(commentator));
        Mockito
                .when(bookingRepository.findByItemIdAndBookerIdAndStatusNotAndEndBefore(Mockito.anyLong(), Mockito.anyLong(),
                        Mockito.any(BookingStatus.class), Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        Mockito
                .when(commentRepository.save(Mockito.any(Comment.class)))
                .thenReturn(comment);

        CommentDto commentDto = CommentDto.builder()
                .text("Точно отлично")
                .build();

        service.addComment(1L, 2L, commentDto);
        Mockito.verify(commentRepository, Mockito.times(1))
                .save(Mockito.any(Comment.class));
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByItemIdAndBookerIdAndStatusNotAndEndBefore(Mockito.anyLong(), Mockito.anyLong(),
                        Mockito.any(BookingStatus.class), Mockito.any(LocalDateTime.class));
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
    }

    private ItemService makeItemService() {
        return new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository, requestRepository);
    }
}
