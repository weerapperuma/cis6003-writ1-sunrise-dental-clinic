package lk.clinic.service.validation;

import lk.clinic.service.dto.AppointmentRegistrationRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Component
public class AppointmentValidator extends AbstractValidator<AppointmentRegistrationRequest> {

    @Override
    protected void doValidate(AppointmentRegistrationRequest req, ValidationResult result) {
        if (req.patientName() == null || req.patientName().isBlank())
            result.addError("Patient name is required.");
        if (req.address() == null || req.address().isBlank())
            result.addError("Patient address is required.");
        if (req.contactNumber() == null || !req.contactNumber().matches("0\\d{9}"))
            result.addError("Contact number must be 10 digits starting with 0.");
        if (req.dentistId() <= 0)
            result.addError("Please select a dentist.");
        if (req.treatmentId() <= 0)
            result.addError("Please select a treatment.");

        if (req.appointmentDate() == null || req.appointmentDate().isBlank()) {
            result.addError("Appointment date is required.");
        } else {
            try {
                if (LocalDate.parse(req.appointmentDate()).isBefore(LocalDate.now()))
                    result.addError("Appointment date cannot be in the past.");
            } catch (DateTimeParseException e) {
                result.addError("Appointment date must be in yyyy-MM-dd format.");
            }
        }

        if (req.appointmentTime() == null || req.appointmentTime().isBlank()) {
            result.addError("Appointment time is required.");
        } else {
            try {
                LocalTime t = LocalTime.parse(req.appointmentTime());
                if (t.isBefore(LocalTime.of(8, 0)) || t.isAfter(LocalTime.of(17, 0)))
                    result.addError("Appointment time must be between 08:00 and 17:00.");
            } catch (DateTimeParseException e) {
                result.addError("Appointment time must be in HH:mm format.");
            }
        }
    }
}