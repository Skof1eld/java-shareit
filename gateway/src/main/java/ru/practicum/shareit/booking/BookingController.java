package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.exception.ValidationException;

import java.time.LocalDateTime;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;
    private static final String USER_HEADER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestBody @Valid NewBookingDto newBookingDto,
                                             @RequestHeader(USER_HEADER_ID) Long bookerId) {
        validateBookingPeriod(newBookingDto);
        return bookingClient.addBooking(newBookingDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@PathVariable long bookingId, @RequestParam boolean approved,
                                                 @RequestHeader(USER_HEADER_ID) Long ownerId) {
        return bookingClient.approveBooking(bookingId, approved, ownerId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingInfo(@PathVariable long bookingId, @RequestHeader(USER_HEADER_ID) Long userId) {
        return bookingClient.getBookingInfo(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookerBookings(@RequestHeader(USER_HEADER_ID) Long bookerId,
                                                    @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                    @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                    @Positive @RequestParam(defaultValue = "10") int size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getBookerBookings(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@RequestHeader(USER_HEADER_ID) Long ownerId,
                                                   @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                   @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                   @Positive @RequestParam(defaultValue = "10") int size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getOwnerBookings(ownerId, state, from, size);
    }

    private void validateBookingPeriod(NewBookingDto newBookingDto) {
        LocalDateTime start = newBookingDto.getStart();
        LocalDateTime end = newBookingDto.getEnd();
        if (!start.isBefore(end)) {
            throw new ValidationException("Дата начала не может быть равна или позже даты конца");
        }
    }
}
