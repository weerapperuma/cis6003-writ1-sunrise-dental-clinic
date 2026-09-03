package lk.clinic.service.validation;

import lk.clinic.service.dto.AppointmentRegistrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentValidatorTest {

    private AppointmentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AppointmentValidator();
    }

    private String futureDate() {
        return LocalDate.now().plusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Test
    @DisplayName("Should pass when all fields are completely valid")
    void testValidAppointmentRequest() {
        AppointmentRegistrationRequest req = new AppointmentRegistrationRequest(
                "Kamal Perera",
                "No 12, Kandy Road, Colombo",
                "0771234567",
                1,
                2,
                futureDate(),
                "10:30"
        );

        ValidationResult result = validator.validate(req);
        assertFalse(result.hasErrors(), "Expected no validation errors for valid request");
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should fail when patient name is null or blank")
    void testMissingPatientName() {
        AppointmentRegistrationRequest req = new AppointmentRegistrationRequest(
                "   ",
                "Valid Address",
                "0771234567",
                1,
                1,
                futureDate(),
                "09:00"
        );

        ValidationResult result = validator.validate(req);
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().contains("Patient name is required."));
    }

    @Test
    @DisplayName("Should fail when address is null or blank")
    void testMissingAddress() {
        AppointmentRegistrationRequest req = new AppointmentRegistrationRequest(
                "Kamal Perera",
                "",
                "0771234567",
                1,
                1,
                futureDate(),
                "09:00"
        );

        ValidationResult result = validator.validate(req);
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().contains("Patient address is required."));
    }

    @Test
    @DisplayName("Should fail when contact number is not 10 digits or does not start with 0")
    void testInvalidContactNumber() {
        // Less than 10 digits
        AppointmentRegistrationRequest req1 = new AppointmentRegistrationRequest(
                "Kamal Perera", "Valid Address", "077123", 1, 1, futureDate(), "09:00"
        );
        assertTrue(validator.validate(req1).getErrors().contains("Contact number must be 10 digits starting with 0."));

        // Does not start with 0
        AppointmentRegistrationRequest req2 = new AppointmentRegistrationRequest(
                "Kamal Perera", "Valid Address", "1771234567", 1, 1, futureDate(), "09:00"
        );
        assertTrue(validator.validate(req2).getErrors().contains("Contact number must be 10 digits starting with 0."));

        // Contains alphabetic characters
        AppointmentRegistrationRequest req3 = new AppointmentRegistrationRequest(
                "Kamal Perera", "Valid Address", "077ABC4567", 1, 1, futureDate(), "09:00"
        );
        assertTrue(validator.validate(req3).getErrors().contains("Contact number must be 10 digits starting with 0."));
    }

    @Test
    @DisplayName("Should fail when dentist or treatment is not selected")
    void testMissingDentistOrTreatment() {
        AppointmentRegistrationRequest req = new AppointmentRegistrationRequest(
                "Kamal Perera", "Valid Address", "0771234567", 0, -1, futureDate(), "09:00"
        );

        ValidationResult result = validator.validate(req);
        assertTrue(result.getErrors().contains("Please select a dentist."));
        assertTrue(result.getErrors().contains("Please select a treatment."));
    }

    @Test
    @DisplayName("Should fail when appointment date is in the past")
    void testPastAppointmentDate() {
        String pastDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        AppointmentRegistrationRequest req = new AppointmentRegistrationRequest(
                "Kamal Perera", "Valid Address", "0771234567", 1, 1, pastDate, "11:00"
        );

        ValidationResult result = validator.validate(req);
        assertTrue(result.getErrors().contains("Appointment date cannot be in the past."));
    }

    @Test
    @DisplayName("Should fail when appointment time is outside working hours (08:00 - 17:00)")
    void testTimeOutsideWorkingHours() {
        // Before 08:00
        AppointmentRegistrationRequest reqEarly = new AppointmentRegistrationRequest(
                "Kamal Perera", "Valid Address", "0771234567", 1, 1, futureDate(), "07:30"
        );
        assertTrue(validator.validate(reqEarly).getErrors().contains("Appointment time must be between 08:00 and 17:00."));

        // After 17:00
        AppointmentRegistrationRequest reqLate = new AppointmentRegistrationRequest(
                "Kamal Perera", "Valid Address", "0771234567", 1, 1, futureDate(), "17:30"
        );
        assertTrue(validator.validate(reqLate).getErrors().contains("Appointment time must be between 08:00 and 17:00."));
    }

    @Test
    @DisplayName("Should fail when date or time format is malformed")
    void testMalformedDateFormat() {
        AppointmentRegistrationRequest req = new AppointmentRegistrationRequest(
                "Kamal Perera", "Valid Address", "0771234567", 1, 1, "05/09/2026", "invalid-time"
        );

        ValidationResult result = validator.validate(req);
        assertTrue(result.getErrors().contains("Appointment date must be in yyyy-MM-dd format."));
        assertTrue(result.getErrors().contains("Appointment time must be in HH:mm format."));
    }
}
