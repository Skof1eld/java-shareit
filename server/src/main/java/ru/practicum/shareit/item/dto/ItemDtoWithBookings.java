package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.item.comments.CommentDto;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoWithBookings extends ItemDto {
    ShortBookingDto lastBooking;
    ShortBookingDto nextBooking;
    List<CommentDto> comments;

    public ItemDtoWithBookings(Long id, Long ownerId, Long requestId, String name, String description, Boolean available,
                               ShortBookingDto lastBooking, ShortBookingDto nextBooking, List<CommentDto> comments) {
        super(id, ownerId, requestId, name, description, available);
        this.lastBooking = lastBooking;
        this.nextBooking = nextBooking;
        this.comments = comments;
    }
}