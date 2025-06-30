package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;
    private static final String USER_HEADER_ID = "X-Sharer-User-Id";

    @PostMapping
    public BookingDto addBooking(@RequestBody NewBookingDto newBookingDto, @RequestHeader(USER_HEADER_ID) Long bookerId) {
        return bookingService.add(newBookingDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@PathVariable long bookingId, @RequestParam boolean approved,
                                     @RequestHeader(USER_HEADER_ID) Long ownerId) {
        return bookingService.approveBooking(bookingId, approved, ownerId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingInfo(@PathVariable long bookingId, @RequestHeader(USER_HEADER_ID) Long userId) {
        return bookingService.findByOwnerIdOrBookerId(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getBookerBookings(@RequestHeader(USER_HEADER_ID) Long bookerId,
                                              @RequestParam(required = false, defaultValue = "ALL") BookingState state,
                                              @RequestParam(defaultValue = "0") int from,
                                              @RequestParam(defaultValue = "10") int size) {
        return bookingService.findAllByBookerIdAndBookingState(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader(USER_HEADER_ID) Long ownerId,
                                             @RequestParam(required = false, defaultValue = "ALL") BookingState state,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        return bookingService.findAllByOwnerIdAndBookingState(ownerId, state, from, size);
    }
}
