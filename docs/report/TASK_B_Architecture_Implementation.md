# TASK B — Architecture & Implementation (~1500 words)

---

## 4.1 Three-Tier Distributed Architecture

The Sunrise Dental Clinic system adopts a three-tier distributed architecture that cleanly separates responsibilities across the Presentation Layer, Business Logic Layer, and Data Persistence Layer.

**Tier 1 — Presentation Layer:** Static HTML5/CSS3/JavaScript pages running in the browser. These pages communicate with the backend exclusively through JSON-based REST calls using the native `fetch()` API, with no direct database access.

**Tier 2 — Business Logic Layer:** A Spring WebMVC application deployed on Apache Tomcat 10. This tier hosts REST controllers, service classes (AppointmentService, BillingService, AuthService), validation components, and the Strategy pattern for billing calculations. All business rules reside here.

**Tier 3 — Data Persistence Layer:** A MySQL 8.0 relational database containing normalised tables, stored functions, BEFORE INSERT triggers, CHECK constraints, and a database view. Data access occurs through Spring's JdbcTemplate.

**Justification:** This separation ensures loose coupling — the frontend has no awareness of the database engine, and the REST API can be consumed by future clients (mobile apps, third-party integrations) without backend modifications. Each tier can be deployed, tested, and scaled independently. The database-level triggers and constraints act as a final safety net even if the application layer is bypassed.

*(Insert architecture diagram here — see Section 4.1 diagram in the artifact file)*

---

## 4.2 Technology Stack

| Layer | Technology | Version | Reason for Selection |
|:---|:---|:---|:---|
| Frontend | HTML5, CSS3, Vanilla JS | ES6+ | Zero external dependencies; native `fetch()` provides adequate HTTP capability for a point-of-sale interface without requiring framework build steps. |
| Backend | Spring WebMVC | 6.x | Annotation-driven controller mapping (`@RestController`, `@GetMapping`), constructor-based dependency injection, and built-in CORS handling — without Spring Boot overhead. |
| Server | Apache Tomcat | 10.1.x | Mature Servlet 6.0 / Jakarta EE 10 container with built-in HTTP session management, which the project uses for authentication state. |
| Database | MySQL | 8.0 | Supports stored functions, BEFORE INSERT triggers, CHECK constraints, and CREATE VIEW — all advanced features this project relies upon. ACID-compliant and widely used in healthcare IT. |
| Data Access | Spring JdbcTemplate | 6.x | Thin JDBC abstraction that prevents resource leaks while giving full SQL control. Chosen over JPA/Hibernate to satisfy the assignment's JDBC emphasis. |
| Security | jBCrypt | 0.4 | Adaptive cost-factor hashing algorithm; passwords are never stored or compared in plaintext. |
| Build | Apache Maven | 3.9.x | Handles dependency resolution, compilation, and WAR packaging through a single `pom.xml`. |

---

## 4.3 Design Patterns

### 4.3.1 Model-View-Controller (MVC)

Separates HTTP handling (Controllers), business logic (Services), and data representation (Models/DTOs). For example, `AppointmentController` receives POST requests, delegates to `AppointmentService`, which coordinates validation and persistence through repositories, then returns a response DTO. Adding new endpoints requires only a new controller method without modifying existing services.

```java
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    public AppointmentController(AppointmentService service) {
        this.appointmentService = service;
    }
    @PostMapping
    public ResponseEntity<AppointmentResponse> register(
            @RequestBody AppointmentRegistrationRequest request, HttpSession session) {
        // delegates to service layer
        AppointmentResponse response = appointmentService.register(request, username);
        if (response.success()) return ResponseEntity.status(201).body(response);
        return ResponseEntity.badRequest().body(response);
    }
}
```

### 4.3.2 Singleton Pattern

Spring's IoC container instantiates every `@Service`, `@Component`, and `@RestController` bean as a singleton by default. A single `BillingService` instance is reused across all concurrent requests, conserving memory while ensuring consistent behaviour.

### 4.3.3 Repository Pattern

