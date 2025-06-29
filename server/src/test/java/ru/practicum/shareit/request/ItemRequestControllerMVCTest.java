package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@Import(ItemRequestControllerMVCTest.TestConfig.class)
public class ItemRequestControllerMVCTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ItemRequestService itemRequestService() {
            return Mockito.mock(ItemRequestService.class);
        }
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ItemRequestService requestService;
    private static final String USER_HEADER_ID = "X-Sharer-User-Id";

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final LocalDateTime created = LocalDateTime.parse(LocalDateTime.now().format(formatter));

    private final ItemRequestDtoWithItems requestDto = new ItemRequestDtoWithItems(1L,
            "Нужно чем нибудь просверлить отверстие", created, Collections.emptyList());

    @Test
    void testAddRequest() throws Exception {
        when(requestService.addRequest(any(), anyLong()))
                .thenReturn(requestDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER_ID, "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription()), String.class))
                .andExpect(jsonPath("$.created", is(requestDto.getCreated().format(formatter)), String.class));
    }

    @Test
    void testFindRequestById() throws Exception {
        when(requestService.findById(anyLong(), anyLong()))
                .thenReturn(requestDto);

        mvc.perform(get("/requests/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER_ID, "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription()), String.class))
                .andExpect(jsonPath("$.created", is(requestDto.getCreated().format(formatter)), String.class));
    }

    @Test
    void testFindAllUserRequests() throws Exception {
        when(requestService.findAll(anyLong()))
                .thenReturn(List.of(requestDto));

        mvc.perform(get("/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER_ID, "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestDto.getDescription()), String.class))
                .andExpect(jsonPath("$[0].created", is(requestDto.getCreated().format(formatter)), String.class));
    }

    @Test
    void testFindAllRequests() throws Exception {
        when(requestService.findAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(requestDto));

        mvc.perform(get("/requests/all")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_HEADER_ID, "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestDto.getDescription()), String.class))
                .andExpect(jsonPath("$[0].created", is(requestDto.getCreated().format(formatter)), String.class));
    }
}
