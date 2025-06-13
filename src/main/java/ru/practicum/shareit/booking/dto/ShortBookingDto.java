package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShortBookingDto {
    Long id;
    Long bookerId;
    ItemDto item;
    LocalDateTime start;
    LocalDateTime end;
    BookingStatus status;
}
