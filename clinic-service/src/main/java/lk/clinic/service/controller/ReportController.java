package lk.clinic.service.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final JdbcTemplate jdbc;

    public ReportController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private boolean loggedIn(HttpSession session) {
        return session.getAttribute("loggedInUser") != null;
    }

    @GetMapping("/daily")
    public ResponseEntity<?> daily(@RequestParam(name = "date") String date, HttpSession session) {
        if (!loggedIn(session)) return ResponseEntity.status(401).body(Map.of("success", false));
        return ResponseEntity.ok(jdbc.queryForList(
                "SELECT appointment_time, appointment_number, patient_name, dentist_name, " +
                        "treatment_type, status FROM vw_daily_appointments " +
                        "WHERE appointment_date = ? ORDER BY appointment_time", LocalDate.parse(date)));
    }

    @GetMapping("/dentist")
    public ResponseEntity<?> dentist(@RequestParam(name = "dentistId") int dentistId,
                                     @RequestParam(name = "date", required = false) String date,
                                     HttpSession session) {
        if (!loggedIn(session)) return ResponseEntity.status(401).body(Map.of("success", false));
        if (date != null && !date.isBlank()) {
            return ResponseEntity.ok(jdbc.queryForList(
                    "SELECT a.appointment_time, a.appointment_number, p.patient_name, t.treatment_type, a.status " +
                            "FROM appointments a JOIN patients p ON p.patient_id = a.patient_id " +
                            "JOIN treatments t ON t.treatment_id = a.treatment_id " +
                            "WHERE a.dentist_id = ? AND a.appointment_date = ? ORDER BY a.appointment_time",
                    dentistId, LocalDate.parse(date)));
        }
        return ResponseEntity.ok(jdbc.queryForList(
                "SELECT a.appointment_date, a.appointment_time, a.appointment_number, p.patient_name, " +
                        "t.treatment_type, a.status FROM appointments a JOIN patients p ON p.patient_id = a.patient_id " +
                        "JOIN treatments t ON t.treatment_id = a.treatment_id " +
                        "WHERE a.dentist_id = ? ORDER BY a.appointment_date, a.appointment_time", dentistId));
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> revenue(HttpSession session) {
        if (!loggedIn(session)) return ResponseEntity.status(401).body(Map.of("success", false));
        return ResponseEntity.ok(jdbc.queryForList(
                "SELECT DATE(generated_at) AS day, COUNT(*) AS bills, SUM(total_amount) AS revenue " +
                        "FROM bills WHERE payment_status = 'PAID' GROUP BY DATE(generated_at) ORDER BY day"));
    }
}