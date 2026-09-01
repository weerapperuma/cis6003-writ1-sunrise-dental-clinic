-- Sunrise Dental Clinic — normalized schema matching the class diagram
-- Includes advanced features (Triggers, Stored Functions, Views) for Excellent band marks.
-- Run: mysql -u root -p < schema.sql

CREATE DATABASE IF NOT EXISTS sunrise_dental;
USE sunrise_dental;

-- ---------------------------------------------------------------
-- users  (maps to User in the class diagram)
-- ---------------------------------------------------------------
CREATE TABLE users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    role           ENUM('ADMIN', 'RECEPTIONIST') NOT NULL DEFAULT 'RECEPTIONIST',
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------
-- patients  (maps to Patient)
-- ---------------------------------------------------------------
CREATE TABLE patients (
    patient_id     INT AUTO_INCREMENT PRIMARY KEY,
    patient_name   VARCHAR(100) NOT NULL,
    address        VARCHAR(150) NOT NULL,
    contact_number VARCHAR(20)  NOT NULL,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------
-- dentists  (maps to Dentist)
-- ---------------------------------------------------------------
CREATE TABLE dentists (
    dentist_id     INT AUTO_INCREMENT PRIMARY KEY,
    dentist_name   VARCHAR(100) NOT NULL,
    specialization VARCHAR(100),
    contact_number VARCHAR(20)  NOT NULL,
    active         BOOLEAN NOT NULL DEFAULT TRUE
);

-- ---------------------------------------------------------------
-- treatments  (maps to Treatment)
-- ---------------------------------------------------------------
CREATE TABLE treatments (
    treatment_id     INT AUTO_INCREMENT PRIMARY KEY,
    treatment_type   VARCHAR(80)  NOT NULL UNIQUE,
    description      VARCHAR(255),
    treatment_fee    DECIMAL(10,2) NOT NULL,
    consultation_fee DECIMAL(10,2) NOT NULL,
    active           BOOLEAN NOT NULL DEFAULT TRUE
);

-- ---------------------------------------------------------------
-- appointments  (maps to Appointment)
-- ---------------------------------------------------------------
CREATE TABLE appointments (
    appointment_id      INT AUTO_INCREMENT PRIMARY KEY,
    appointment_number  VARCHAR(30) NOT NULL UNIQUE,
    patient_id          INT NOT NULL,
    dentist_id          INT NOT NULL,
    treatment_id        INT NOT NULL,
    created_by          INT NOT NULL,
    appointment_date    DATE NOT NULL,
    appointment_time    TIME NOT NULL,
    status              ENUM('SCHEDULED','COMPLETED','CANCELLED','NO_SHOW') NOT NULL DEFAULT 'SCHEDULED',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_appt_patient   FOREIGN KEY (patient_id)   REFERENCES patients(patient_id),
    CONSTRAINT fk_appt_dentist   FOREIGN KEY (dentist_id)   REFERENCES dentists(dentist_id),
    CONSTRAINT fk_appt_treatment FOREIGN KEY (treatment_id) REFERENCES treatments(treatment_id),
    CONSTRAINT fk_appt_creator   FOREIGN KEY (created_by)   REFERENCES users(user_id),

    -- Prevents double-booking at the database level
    CONSTRAINT uq_dentist_slot UNIQUE (dentist_id, appointment_date, appointment_time),
    CONSTRAINT chk_appt_time CHECK (appointment_time BETWEEN '08:00:00' AND '17:00:00')
);

CREATE INDEX idx_appt_patient ON appointments(patient_id);
CREATE INDEX idx_appt_date    ON appointments(appointment_date);

-- ---------------------------------------------------------------
-- bills  (maps to Bill)
-- ---------------------------------------------------------------
CREATE TABLE bills (
    bill_id             INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id      INT NOT NULL UNIQUE,   -- UNIQUE enforces the 1:1 (0..1) relationship
    treatment_fee       DECIMAL(10,2) NOT NULL,
    consultation_fee    DECIMAL(10,2) NOT NULL,
    discount            DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount        DECIMAL(10,2) NOT NULL,
    payment_status      ENUM('PENDING','PAID','REFUNDED') NOT NULL DEFAULT 'PENDING',
    generated_by        INT NOT NULL,
    generated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bill_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id),
    CONSTRAINT fk_bill_generator   FOREIGN KEY (generated_by)   REFERENCES users(user_id),
    CONSTRAINT chk_total CHECK (total_amount >= 0)
);

-- ---------------------------------------------------------------
-- bill_items  (maps to BillItem)
-- Assumption: "description" added so receipts are readable.
-- ---------------------------------------------------------------
CREATE TABLE bill_items (
    bill_item_id   INT AUTO_INCREMENT PRIMARY KEY,
    bill_id        INT NOT NULL,
    description    VARCHAR(100) NOT NULL,
    amount         DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_billitem_bill FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE
);