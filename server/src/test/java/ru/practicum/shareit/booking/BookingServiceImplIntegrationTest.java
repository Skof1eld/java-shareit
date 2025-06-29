package ru.practicum.shareit.booking;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
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
public class BookingServiceImplIntegrationTest {
    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;

    @Test
    void testFindAllByBookerIdAndBookingState() {
        UserDto userDtoOne = UserDto.builder()
                .name("Serg")
                .email("serg@test.ru")
                .build();
        UserDto userDtoTwo = UserDto.builder()
                .name("Alex")
                .email("alex@test.ru")
                .build();
        UserDto createdUserOne = userService.createUser(userDtoOne);
        UserDto createdUserTwo = userService.createUser(userDtoTwo);

        ItemDto itemDtoOne = ItemDto.builder()
                .name("Бетономешалка")
                .description("Мешает бетон")
                .available(true)
                .build();
        ItemDto addedItemOne = itemService.addItem(createdUserOne.getId(), itemDtoOne);

        NewBookingDto newBookingDto = NewBookingDto.builder()
                .itemId(addedItemOne.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        BookingDto addedBooking = bookingService.add(newBookingDto, createdUserTwo.getId());

        List<BookingDto> bookings = bookingService.findAllByBookerIdAndBookingState(createdUserTwo.getId(),
                BookingState.ALL, 0, 10);

        assertThat(bookings, hasSize(1));
        assertThat(bookings, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("item", equalTo(addedBooking.getItem())),
                hasProperty("booker", equalTo(addedBooking.getBooker())),
                hasProperty("status", equalTo(addedBooking.getStatus())),
                hasProperty("start", equalTo(addedBooking.getStart())),
                hasProperty("end", equalTo(addedBooking.getEnd()))
        )));
    }
}
