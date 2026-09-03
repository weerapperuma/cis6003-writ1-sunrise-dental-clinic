package lk.clinic.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.clinic.service.dto.AppointmentRegistrationRequest;
import lk.clinic.service.dto.AppointmentResponse;
import lk.clinic.service.dto.AppointmentSummary;
import lk.clinic.service.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private AppointmentService appointmentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AppointmentController controller = new AppointmentController(appointmentService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("POST /api/appointments - Should reject with 401 Unauthorized when unauthenticated")
    void testRegisterUnauthenticated() throws Exception {
        AppointmentRegistrationRequest req = new AppointmentRegistrationRequest(
                "Nimal Silva", "Colombo 03", "0771234567", 1, 1, "2026-09-10", "10:00"
        );

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Login required to register appointments."));
    }

    @Test
    @DisplayName("POST /api/appointments - Should return 201 Created on valid booking")
    void testRegisterSuccess() throws Exception {
        AppointmentRegistrationRequest req = new AppointmentRegistrationRequest(
                "Nimal Silva", "Colombo 03", "0771234567", 1, 1, "2026-09-10", "10:00"
        );
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", "reception");

        when(appointmentService.register(any(AppointmentRegistrationRequest.class), eq("reception")))
                .thenReturn(AppointmentResponse.ok("APT-20260910-0001"));

        mockMvc.perform(post("/api/appointments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.appointmentNumber").value("APT-20260910-0001"));
    }

    @Test
    @DisplayName("POST /api/appointments - Should return 400 Bad Request on validation failure")
    void testRegisterValidationFailure() throws Exception {
        AppointmentRegistrationRequest req = new AppointmentRegistrationRequest(
                "", "", "invalid", 0, 0, "2020-01-01", "22:00"
        );
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", "reception");

        when(appointmentService.register(any(AppointmentRegistrationRequest.class), eq("reception")))
                .thenReturn(AppointmentResponse.invalid(List.of(
                        "Patient name is required.",
                        "Contact number must be 10 digits starting with 0."
                )));

        mockMvc.perform(post("/api/appointments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/appointments - Should return 409 Conflict on double-booking collision")
    void testRegisterConflict() throws Exception {
        AppointmentRegistrationRequest req = new AppointmentRegistrationRequest(
                "Nimal Silva", "Colombo 03", "0771234567", 1, 1, "2026-09-10", "10:00"
        );
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", "reception");

        when(appointmentService.register(any(AppointmentRegistrationRequest.class), eq("reception")))
                .thenReturn(AppointmentResponse.conflict("Dentist is already booked for the selected date and time."));

        mockMvc.perform(post("/api/appointments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Dentist is already booked for the selected date and time."));
    }

    @Test
    @DisplayName("GET /api/appointments/search - Should reject with 401 when unauthenticated")
    void testSearchUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/appointments/search"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/appointments/search - Should return 200 with result count and appointment array")
    void testSearchAuthenticated() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", "admin");

        List<AppointmentSummary> mockList = List.of(
                new AppointmentSummary(5, "APT-20260902-0001", "Nimal Silva", "Dr. Sunila", "Scaling", "2026-09-02", "09:00", "SCHEDULED")
        );

        when(appointmentService.search(eq("2026-09-02"), isNull(), isNull())).thenReturn(mockList);

        mockMvc.perform(get("/api/appointments/search?date=2026-09-02")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.appointments[0].appointmentNumber").value("APT-20260902-0001"))
                .andExpect(jsonPath("$.appointments[0].appointmentId").value(5));
    }
}
