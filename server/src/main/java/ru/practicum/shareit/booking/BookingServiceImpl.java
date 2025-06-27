package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;

import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto add(NewBookingDto newBookingDto, long bookerId) {
        validateBookingPeriod(newBookingDto);
        User booker = doesUserExist(bookerId);
        Item item = itemRepository.getItemById(newBookingDto.getItemId());

        if (item == null) {
            throw new NotFoundException(newBookingDto.getItemId());
        }

        if (Objects.equals(booker.getId(), item.getOwner().getId())) {
            throw new NotFoundException();
        }

        if (!item.getAvailable()) {
            throw new BadRequestException("Предмет недоступен для бронирования");
        }
        checkCrossedPeriods(newBookingDto);
        Booking newBooking = BookingMapper.mapToBooking(newBookingDto, booker, item);
        return BookingMapper.mapToBookingDto(bookingRepository.save(newBooking));
    }

    @Override
    @Transactional
    public BookingDto approveBooking(long bookingId, boolean isApproved, long ownerId) {
        BookingStatus status = isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        Booking booking = bookingRepository.findByOwnerIdOrBookerId(bookingId, ownerId);

        if (booking == null) {
            throw new BadRequestException("Бронирование не найдено");
        } else if (booking.getBooker().getId() == ownerId) {
            throw new NotFoundException(bookingId);
        } else if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException();
        }
        booking.setStatus(status);

        return BookingMapper.mapToBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto findByOwnerIdOrBookerId(long bookingId, long userId) {
        Booking booking = bookingRepository.findByOwnerIdOrBookerId(bookingId, userId);
        if (booking == null) {
            throw new NotFoundException(bookingId);
        }
        return BookingMapper.mapToBookingDto(booking);
    }

    @Override
    public List<BookingDto> findAllByBookerIdAndBookingState(long bookerId, BookingState state) {
        List<BookingDto> bookingsDto = new ArrayList<>();
        doesUserExist(bookerId);
        switch (state) {
            case ALL:
                bookingsDto = BookingMapper.mapToBookingDto(bookingRepository
                        .findAllByBookerIdOrderByStartDesc(bookerId));
                break;
            case CURRENT:
                Sort sort = Sort.by("start").descending();
                bookingsDto = BookingMapper.mapToBookingDto(bookingRepository
                        .findAllBookerCurrentBookings(bookerId, LocalDateTime.now(), sort));
                break;
            case PAST:
                bookingsDto = BookingMapper.mapToBookingDto(bookingRepository
                        .findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerId, LocalDateTime.now()));
                break;
            case FUTURE:
                bookingsDto = BookingMapper.mapToBookingDto(bookingRepository
                        .findAllByBookerIdAndStartAfterOrderByStartDesc(bookerId, LocalDateTime.now()));
                break;
            case WAITING:
                bookingsDto = BookingMapper.mapToBookingDto(bookingRepository
                        .findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING));
                break;
            case REJECTED:
                bookingsDto = BookingMapper.mapToBookingDto(bookingRepository
                        .findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.REJECTED));
                break;
        }
        return bookingsDto;
    }

    @Override
    public List<BookingDto> findAllByOwnerIdAndBookingState(long ownerId, BookingState state) {
        List<BookingDto> bookingsDto = new ArrayList<>();
        doesUserExist(ownerId);
        switch (state) {
            case ALL:
                bookingsDto = BookingMapper.mapToBookingDto(bookingRepository
                        .findAllByItemOwnerIdOrderByStartDesc(ownerId));
                break;
            case CURRENT:
                Sort sort = Sort.by("start").descending();
                bookingsDto = BookingMapper.mapToBookingDto(bookingRepository
                        .findAllOwnerCurrentBookings(ownerId, LocalDateTime.now(), sort));
                break;
            case PAST:
                bookingsDto = BookingMapper.mapToBookingDto(bookingRepository
                        .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId, LocalDateTime.now()));
                break;
            case FUTURE:
                bookingsDto = BookingMapper.mapToBookingDto(bookingRepository
                        .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId, LocalDateTime.now()));
                break;
            case WAITING:
                bookingsDto = BookingMapper.mapToBookingDto(bookingRepository
                        .findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING));
                break;
            case REJECTED:
                bookingsDto = BookingMapper.mapToBookingDto(bookingRepository
                        .findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED));
                break;
        }
        return bookingsDto;
    }

    private void validateBookingPeriod(NewBookingDto newBookingDto) {
        LocalDateTime start = newBookingDto.getStart();
        LocalDateTime end = newBookingDto.getEnd();
        if (!start.isBefore(end)) {
            throw new ValidationException();
        }
    }

    private void checkCrossedPeriods(NewBookingDto newBookingDto) {
        LocalDateTime start = newBookingDto.getStart();
        LocalDateTime end = newBookingDto.getEnd();
        List<Booking> bookings = bookingRepository.findByItemIdAndEndAfter(newBookingDto.getItemId(), LocalDateTime.now());
        boolean haveCrossPeriods = bookings.stream()
                .anyMatch(b
                        -> start.isAfter(b.getStart()) && start.isBefore(b.getEnd())
                        || end.isAfter(b.getStart()) && end.isBefore(b.getEnd()));
        if (haveCrossPeriods) {
            throw new BadRequestException("Периоды бронирования пересекаются с имеющимися");
        }
    }

    private User doesUserExist(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException();
        }
        return user.get();
    }
}
