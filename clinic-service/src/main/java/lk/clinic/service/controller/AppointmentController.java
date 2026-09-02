package lk.clinic.service.controller;

import jakarta.servlet.http.HttpSession;
import lk.clinic.service.dto.AppointmentRegistrationRequest;
import lk.clinic.service.dto.AppointmentResponse;
import lk.clinic.service.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}