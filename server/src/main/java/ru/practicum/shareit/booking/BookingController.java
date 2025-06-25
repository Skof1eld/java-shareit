package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    public BookingDto addBooking(@RequestBody @Valid NewBookingDto newBookingDto,
                                 @RequestHeader(USER_HEADER_ID) Long bookerId) {
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
                                              @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.findAllByBookerIdAndBookingState(bookerId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader(USER_HEADER_ID) Long ownerId,
                                             @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.findAllByOwnerIdAndBookingState(ownerId, state);
    }
}
