package lk.clinic.service.service;

import lk.clinic.service.dto.AppointmentRegistrationRequest;
import lk.clinic.service.dto.AppointmentResponse;
import lk.clinic.service.dto.AppointmentSummary;
import lk.clinic.service.model.Appointment;
import lk.clinic.service.model.Patient;
import lk.clinic.service.repository.AppointmentRepository;
import lk.clinic.service.repository.PatientRepository;
import lk.clinic.service.repository.UserRepository;
import lk.clinic.service.util.AppointmentNumberGenerator;
import lk.clinic.service.validation.AppointmentValidator;
import lk.clinic.service.validation.ValidationResult;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentValidator validator;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentNumberGenerator numberGenerator;
    private final UserRepository userRepository;

    public AppointmentService(AppointmentValidator validator, PatientRepository patientRepository,
                              AppointmentRepository appointmentRepository,
                              AppointmentNumberGenerator numberGenerator,
                              UserRepository userRepository) {
        this.validator = validator;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.numberGenerator = numberGenerator;
        this.userRepository = userRepository;
    }

    @Transactional
    public AppointmentResponse register(AppointmentRegistrationRequest req, String username) {

        // 1. Server-side validation (Template Method pattern)
        ValidationResult result = validator.validate(req);
        if (result.hasErrors()) {
            return AppointmentResponse.invalid(result.getErrors());
        }

        LocalDate date = LocalDate.parse(req.appointmentDate());
        LocalTime time = LocalTime.parse(req.appointmentTime());

        // 2. Application-level double-booking check
        if (appointmentRepository.existsByDentistAndDateTime(req.dentistId(), date, time)) {
            return AppointmentResponse.conflict(
                    "Dentist is already booked for the selected date and time.");
        }

        try {
            // 3. DTO -> Model translation happens ONLY here
            Patient patient = new Patient(req.patientName(), req.address(), req.contactNumber());
            int patientId = patientRepository.save(patient);

            String number = numberGenerator.next();
            int createdBy = userRepository.findByUsername(username).userId();

            Appointment appointment = new Appointment(number, patientId, req.dentistId(),
                    req.treatmentId(), createdBy, date, time);
            appointmentRepository.save(appointment);

            return AppointmentResponse.ok(number);
        } catch (DataAccessException e) {
            // 4. DB trigger / UNIQUE constraint = final safety net
            return AppointmentResponse.conflict(
                    "Dentist is already booked for the selected date and time.");
        }
    }

    public List<AppointmentSummary> search(String date, Integer dentistId, String patientName) {
        return appointmentRepository.search(date, dentistId, patientName);
    }
}