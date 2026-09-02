package lk.clinic.service.repository;

import lk.clinic.service.model.Appointment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

@Repository
public class AppointmentRepository {

    private final JdbcTemplate jdbc;

    public AppointmentRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public boolean existsByDentistAndDateTime(int dentistId, LocalDate date, LocalTime time) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND appointment_date = ? " +
                        "AND appointment_time = ? AND status <> 'CANCELLED'",
                Integer.class, dentistId, date, time);
        return count != null && count > 0;
    }

    public void save(Appointment appointment) {
        jdbc.update(
                "INSERT INTO appointments (appointment_number, patient_id, dentist_id, treatment_id, " +
                        "created_by, appointment_date, appointment_time, status) VALUES (?,?,?,?,?,?,?,?)",
                appointment.appointmentNumber(),
                appointment.patientId(),
                appointment.dentistId(),
                appointment.treatmentId(),
                appointment.createdBy(),
                appointment.appointmentDate(),
                appointment.appointmentTime(),
                appointment.status());
    }
}