# Critical Reflection (≈350 words)

---

## What Went Well

The three-tier architecture proved to be the most successful structural decision made during this project. Separating the presentation layer (HTML/CSS/JavaScript), the business layer (Spring MVC controllers and services), and the persistence layer (MySQL with JdbcTemplate) meant that each tier could be developed, tested, and debugged independently. When a billing calculation discrepancy was found during integration testing, the issue was isolated to the `BillingStrategy` class without touching either the database schema or the frontend — a direct benefit of clean separation of concerns.

The test-driven approach to the discount-cap business rule also delivered measurable value. Writing the test before the production guard forced a precise definition of the expected behaviour — the error message wording, the HTTP status code, and the side effect (no database write) — before a single line of production code was written. The implementation was correct on the first attempt, which would not have been guaranteed with a code-first approach.

The use of MySQL database triggers (`trg_prevent_double_booking`, `trg_bills_before_insert`) as a second line of defence for critical business rules — beyond the application-level checks — gave the system genuine data integrity that survives even direct database access from outside the application.

---

## Limitations

The current system requires staff to be physically present at a clinic workstation. There is no patient-facing portal, meaning all appointment bookings must be made by reception staff over the phone or in person. This creates a bottleneck during peak hours and limits patient autonomy.

The authentication system uses a basic username/password model stored with BCrypt hashing, which is secure, but has no support for multi-factor authentication, account lockout after repeated failures, or password reset — all standard requirements in a real clinical environment subject to healthcare data protection obligations.

---

## Future Work

Three enhancements would significantly extend the system's value in a real clinic deployment:

1. **Automated Email and SMS Alerts**: Integrating the Twilio SMS API and JavaMail (via SMTP) would enable automated appointment confirmation messages to patients at booking time and reminder messages 24 hours before the scheduled slot. This would reduce no-shows — a common operational cost in dental practices.

2. **Online Payment Integration**: Replacing the manual "mark as PAID" button with a payment gateway such as PayHere (Sri Lanka) or Stripe would allow patients to pay invoices online via a secure link sent with their appointment confirmation, eliminating the need for cash handling at reception.

3. **Patient Self-Service Portal**: A separate patient-facing web interface would allow patients to book, reschedule, or cancel appointments and view their billing history without staff involvement. This would require a separate authentication flow with patient credentials, distinct from the staff login system used in the current implementation.
