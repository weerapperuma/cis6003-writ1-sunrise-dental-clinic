package lk.clinic.service.dto;

import java.util.List;

public record AppointmentResponse(
        boolean success,
        String message,
        String appointmentNumber,
        List<String> errors
) {
    public static AppointmentResponse ok(String number) {
        return new AppointmentResponse(true, "Appointment registered successfully", number, List.of());
    }
    public static AppointmentResponse invalid(List<String> errors) {
        return new AppointmentResponse(false, "Validation failed", null, errors);
    }
    public static AppointmentResponse conflict(String msg) {
        return new AppointmentResponse(false, msg, null, List.of());
    }
}
