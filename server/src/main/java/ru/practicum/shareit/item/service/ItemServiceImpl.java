package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NoRightsForUpdateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.comments.Comment;
import ru.practicum.shareit.item.comments.CommentDto;
import ru.practicum.shareit.item.comments.CommentMapper;
import ru.practicum.shareit.item.comments.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;


    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.getItemById(itemId);
        if (item == null) {
            throw new NotFoundException("Предмет", itemId);
        }
        ItemDto itemDto;
        List<Booking> bookings;
        if (Objects.equals(item.getOwner().getId(), userId)) {
            Sort sort = Sort.by("start").ascending();
            bookings = bookingRepository.findLastAndNearFutureBookingsByItemIn(Set.of(itemId), LocalDateTime.now(), sort);
        } else {
            bookings = Collections.emptyList();
        }
        List<CommentDto> comments = CommentMapper.mapToCommentDto(commentRepository.findByItemIdInOrderByCreated(Set.of(itemId)));
        itemDto = ItemMapper.mapToItemDtoWithBookings(item, bookings, comments);

        return itemDto;
    }

    @Override
    public List<ItemDto> findItemsByOwnerId(Long ownerId, int from, int size) {
        List<Item> items;

        PageRequest page = PageRequest.of(from / size, size);
        items = itemRepository.findByOwnerIdOrderById(ownerId, page)
                .getContent();
        List<ItemDto> itemsWithBookings;
        Sort sort = Sort.by("start").ascending();
        Collection<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toSet());
        Map<Long, List<Booking>> bookings = bookingRepository.findLastAndNearFutureBookingsByItemIn(
                        itemIds, LocalDateTime.now(), sort)
                .stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));
        Map<Long, List<Comment>> comments = commentRepository.findByItemIdInOrderByCreated(itemIds).stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));
        itemsWithBookings = ItemMapper.mapToItemDtoWithBookings(items, bookings, comments);
        return itemsWithBookings;
    }

    @Override
    public List<ItemDto> searchItemsByPhrase(String searchPhrase, int from, int size) {
        if (searchPhrase == null || searchPhrase.trim().isEmpty()) {
            return Collections.emptyList();
        }
        PageRequest page = PageRequest.of(from / size, size);

        return itemRepository.findByNameOrDescription(searchPhrase, page)
                .stream()
                .map(ItemMapper::mapItemToItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ItemDto addItem(Long ownerId, ItemDto itemDto) {
        Optional<User> owner = userRepository.findById(ownerId);
        if (owner.isEmpty()) {
            throw new NotFoundException("Пользователь", ownerId);
        }
        ItemRequest itemRequest = null;
        if (itemDto.getRequestId() != null) {
            itemRequest = requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(NotFoundException::new);
        }
        Item item = ItemMapper.mapItemDtoToItem(itemDto, owner.get(), itemRequest);
        itemDto = ItemMapper.mapItemToItemDto(itemRepository.save(item));
        log.info("Предмет с идентификатором {} был добавлен для пользователя {} был создан", item.getId(), ownerId);
        return itemDto;
    }

    @Transactional
    @Override
    public ItemDto updateItem(Long itemId, Long ownerId, ItemDto itemDto) {
        Optional<User> owner = userRepository.findById(ownerId);
        if (owner.isEmpty()) {
            throw new NotFoundException("Пользователь", ownerId);
        }
        Item item = itemRepository.getItemById(itemId);
        if (item == null) {
            throw new NotFoundException("Предмет", itemId);
        }
        if (!Objects.equals(owner.get().getId(), item.getOwner().getId())) {
            throw new NoRightsForUpdateException();
        }
        Optional<User> newOwner = Objects.equals(ownerId, itemDto.getOwnerId()) || itemDto.getOwnerId() == null
                ? owner : userRepository.findById(itemDto.getOwnerId());
        if (newOwner.isEmpty()) {
            throw new NotFoundException("Пользователь", ownerId);
        }
        ItemMapper.mapItemDtoToItemForUpdate(itemDto, item, newOwner.get());
        itemDto = ItemMapper.mapItemToItemDto(itemRepository.save(item));
        log.info("Данные предмета с идентификатором {} были обновлены", item.getId());
        return itemDto;
    }

    @Transactional
    @Override
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        Item item = itemRepository.getItemById(itemId);
        if (item == null) {
            throw new NotFoundException();
        }
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException();
        }
        List<Booking> bookings = bookingRepository.findByItemIdAndBookerIdAndStatusNotAndEndBefore(
                itemId, userId, BookingStatus.REJECTED, LocalDateTime.now());
        if (bookings.isEmpty()) {
            throw new BadRequestException("Нельзя оставить комментарий без бронирования");
        }
        Comment comment = commentRepository.save(CommentMapper.mapToComment(commentDto, item, user.get()));
        return CommentMapper.mapToCommentDto(comment);
    }
}
