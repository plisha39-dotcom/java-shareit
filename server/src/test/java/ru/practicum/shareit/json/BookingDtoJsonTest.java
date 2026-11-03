package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingBookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {
    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void bookingDatesAndNestedObjectsHaveStableJsonRepresentation() throws Exception {
        BookingDto dto = new BookingDto();
        dto.setId(23L);
        dto.setStart(LocalDateTime.of(2030, 3, 4, 12, 15, 30));
        dto.setEnd(LocalDateTime.of(2030, 3, 5, 12, 15, 30));
        dto.setStatus(Status.WAITING);
        BookingBookerDto booker = new BookingBookerDto();
        booker.setId(7L);
        dto.setBooker(booker);
        BookingItemDto item = new BookingItemDto();
        item.setId(5L);
        item.setName("Drill");
        dto.setItem(item);

        assertThat(json.write(dto)).extractingJsonPathStringValue("$.start")
                                   .isEqualTo("2030-03-04T12:15:30");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.end")
                                   .isEqualTo("2030-03-05T12:15:30");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(json.write(dto)).extractingJsonPathNumberValue("$.booker.id").isEqualTo(7);
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.item.name").isEqualTo("Drill");
    }

    @Test
    void jsonDatesDeserializeBackToLocalDateTime() throws Exception {
        BookingDto parsed = json.parseObject("{\"id\":23,\"start\":\"2030-03-04T12:15:30\"," +
                "\"end\":\"2030-03-05T12:15:30\",\"status\":\"WAITING\"}");
        assertThat(parsed.getStart()).isEqualTo(LocalDateTime.of(2030, 3, 4, 12, 15, 30));
        assertThat(parsed.getEnd()).isEqualTo(LocalDateTime.of(2030, 3, 5, 12, 15, 30));
    }
}
