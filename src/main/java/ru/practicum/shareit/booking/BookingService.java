package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;

import java.util.List;

public interface BookingService {
    BookingDto add(NewBookingDto newBookingDto, long bookerId);

    BookingDto approveBooking(long bookingId, boolean isApproved, long ownerId);

    BookingDto findByOwnerIdOrBookerId(long bookingId, long userId);

    List<BookingDto> findAllByBookerIdAndBookingState(long bookerId, BookingState state);

    List<BookingDto> findAllByOwnerIdAndBookingState(long ownerId, BookingState state);

}
