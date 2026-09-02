package lk.clinic.service.repository;

import lk.clinic.service.model.Patient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class PatientRepository {

    private final JdbcTemplate jdbc;

    public PatientRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public int save(Patient patient) {
        KeyHolder holder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO patients (patient_name, address, contact_number) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, patient.patientName());
            ps.setString(2, patient.address());
            ps.setString(3, patient.contactNumber());
            return ps;
        }, holder);
        return holder.getKey().intValue();
    }
}