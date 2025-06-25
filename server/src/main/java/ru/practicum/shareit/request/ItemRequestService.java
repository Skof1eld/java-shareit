package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;

import java.util.List;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addRequest(ItemRequestDto itemRequestDto, Long userId);

    List<ItemRequestDtoWithItems> findAll(Long userId);

    List<ItemRequestDtoWithItems> findAll(Long userId, int from, int size);

    ItemRequestDto findById(Long userId, Long requestId);
}
