package lk.clinic.service.repository;

import lk.clinic.service.dto.AppointmentBillingInfo;
import lk.clinic.service.dto.AppointmentSummary;
import lk.clinic.service.model.Appointment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

    public List<AppointmentSummary> search(String date, Integer dentistId, String patientName) {
        StringBuilder sql = new StringBuilder(
                "SELECT a.appointment_id, a.appointment_number, p.patient_name, d.dentist_name, t.treatment_type, " +
                        "a.appointment_date, a.appointment_time, a.status " +
                        "FROM appointments a " +
                        "JOIN patients p   ON p.patient_id   = a.patient_id " +
                        "JOIN dentists d   ON d.dentist_id   = a.dentist_id " +
                        "JOIN treatments t ON t.treatment_id = a.treatment_id " +
                        "WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (date != null && !date.isBlank()) {
            sql.append(" AND a.appointment_date = ?");
            params.add(LocalDate.parse(date));
        }
        if (dentistId != null) {
            sql.append(" AND a.dentist_id = ?");
            params.add(dentistId);
        }
        if (patientName != null && !patientName.isBlank()) {
            sql.append(" AND p.patient_name LIKE ?");
            params.add("%" + patientName + "%");
        }
        sql.append(" ORDER BY a.appointment_date, a.appointment_time");

        return jdbc.query(sql.toString(), (rs, i) -> new AppointmentSummary(
                        rs.getInt("appointment_id"),
                        rs.getString("appointment_number"),
                        rs.getString("patient_name"),
                        rs.getString("dentist_name"),
                        rs.getString("treatment_type"),
                        rs.getDate("appointment_date").toLocalDate().toString(),
                        rs.getTime("appointment_time").toLocalTime().toString(),
                        rs.getString("status")),
                params.toArray());
    }

    public AppointmentBillingInfo findBillingInfo(int appointmentId) {
        List<AppointmentBillingInfo> list = jdbc.query(
                "SELECT a.appointment_id, a.appointment_number, t.treatment_fee, t.consultation_fee " +
                        "FROM appointments a JOIN treatments t ON t.treatment_id = a.treatment_id " +
                        "WHERE a.appointment_id = ?",
                (rs, i) -> new AppointmentBillingInfo(rs.getInt("appointment_id"),
                        rs.getString("appointment_number"),
                        rs.getBigDecimal("treatment_fee"),
                        rs.getBigDecimal("consultation_fee")),
                appointmentId);
        return list.isEmpty() ? null : list.get(0);
    }
}