Four repository classes (`AppointmentRepository`, `BillRepository`, `PatientRepository`, `UserRepository`) encapsulate all SQL and JdbcTemplate interactions. If the database engine changes, only repository classes require modification — service classes remain unaffected.

```java
@Repository
public class PatientRepository {
    private final JdbcTemplate jdbc;
    public int save(Patient patient) {
        jdbc.update("INSERT INTO patients (patient_name, address, contact_number) VALUES (?,?,?)",
                patient.name(), patient.address(), patient.contactNumber());
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
    }
}
```

### 4.3.4 Service Layer Pattern

`AppointmentService.register()` orchestrates the full workflow: validates input, checks for double-booking, persists the patient, generates an appointment number, and saves the appointment. Business rules live in one well-defined location.

### 4.3.5 Strategy Pattern

The `BillingStrategy` interface declares `calculateTotal(treatmentFee, consultationFee, discount)`. `StandardBillingStrategy` implements the formula: `(treatmentFee + consultationFee) - discount`. The service depends on the interface, not the concrete class — enabling future pricing strategies (e.g., insurance billing) without modifying `BillingService`.

```java
public interface BillingStrategy {
    BigDecimal calculateTotal(BigDecimal treatmentFee, BigDecimal consultationFee, BigDecimal discount);
}

@Component
public class StandardBillingStrategy implements BillingStrategy {
    @Override
    public BigDecimal calculateTotal(BigDecimal treatmentFee, BigDecimal consultationFee, BigDecimal discount) {
        return treatmentFee.add(consultationFee).subtract(discount);
    }
}
```

### 4.3.6 Factory Pattern

`AppointmentNumberGenerator` acts as a factory for unique identifiers, producing formatted strings like `APT-20260902-0001` based on today's date and the current count. The numbering scheme can be changed by modifying this single component.

### 4.3.7 Template Method Pattern

`AbstractValidator<T>` provides the fixed skeleton: create result → call abstract `doValidate()` → return result. `AppointmentValidator` overrides `doValidate()` with domain-specific checks. New validators simply extend the base class.

```java
public abstract class AbstractValidator<T> {
    public final ValidationResult validate(T target) {
        ValidationResult result = new ValidationResult();
        doValidate(target, result);
        return result;
    }
    protected abstract void doValidate(T target, ValidationResult result);
}
```

---

## 4.4 Database Design

*(Insert ERD diagram here — see Section 4.4 ERD in the artifact file)*

The schema consists of 7 normalised tables: `users`, `patients`, `dentists`, `treatments`, `appointments`, `bills`, and `bill_items`. Key constraints include `UNIQUE(dentist_id, appointment_date, appointment_time)` preventing double-booking, `CHECK(appointment_time BETWEEN '08:00' AND '17:00')` enforcing clinic hours, and `UNIQUE(appointment_id)` on bills enforcing the 1:1 billing rule.

### Advanced Features

**1. Stored Function — `fn_calculate_total`:** Computes `(treatment_fee + consultation_fee) - discount`, centralising the billing formula at the database level.

```sql
CREATE FUNCTION fn_calculate_total(p_fee DECIMAL(10,2), p_consult DECIMAL(10,2), p_disc DECIMAL(10,2))
    RETURNS DECIMAL(10,2) DETERMINISTIC
BEGIN
    RETURN (p_fee + p_consult) - p_disc;
END;
```

**2. Trigger — `trg_bills_before_insert`:** Auto-calculates `total_amount` before every bill insertion by invoking `fn_calculate_total`, ensuring no bill is ever saved without a properly computed total.

**3. Trigger — `trg_prevent_double_booking`:** Counts existing non-cancelled bookings for the same dentist/date/time slot before insertion. If one exists, it raises `SQLSTATE '45000'`, aborting the transaction.

**4. View — `vw_daily_appointments`:** Pre-joins four tables to produce a flattened read-only dataset for the daily appointments report.

---

## 4.5 REST Web Services

