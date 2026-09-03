package lk.clinic.service.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meta")
public class MetaController {

    private final JdbcTemplate jdbc;

    public MetaController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/dentists")
    public List<Map<String, Object>> dentists() {
        return jdbc.queryForList("SELECT dentist_id AS id, dentist_name AS name FROM dentists");
    }

    @GetMapping("/treatments")
    public List<Map<String, Object>> treatments() {
        return jdbc.queryForList(
                "SELECT treatment_id AS id, treatment_type AS name, treatment_fee AS fee, " +
                        "consultation_fee AS consultFee FROM treatments");
    }
}