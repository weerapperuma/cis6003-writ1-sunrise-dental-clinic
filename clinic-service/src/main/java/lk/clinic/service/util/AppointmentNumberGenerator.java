package lk.clinic.service.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class AppointmentNumberGenerator {

    private final JdbcTemplate jdbc;

    public AppointmentNumberGenerator(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public String next() {
        String prefix = "APT-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM appointments WHERE appointment_number LIKE ?",
                Integer.class, prefix + "%");
        return prefix + String.format("%04d", (count == null ? 0 : count) + 1);
    }
}