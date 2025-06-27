package ru.practicum.shareit.request.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestDtoWithItems extends ItemRequestDto {
    List<ItemDto> items;

    public ItemRequestDtoWithItems(Long id, String text,
                                   LocalDateTime created, List<ItemDto> items) {
        super(id, text, created);
        this.items = items;
    }
}
