package lk.clinic.service.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentNumberGeneratorTest {

    @Mock
    private JdbcTemplate jdbc;

    private AppointmentNumberGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new AppointmentNumberGenerator(jdbc);
    }

    @Test
    @DisplayName("Should generate first appointment sequence number 0001 when count is 0")
    void testFirstSequenceNumber() {
        String todayPrefix = "APT-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(todayPrefix + "%"))).thenReturn(0);

        String generated = generator.next();

        assertEquals(todayPrefix + "0001", generated);
    }

    @Test
    @DisplayName("Should increment sequence number based on existing count")
    void testIncrementedSequenceNumber() {
        String todayPrefix = "APT-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(todayPrefix + "%"))).thenReturn(8);

        String generated = generator.next();

        assertEquals(todayPrefix + "0009", generated);
    }

    @Test
    @DisplayName("Should handle null count safely defaulting to 0001")
    void testNullCountFallback() {
        String todayPrefix = "APT-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(todayPrefix + "%"))).thenReturn(null);

        String generated = generator.next();

        assertEquals(todayPrefix + "0001", generated);
    }
}
