package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplUnitTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @Test
    void testAddBookingUserNotFound() {
        BookingService service = getBookingService();
        NewBookingDto newBookingDto = NewBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.add(newBookingDto, 1L));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void testAddBookingItemNotFound() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Alex", "alex@test.ru");

        NewBookingDto newBookingDto = NewBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));
        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(null);

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.add(newBookingDto, 2L));

        assertThat(exception.getMessage(), equalTo("Объект c id - 1 не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
    }

    @Test
    void testAddBookingOwnerCantBookOwnItem() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Alex", "alex@test.ru");
        User owner = new User(2L, "Alex", "alex@test.ru");
        Item item = new Item(null, owner, null, "Пила", "Пилит", true);

        NewBookingDto newBookingDto = NewBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));
        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(item);

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.add(newBookingDto, 2L));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
    }

    @Test
    void testAddBookingItemNotAvailable() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Alex", "alex@test.ru");
        User owner = new User(1L, "Serg", "serg@test.ru");
        Item item = new Item(null, owner, null, "Бетономешалка", "Мешает бетон", false);

        NewBookingDto newBookingDto = NewBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));
        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(item);

        final BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                () -> service.add(newBookingDto, 2L));

        assertThat(exception.getMessage(), equalTo("Предмет недоступен для бронирования"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
    }

    @Test
    void testAddBookingItemWithCrossedPeriods() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Alex", "alex@test.ru");
        User owner = new User(1L, "Serg", "serg@test.ru");
        Item item = new Item(null, owner, null, "Бетономешалка",
                "Мешает бетон", true);
        Booking notCrossedbooking = new Booking(1L, booker, item, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6), BookingStatus.WAITING);
        NewBookingDto newBookingDto = NewBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(5))
                .build();

        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));
        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(item);
        Mockito
                .when(bookingRepository.findByItemIdAndEndAfter(anyLong(), any()))
                .thenReturn(List.of(notCrossedbooking));

        final BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                () -> service.add(newBookingDto, 2L));
        assertThat(exception.getMessage(), equalTo("Периоды бронирования пересекаются с имеющимися"));
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
    }

    @Test
    void testAddBookingItemOk() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Alex", "alex@test.ru");
        User owner = new User(1L, "Serg", "serg@test.ru");
        Item item = new Item(null, owner, null, "Бетономешалка",
                "Мешает бетон", true);
        Booking booking = new Booking(1L, booker, item, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), BookingStatus.WAITING);
        Booking notCrossedbooking = new Booking(1L, booker, item, LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(6), BookingStatus.WAITING);
        NewBookingDto newBookingDto = NewBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        Mockito
                .when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));
        Mockito
                .when(itemRepository.getItemById(1L))
                .thenReturn(item);
        Mockito
                .when(bookingRepository.save(any()))
                .thenReturn(booking);
        Mockito
                .when(bookingRepository.findByItemIdAndEndAfter(anyLong(), any()))
                .thenReturn(List.of(notCrossedbooking));

        BookingDto bookingDto = service.add(newBookingDto, 2L);
        assertThat(bookingDto.getId(), equalTo(booking.getId()));
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(2L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .save(any());
        Mockito.verify(itemRepository, Mockito.times(1))
                .getItemById(1L);
    }

    @Test
    void testApproveBookingNotFound() {
        BookingService service = getBookingService();

        Mockito
                .when(bookingRepository.findByOwnerIdOrBookerId(1L, 1L))
                .thenReturn(null);

        final BadRequestException exception = Assertions.assertThrows(BadRequestException.class,
                () -> service.approveBooking(1L, true, 1L));

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByOwnerIdOrBookerId(1L, 1L);
        Mockito.verifyNoMoreInteractions(bookingRepository);

        assertThat(exception.getMessage(), equalTo("Бронирование не найдено"));
    }

    @Test
    void testApproveBookingOwnerEqualBooker() {
        BookingService service = getBookingService();
        User booker = new User(1L, "Ale", "alex@test.ru");
        User owner = new User(1L, "Alex", "alex@test.ru");
        Item item = new Item(null, owner, null, "Бетономешалка",
                "Мешает бетон", false);
        Booking booking = Booking.builder()
                .id(1L)
                .booker(booker)
                .item(item)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        Mockito
                .when(bookingRepository.findByOwnerIdOrBookerId(1L, 1L))
                .thenReturn(booking);

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.approveBooking(1L, true, 1L));

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByOwnerIdOrBookerId(1L, 1L);
        Mockito.verifyNoMoreInteractions(bookingRepository);

        assertThat(exception.getMessage(), equalTo("Объект c id - 1 не найден"));
    }

    @Test
    void testApproveBookingStatusNotWaiting() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Alex", "alex@test.ru");
        User owner = new User(1L, "Serg", "serg@test.ru");
        Item item = new Item(null, owner, null, "Бетономешалка",
                "Мешает бетон", false);
        Booking booking = Booking.builder()
                .id(1L)
                .booker(booker)
                .item(item)
                .status(BookingStatus.REJECTED)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        Mockito
                .when(bookingRepository.findByOwnerIdOrBookerId(1L, 1L))
                .thenReturn(booking);

        final ValidationException exception = Assertions.assertThrows(ValidationException.class,
                () -> service.approveBooking(1L, true, 1L));

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByOwnerIdOrBookerId(1L, 1L);
        Mockito.verifyNoMoreInteractions(bookingRepository);

        assertThat(exception.getMessage(), equalTo("Введены невалидные данные"));
    }

    @Test
    void testApproveBookingOk() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Alex", "alex@test.ru");
        User owner = new User(1L, "Serg", "serg@test.ru");
        Item item = new Item(null, owner, null, "Бетономешалка",
                "Мешает бетон", false);
        Booking booking = Booking.builder()
                .id(1L)
                .booker(booker)
                .item(item)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        Booking bookingApproved = Booking.builder()
                .id(1L)
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        Mockito
                .when(bookingRepository.findByOwnerIdOrBookerId(1L, 1L))
                .thenReturn(booking);
        Mockito
                .when(bookingRepository.save(any()))
                .thenReturn(bookingApproved);

        BookingDto bookingDto = service.approveBooking(1L, true, 1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByOwnerIdOrBookerId(1L, 1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .save(any());

        assertThat(bookingDto.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void testFindAllByBookerIdAndBookingStateUserNotFound() {
        BookingService service = getBookingService();
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.findAllByBookerIdAndBookingState(1L, BookingState.ALL));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(bookingRepository);
    }

    @Test
    void testFindAllByBookerIdAndBookingStateWithStateAll() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Alex", "alex@test.ru");

        Mockito.when(userRepository.findById(2L))
                .thenReturn(Optional.of(booker));

        Mockito.when(bookingRepository.findAllByBookerIdOrderByStartDesc(anyLong()))
                .thenReturn(List.of());

        service.findAllByBookerIdAndBookingState(2L, BookingState.ALL);

        Mockito.verify(userRepository, Mockito.times(1)).findById(2L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdOrderByStartDesc(anyLong());
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByBookerIdAndBookingStateWithStateCurrent() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Alex", "aleex@test.ru");

        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepository.findAllBookerCurrentBookings(
                anyLong(), any(LocalDateTime.class), any(Sort.class)
        )).thenReturn(List.of());

        service.findAllByBookerIdAndBookingState(2L, BookingState.CURRENT);

        Mockito.verify(userRepository, Mockito.times(1)).findById(2L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllBookerCurrentBookings(anyLong(), any(LocalDateTime.class), any(Sort.class));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByBookerIdAndBookingStateWithStatePast() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Alex", "alex@test.ru");

        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(
                        anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of());

        service.findAllByBookerIdAndBookingState(2L, BookingState.PAST);

        Mockito.verify(userRepository, Mockito.times(1)).findById(2L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(LocalDateTime.class));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByBookerIdAndBookingStateWithStateFuture() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Alex", "alex@test.ru");

        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(
                        anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of());

        service.findAllByBookerIdAndBookingState(2L, BookingState.FUTURE);

        Mockito.verify(userRepository, Mockito.times(1)).findById(2L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByBookerIdAndBookingStateWithStateWaiting() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Alex", "alex@test.ru");
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        Mockito.when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        2L, BookingStatus.WAITING))
                .thenReturn(List.of());

        service.findAllByBookerIdAndBookingState(2L, BookingState.WAITING);

        Mockito.verify(userRepository, Mockito.times(1)).findById(2L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndStatusOrderByStartDesc(2L, BookingStatus.WAITING);
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByBookerIdAndBookingStateWithStateRejected() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Alex", "alex@test.ru");
        User owner = new User(1L, "Serg", "serg@test.ru");
        Item item = new Item(null, owner, null, "Бетономешалка",
                "Мешает бетон", false);
        Booking booking = Booking.builder()
                .id(1L)
                .booker(booker)
                .item(item)
                .status(BookingStatus.REJECTED)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        Mockito.when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        2L, BookingStatus.REJECTED))
                .thenReturn(List.of(booking));

        service.findAllByBookerIdAndBookingState(2L, BookingState.REJECTED);

        Mockito.verify(userRepository, Mockito.times(1)).findById(2L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndStatusOrderByStartDesc(2L, BookingStatus.REJECTED);
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByOwnerIdAndBookingStateUserNotFound() {
        BookingService service = getBookingService();
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.findAllByOwnerIdAndBookingState(1L, BookingState.ALL));

        assertThat(exception.getMessage(), equalTo("Объект не найден"));

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verifyNoInteractions(bookingRepository);
    }

    @Test
    void testFindAllByOwnerIdAndBookingStateWithStateAll() {
        BookingService service = getBookingService();
        User owner = new User(1L, "Alex", "alex@test.ru");

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(anyLong()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        service.findAllByOwnerIdAndBookingState(1L, BookingState.ALL);

        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1)).findAllByItemOwnerIdOrderByStartDesc(anyLong());
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByOwnerIdAndBookingStateWithStateCurrent() {
        BookingService service = getBookingService();
        User owner = new User(1L, "Alex", "alex@test.ru");

        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));
        Mockito
                .when(bookingRepository.findAllOwnerCurrentBookings(anyLong(),
                        any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(new PageImpl<>(Mockito.anyList()));

        service.findAllByOwnerIdAndBookingState(1L, BookingState.CURRENT);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllOwnerCurrentBookings(anyLong(), any(LocalDateTime.class), any(Sort.class));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByOwnerIdAndBookingStateWithStatePast() {
        BookingService service = getBookingService();
        User owner = new User(1L, "Alex", "alex@test.ru");

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(
                        anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of());

        service.findAllByOwnerIdAndBookingState(1L, BookingState.PAST);

        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(LocalDateTime.class));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByOwnerIdAndBookingStateWithStateFuture() {
        BookingService service = getBookingService();
        User owner = new User(1L, "Alex", "alex@test.ru");

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(
                        anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of());

        service.findAllByOwnerIdAndBookingState(1L, BookingState.FUTURE);

        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class));
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByOwnerIdAndBookingStateWithStateWaiting() {
        BookingService service = getBookingService();
        User owner = new User(1L, "Alex", "alex@test.ru");

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                        1L, BookingStatus.WAITING))
                .thenReturn(List.of());

        service.findAllByOwnerIdAndBookingState(1L, BookingState.WAITING);

        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(1L, BookingStatus.WAITING);
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindAllByOwnerIdAndBookingStateWithStateRejected() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Alex", "alex@test.ru");
        User owner = new User(1L, "Serg", "serg@test.ru");
        Item item = new Item(null, owner, null, "Бетономешалка",
                "Мешает бетон", false);
        Booking booking = Booking.builder()
                .id(1L)
                .booker(booker)
                .item(item)
                .status(BookingStatus.REJECTED)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Mockito.when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                        1L, BookingStatus.REJECTED))
                .thenReturn(List.of(booking));

        service.findAllByOwnerIdAndBookingState(1L, BookingState.REJECTED);

        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(1L, BookingStatus.REJECTED);
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testFindByOwnerIdOrBookerIdBookingNotFound() {
        BookingService service = getBookingService();

        Mockito
                .when(bookingRepository.findByOwnerIdOrBookerId(1L, 1L))
                .thenReturn(null);

        final NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> service.findByOwnerIdOrBookerId(1L, 1L));

        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByOwnerIdOrBookerId(1L, 1L);
        Mockito.verifyNoMoreInteractions(bookingRepository);

        assertThat(exception.getMessage(), equalTo("Объект c id - 1 не найден"));
    }

    @Test
    void testFindByOwnerIdOrBookerIdOk() {
        BookingService service = getBookingService();
        User booker = new User(2L, "Alex", "alex@test.ru");
        User owner = new User(1L, "Serg", "serg@test.ru");
        Item item = new Item(null, owner, null, "Бетономешалка",
                "Мешает бетон", false);
        Booking booking = Booking.builder()
                .id(1L)
                .booker(booker)
                .item(item)
                .status(BookingStatus.REJECTED)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        Mockito
                .when(bookingRepository.findByOwnerIdOrBookerId(1L, 1L))
                .thenReturn(booking);

        BookingDto bookingDto = service.findByOwnerIdOrBookerId(1L, 1L);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findByOwnerIdOrBookerId(1L, 1L);
        Mockito.verifyNoMoreInteractions(bookingRepository);

        assertThat(bookingDto.getId(), equalTo(booking.getId()));
    }

    private BookingService getBookingService() {
        return new BookingServiceImpl(bookingRepository, userRepository, itemRepository);
    }
}
