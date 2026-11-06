package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void requestSerializesCreationTimeAndResponseItems() throws Exception {
        ItemRequestResponseDto response = new ItemRequestResponseDto();
        response.setId(17L);
        response.setName("Camera");
        response.setOwnerId(9L);
        ItemRequestDto request = new ItemRequestDto();
        request.setId(11L);
        request.setDescription("Need a camera");
        request.setCreated(LocalDateTime.of(2030, 4, 5, 10, 20, 30));
        request.setItems(List.of(response));

        assertThat(json.write(request)).extractingJsonPathStringValue("$.created")
                                       .isEqualTo("2030-04-05T10:20:30");
        assertThat(json.write(request)).extractingJsonPathStringValue("$.description")
                                       .isEqualTo("Need a camera");
        assertThat(json.write(request)).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(17);
        assertThat(json.write(request)).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(9);
    }

    @Test
    void requestDeserializesCreationDateAndAnswers() throws Exception {
        ItemRequestDto parsed = json.parseObject("{\"id\":11,\"description\":\"Need a camera\"," +
                "\"created\":\"2030-04-05T10:20:30\",\"items\":[{\"id\":17,\"name\":\"Camera\",\"ownerId\":9}]}");
        assertThat(parsed.getCreated()).isEqualTo(LocalDateTime.of(2030, 4, 5, 10, 20, 30));
        assertThat(parsed.getItems()).hasSize(1);
        assertThat(parsed.getItems().getFirst().getOwnerId()).isEqualTo(9L);
    }
}
