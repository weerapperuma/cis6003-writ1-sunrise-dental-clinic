package lk.clinic.service.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record Appointment(
        int appointmentId,
        String appointmentNumber,
        int patientId,
        int dentistId,
        int treatmentId,
        int createdBy,
        LocalDate appointmentDate,
        LocalTime appointmentTime,
        String status) {

    // convenience constructor for creating a NEW appointment
    public Appointment(
            String appointmentNumber,
            int patientId,
            int dentistId,
            int treatmentId,
            int createdBy,
            LocalDate appointmentDate,
            LocalTime appointmentTime
    ) {
        this(
                0,
                appointmentNumber,
                patientId,
                dentistId,
                treatmentId,
                createdBy,
                appointmentDate,
                appointmentTime,
                "SCHEDULED"
        );
    }
}