| Method | Endpoint | Purpose | Status |
|:---|:---|:---|:---|
| POST | `/api/auth/login` | Staff authentication | 200 |
| POST | `/api/auth/logout` | Terminate session | 200 |
| GET | `/api/auth/me` | Retrieve session identity | 200/401 |
| GET | `/api/meta/dentists` | List active dentists | 200 |
| GET | `/api/meta/treatments` | List treatments with fees | 200 |
| POST | `/api/appointments` | Register new appointment | 201/400/409 |
| GET | `/api/appointments/search` | Search with filters | 200 |
| POST | `/api/bills` | Generate invoice | 201/409 |
| GET | `/api/bills?appointmentId=` | Retrieve existing bill | 200/404 |
| POST | `/api/bills/{billId}/pay` | Mark bill as paid | 200 |
| GET | `/api/reports/daily?date=` | Daily appointment schedule | 200 |
| GET | `/api/reports/dentist?dentistId=` | Dentist workload report | 200 |
| GET | `/api/reports/revenue` | Revenue summary by day | 200 |

*(Insert Postman screenshot of sample API response here)*

---

## 4.6 Frontend Implementation

Seven static HTML pages (`login.html`, `dashboard.html`, `appointment.html`, `search.html`, `bill.html`, `reports.html`, `help.html`) share a common utility module `api.js` that standardises HTTP communication. The `initAuth()` function runs on every page load — it calls `GET /api/auth/me` and redirects to `login.html` if the session has expired. All `fetch()` calls include `credentials: 'include'` to transmit the `JSESSIONID` cookie automatically. CORS is configured in `WebConfig.java` with `.allowCredentials(true)` to permit cross-origin cookie transmission during development.

---

## 4.7 Validation Mechanisms

Validation operates at three independent layers:

| Field | Client (JavaScript) | Server (Java) | Database (MySQL) |
|:---|:---|:---|:---|
| Patient Name | Required check | `isBlank()` | NOT NULL |
| Contact Number | Regex `^0\d{9}$` | `String.matches("0\\d{9}")` | NOT NULL |
| Appointment Date | HTML `min=today` | `isBefore(LocalDate.now())` | — |
| Appointment Time | Range 08:00–17:00 | `LocalTime` bounds check | CHECK constraint |
| Double Booking | — | `existsByDentistAndDateTime()` | UNIQUE + trigger |
| 1:1 Billing | — | `existsByAppointment()` → 409 | UNIQUE on `appointment_id` |

*(Insert validation error screenshot here)*

---

## 4.8 Reports

**Daily Appointments:** Fetches `GET /api/reports/daily?date=yyyy-MM-dd`, rendering a table of scheduled appointments with appointment ID, time, patient, dentist, treatment, and status.

**Dentist Schedule:** Fetches `GET /api/reports/dentist?dentistId=&date=`, showing all bookings for a selected practitioner across the chosen period.

**Revenue Analytics:** Fetches `GET /api/reports/revenue`, grouping paid bills by date with counts and sum totals, providing management with daily income trends.

*(Insert report screenshots here)*

---

## 4.9 Security & Sessions

**Authentication:** Staff submit credentials via `POST /api/auth/login`. `AuthService` verifies the password against the stored BCrypt hash using `BCrypt.checkpw()`. On success, the username and role are stored as `HttpSession` attributes, and Tomcat issues a `JSESSIONID` cookie.

**Session Management:** Every subsequent API call includes the session cookie (via `credentials: 'include'`). Controllers verify `session.getAttribute("loggedInUser")` is not null; if missing, HTTP 401 is returned and the frontend redirects to login.

**Roles:** Two roles exist — `ADMIN` (full access) and `RECEPTIONIST` (front-desk operations). The role is stored as an `ENUM` in the `users` table and displayed in the UI header.

**Password Security:** BCrypt incorporates an adaptive cost factor and a built-in random salt. Two users with identical passwords produce different hashes. Plaintext passwords are never stored, logged, or returned in API responses.

**Cookie Security:** `JSESSIONID` is HttpOnly by default (preventing XSS-based theft), and `SameSite=Lax` reduces CSRF risk.
