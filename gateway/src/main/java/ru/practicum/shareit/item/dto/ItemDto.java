package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDto {
    Long id;
    Long ownerId;
    Long requestId;

    @NotBlank(message = "Name не может быть пустым")
    String name;

    @NotBlank(message = "Description не может быть пустым")
    String description;

    @NotNull(message = "Available обязательно")
    Boolean available;
}
