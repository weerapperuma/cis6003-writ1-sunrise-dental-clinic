package lk.clinic.service.dto;

public record AppointmentRegistrationRequest (
        String patientName,
        String address,
        String contactNumber,
        int dentistId,
        int treatmentId,
        String appointmentDate,
        String appointmentTime
){
}
