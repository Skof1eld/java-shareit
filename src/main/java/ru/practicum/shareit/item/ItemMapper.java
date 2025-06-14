package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.item.comments.Comment;
import ru.practicum.shareit.item.comments.CommentDto;
import ru.practicum.shareit.item.comments.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class ItemMapper {
    public ItemDto mapItemToItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .ownerId(item.getOwner().getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public Item mapItemDtoToItem(ItemDto itemDto, User owner) {
        return Item.builder()
                .id(itemDto.getId())
                .owner(owner)
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public void mapItemDtoToItemForUpdate(ItemDto itemDto, Item item, User owner) {
        if (itemDto.getOwnerId() != null) {
            item.setOwner(owner);
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
    }

    public static ItemDto mapToItemDtoWithBookings(Item item, List<Booking> bookings, List<CommentDto> comments) {
        ShortBookingDto lastBooking = null;
        ShortBookingDto nextBooking = null;
        if (bookings.size() > 1) {
            lastBooking = BookingMapper.mapToShortBookingDto(bookings.get(0));
            nextBooking = BookingMapper.mapToShortBookingDto(bookings.get(1));
        } else if (bookings.size() == 1) {
            LocalDateTime now = LocalDateTime.now();
            if (bookings.get(0).getStart().isAfter(now)) {
                nextBooking = BookingMapper.mapToShortBookingDto(bookings.get(0));
            } else {
                lastBooking = BookingMapper.mapToShortBookingDto(bookings.get(0));
            }
        }
        return new ItemDtoWithBookings(item.getId(), item.getOwner().getId(),
                item.getName(), item.getDescription(), item.getAvailable(), lastBooking, nextBooking, comments);
    }

    public static List<ItemDto> mapToItemDtoWithBookings(List<Item> items, Map<Long,
            List<Booking>> bookings, Map<Long, List<Comment>> comments) {
        return items.stream()
                .map(i -> ItemMapper.mapToItemDtoWithBookings(i, bookings.getOrDefault(i.getId(), new ArrayList<>()),
                        CommentMapper.mapToCommentDto(comments.getOrDefault(i.getId(), new ArrayList<>()))))
                .collect(Collectors.toList());
    }
}
