package lk.clinic.service.service;

import lk.clinic.service.dto.AppointmentRegistrationRequest;
import lk.clinic.service.dto.AppointmentResponse;
import lk.clinic.service.dto.AppointmentSummary;
import lk.clinic.service.model.Appointment;
import lk.clinic.service.model.Patient;
import lk.clinic.service.model.User;
import lk.clinic.service.repository.AppointmentRepository;
import lk.clinic.service.repository.PatientRepository;
import lk.clinic.service.repository.UserRepository;
import lk.clinic.service.util.AppointmentNumberGenerator;
import lk.clinic.service.validation.AppointmentValidator;
import lk.clinic.service.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentValidator validator;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private AppointmentNumberGenerator numberGenerator;
    @Mock
    private UserRepository userRepository;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(
                validator,
                patientRepository,
                appointmentRepository,
                numberGenerator,
                userRepository
        );
    }

    @Test
    @DisplayName("Should register appointment successfully when inputs and slot are valid")
    void testSuccessfulAppointmentRegistration() {
        AppointmentRegistrationRequest req = new AppointmentRegistrationRequest(
                "Sunil Fernando", "Colombo 07", "0712345678", 1, 2, "2026-09-10", "14:00"
        );

        when(validator.validate(req)).thenReturn(new ValidationResult());
        when(appointmentRepository.existsByDentistAndDateTime(eq(1), eq(LocalDate.parse("2026-09-10")), eq(LocalTime.parse("14:00"))))
                .thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(15);
        when(numberGenerator.next()).thenReturn("APT-20260910-0001");
        when(userRepository.findByUsername("reception"))
                .thenReturn(new User(2, "reception", "hash", "Staff", "RECEPTIONIST", true));

        AppointmentResponse res = appointmentService.register(req, "reception");

        assertTrue(res.success());
        assertEquals("APT-20260910-0001", res.appointmentNumber());
        verify(patientRepository, times(1)).save(any(Patient.class));
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should return validation errors and reject registration without calling DB")
    void testValidationFailure() {
        AppointmentRegistrationRequest req = new AppointmentRegistrationRequest(
                "", "", "invalid", 0, 0, "2020-01-01", "20:00"
        );

        ValidationResult failedResult = new ValidationResult();
        failedResult.addError("Patient name is required.");
        failedResult.addError("Contact number must be 10 digits starting with 0.");

        when(validator.validate(req)).thenReturn(failedResult);

        AppointmentResponse res = appointmentService.register(req, "reception");

        assertFalse(res.success());
        assertEquals("Validation failed", res.message());
        assertEquals(2, res.errors().size());
        verifyNoInteractions(patientRepository);
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    @DisplayName("Should return conflict when dentist is already booked at selected date/time")
    void testDoubleBookingConflict() {
        AppointmentRegistrationRequest req = new AppointmentRegistrationRequest(
                "Nimal Perera", "Colombo 03", "0771234567", 1, 1, "2026-09-12", "10:00"
        );

        when(validator.validate(req)).thenReturn(new ValidationResult());
        when(appointmentRepository.existsByDentistAndDateTime(eq(1), eq(LocalDate.parse("2026-09-12")), eq(LocalTime.parse("10:00"))))
                .thenReturn(true);

        AppointmentResponse res = appointmentService.register(req, "reception");

        assertFalse(res.success());
        assertTrue(res.message().contains("already booked"));
        verifyNoInteractions(patientRepository);
    }

    @Test
    @DisplayName("Should catch DataAccessException and return conflict when database constraint fails")
    void testDatabaseTriggerConstraintConflict() {
        AppointmentRegistrationRequest req = new AppointmentRegistrationRequest(
                "Nimal Perera", "Colombo 03", "0771234567", 1, 1, "2026-09-15", "11:00"
        );

        when(validator.validate(req)).thenReturn(new ValidationResult());
        when(appointmentRepository.existsByDentistAndDateTime(anyInt(), any(), any())).thenReturn(false);
        when(patientRepository.save(any())).thenReturn(20);
        when(numberGenerator.next()).thenReturn("APT-20260915-0002");
        when(userRepository.findByUsername("admin"))
                .thenReturn(new User(1, "admin", "hash", "Admin", "ADMIN", true));
        doThrow(new DataIntegrityViolationException("Duplicate slot in DB"))
                .when(appointmentRepository).save(any(Appointment.class));

        AppointmentResponse res = appointmentService.register(req, "admin");

        assertFalse(res.success());
        assertTrue(res.message().contains("already booked"));
    }

    @Test
    @DisplayName("Should delegate search query parameters to appointmentRepository")
    void testSearchDelegation() {
        List<AppointmentSummary> mockList = List.of(
                new AppointmentSummary(5, "APT-20260902-0001", "Nimal Silva", "Dr. Sunila", "Scaling", "2026-09-02", "09:00", "SCHEDULED")
        );
        when(appointmentRepository.search("2026-09-02", 1, "Nimal")).thenReturn(mockList);

        List<AppointmentSummary> result = appointmentService.search("2026-09-02", 1, "Nimal");

        assertEquals(1, result.size());
        assertEquals("APT-20260902-0001", result.get(0).appointmentNumber());
        assertEquals(5, result.get(0).appointmentId());
    }
}
