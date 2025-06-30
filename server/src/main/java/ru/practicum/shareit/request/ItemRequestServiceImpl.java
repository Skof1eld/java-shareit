package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto addRequest(ItemRequestDto itemRequestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);
        ItemRequest itemRequest = requestRepository.save(ItemRequestMapper.mapToItemRequest(itemRequestDto, user));
        return ItemRequestMapper.mapToItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestDtoWithItems> findAll(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);
        Sort sort = Sort.by("created").descending();
        List<ItemRequest> itemRequests = requestRepository.findByRequesterId(userId, sort);
        Map<Long, List<Item>> items = itemRepository.findByRequestIdIn(itemRequests.stream()
                        .map(ItemRequest::getId)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.groupingBy(i -> i.getRequest().getId()));
        return ItemRequestMapper.mapToItemRequestDto(itemRequests, items);
    }

    @Override
    public List<ItemRequestDtoWithItems> findAll(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);
        List<ItemRequest> itemRequests;
        PageRequest page = PageRequest.of(from / size, size, Sort.by("created").descending());
        itemRequests = requestRepository.findByRequesterIdNot(userId, page)
                .getContent();
        Map<Long, List<Item>> items = itemRepository.findByRequestIdIn(itemRequests.stream()
                        .map(ItemRequest::getId)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.groupingBy(i -> i.getRequest().getId()));
        return ItemRequestMapper.mapToItemRequestDto(itemRequests, items);
    }

    @Override
    public ItemRequestDto findById(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);
        ItemRequest itemRequest = requestRepository.findById(requestId)
                .orElseThrow(NotFoundException::new);
        List<Item> items = itemRepository.findByRequestIdIn(Set.of(requestId));
        return ItemRequestMapper.mapToItemRequestDto(itemRequest, items);
    }
}
