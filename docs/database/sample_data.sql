-- =================================================================
-- SEED DATA (Using BCrypt hashes for security compliance)
-- =================================================================

-- Password for both is "password123" hashed with BCrypt
INSERT INTO users (username, password_hash, full_name, role) VALUES
                                                                 ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System Administrator', 'ADMIN'),
                                                                 ('reception', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nadia Perera', 'RECEPTIONIST');

INSERT INTO dentists (dentist_name, specialization, contact_number) VALUES
                                                                        ('Dr. Sunila Jayawardena', 'Orthodontics', '0711234567'),
                                                                        ('Dr. Kasun Fernando', 'General Dentistry', '0719876543');

INSERT INTO treatments (treatment_type, description, treatment_fee, consultation_fee) VALUES
                                                                                          ('Consultation',          'Initial check-up and diagnosis', 0.00,    1500.00),
                                                                                          ('Scaling and Polishing', 'Routine cleaning',               3500.00, 1500.00),
                                                                                          ('Filling',               'Cavity filling',                 4500.00, 1500.00),
                                                                                          ('Tooth Extraction',      'Surgical or simple extraction',  5000.00, 1500.00),
                                                                                          ('Root Canal Treatment',  'Endodontic treatment',           15000.00,1500.00);