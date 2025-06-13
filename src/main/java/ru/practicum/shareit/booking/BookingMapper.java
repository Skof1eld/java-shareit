package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

public class BookingMapper {

    public static BookingDto mapToBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .booker(UserMapper.mapToDto(booking.getBooker()))
                .item(ItemMapper.mapItemToItemDto(booking.getItem()))
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .build();
    }

    public static ShortBookingDto mapToShortBookingDto(Booking booking) {
        return ShortBookingDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .item(ItemMapper.mapItemToItemDto(booking.getItem()))
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .build();
    }

    public static List<BookingDto> mapToBookingDto(Iterable<Booking> bookings) {
        List<BookingDto> bookingsDto = new ArrayList<>();
        for (Booking booking : bookings) {
            bookingsDto.add(BookingMapper.mapToBookingDto(booking));
        }
        return bookingsDto;
    }

    public static Booking mapToBooking(BookingDto bookingDto, UserDto userDto, ItemDto itemDto) {
        return Booking.builder()
                .id(bookingDto.getId())
                .booker(UserMapper.mapToEntity(userDto))
                .item(ItemMapper.mapItemDtoToItem(itemDto, null))
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .status(bookingDto.getStatus())
                .build();
    }

    public static Booking mapToBooking(NewBookingDto newBookingDto, User booker, Item item) {
        return Booking.builder()
                .booker(booker)
                .item(item)
                .start(newBookingDto.getStart())
                .end(newBookingDto.getEnd())
                .status(BookingStatus.WAITING)
                .build();
    }
}
