package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.NewBookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class NewBookingDtoJsonTest {
    @Autowired
    private JacksonTester<NewBookingDto> json;

    @Test
    public void testInvalidNewBookingDto() throws Exception {
        NewBookingDto invalidDto = NewBookingDto.builder()
                .itemId(null)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .build();

        JsonContent<NewBookingDto> result = json.write(invalidDto);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isNull();
    }

    @Test
    public void testValidNewBookingDto() throws Exception {
        NewBookingDto validDto = NewBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        JsonContent<NewBookingDto> result = json.write(validDto);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
    }
}
