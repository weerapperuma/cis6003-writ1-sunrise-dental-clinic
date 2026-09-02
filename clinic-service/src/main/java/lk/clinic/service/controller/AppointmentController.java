package lk.clinic.service.controller;

import jakarta.servlet.http.HttpSession;
import lk.clinic.service.dto.AppointmentRegistrationRequest;
import lk.clinic.service.dto.AppointmentResponse;
import lk.clinic.service.dto.AppointmentSummary;
import lk.clinic.service.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> register(
            @RequestBody AppointmentRegistrationRequest request,
            HttpSession session) {

        // Security: only logged-in staff may register (TC-15 session use)
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) {
            return ResponseEntity.status(401).body(new AppointmentResponse(
                    false, "Login required to register appointments.", null, List.of()));
        }

        AppointmentResponse response = appointmentService.register(request, username);

        if (response.success())            return ResponseEntity.status(201).body(response);
        if (response.errors().isEmpty())   return ResponseEntity.status(409).body(response);
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(name = "date", required = false) String date,
            @RequestParam(name = "dentistId", required = false) Integer dentistId,
            @RequestParam(name = "patientName", required = false) String patientName,
            HttpSession session) {

        if (session.getAttribute("loggedInUser") == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Login required to search appointments."));
        }
        List<AppointmentSummary> results = appointmentService.search(date, dentistId, patientName);
        return ResponseEntity.ok(Map.of("success", true, "count", results.size(), "appointments", results));
    }
}