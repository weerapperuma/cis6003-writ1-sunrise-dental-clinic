package lk.clinic.service.dto;

public record AppointmentSummary(
        int appointmentId,
        String appointmentNumber,
        String patientName,
        String dentistName,
        String treatmentType,
        String appointmentDate,
        String appointmentTime,
        String status
) {}