-- =================================================================
-- ADVANCED DATABASE FEATURES (Required for Excellent Band 70-100)
-- =================================================================

-- 1. Stored Function: Calculates the total bill amount
DELIMITER //
CREATE FUNCTION fn_calculate_total(p_fee DECIMAL(10,2), p_consult DECIMAL(10,2), p_disc DECIMAL(10,2))
    RETURNS DECIMAL(10,2)
    DETERMINISTIC
BEGIN
RETURN (p_fee + p_consult) - p_disc;
END //
DELIMITER ;

-- 2. Trigger: Auto-calculates total_amount before inserting a bill
DELIMITER //
CREATE TRIGGER trg_bills_before_insert
    BEFORE INSERT ON bills
    FOR EACH ROW
BEGIN
    SET NEW.total_amount = fn_calculate_total(NEW.treatment_fee, NEW.consultation_fee, NEW.discount);
END //
DELIMITER ;

-- 3. Trigger: Double-booking safety net (throws error if slot taken)
DELIMITER //
CREATE TRIGGER trg_prevent_double_booking
    BEFORE INSERT ON appointments
    FOR EACH ROW
BEGIN
    DECLARE slot_count INT;
    SELECT COUNT(*) INTO slot_count FROM appointments
    WHERE dentist_id = NEW.dentist_id
      AND appointment_date = NEW.appointment_date
      AND appointment_time = NEW.appointment_time
      AND status <> 'CANCELLED';

    IF slot_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Dentist already booked at this date and time.';
END IF;
END //
DELIMITER ;

-- 4. View: For the Daily Appointments Report feature
CREATE VIEW vw_daily_appointments AS
SELECT a.appointment_date, a.appointment_time, a.appointment_number,
       p.patient_name, d.dentist_name, t.treatment_type, a.status
FROM appointments a
         JOIN patients   p ON p.patient_id   = a.patient_id
         JOIN dentists   d ON d.dentist_id   = a.dentist_id
         JOIN treatments t ON t.treatment_id = a.treatment_id;
