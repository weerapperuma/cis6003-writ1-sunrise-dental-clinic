# TASK C – Testing & TDD (≈800 words)

---

## 5.1 Test Rationale

Testing ensures that individual components and the overall system behave correctly under both expected and unexpected inputs. For the Sunrise Dental Clinic project, testing was prioritised for three reasons.

First, several **business-critical rules** carry real operational consequences if they fail: double-booking prevention, the 1:1 appointment-to-bill constraint, contact number format enforcement, and clinic-hours validation. A defect in any of these rules would directly impact clinic scheduling and patient billing records.

Second, the system involves three tiers — browser frontend, Spring backend, and MySQL database — so a defect in one layer can silently propagate to the others. **Automated tests catch cross-layer failures at commit time** rather than during live clinic use.

Third, the assignment requires testing at both the service layer (unit) and the HTTP layer (integration), along with CI automation to evidence a disciplined development process.

---

## 5.2 TDD in Practice — Red → Green Walkthrough

Test-Driven Development (TDD) follows a strict three-phase cycle: **Red** (write a failing test first), **Green** (write the minimum production code to pass it), and **Refactor** (clean up without breaking the test).

### The Rule Being Implemented

A business rule was identified during development review: **a discount must not exceed the sum of treatment and consultation fees**. Allowing, for example, a LKR 4,000 discount on a LKR 3,000 bill would produce a negative total — corrupting the daily revenue report.

### Step 1 — RED (Test Written Before Any Production Code)

The following test was added to `BillingServiceTest` before any guard existed in `BillingService.generate()`:

```java
@Test
@DisplayName("[TDD] Should reject bill when discount exceeds sum of treatment and consultation fees")
void testDiscountExceedsTotalFees() {
    BigDecimal treatmentFee      = new BigDecimal("2000.00");
    BigDecimal consultFee        = new BigDecimal("1000.00");
    BigDecimal oversizedDiscount = new BigDecimal("4000.00"); // 4000 > (2000+1000)

    AppointmentBillingInfo info =
            new AppointmentBillingInfo(7, "APT-20260903-0007", treatmentFee, consultFee);
    when(appointmentRepository.findBillingInfo(7)).thenReturn(info);
    when(billRepository.existsByAppointment(7)).thenReturn(false);

    BillResponse res = billingService.generate(new BillGenerationRequest(7, oversizedDiscount), "admin");

    assertFalse(res.success(), "Discount exceeding total fees must be rejected");
    assertTrue(res.message().toLowerCase().contains("discount"));
    verify(billRepository, never()).save(any());
}
```

Running `mvn test` at this point produced **Tests run: 1, Errors: 1** — execution reached `userRepository.findByUsername()` (no stub existed, no guard stopped it). This confirmed the guard was absent (**RED**).

> *(Insert screenshot: Maven output showing testDiscountExceedsTotalFees ERROR — RED phase)*

### Step 2 — GREEN (Minimum Code Added to Pass)

The following guard was inserted into `BillingService.generate()` immediately after the appointment-existence check:

```java
// Guard: discount must not exceed the sum of treatment and consultation fees
BigDecimal totalFees = info.treatmentFee().add(info.consultationFee());
if (discount.compareTo(totalFees) > 0) {
    return BillResponse.error(
            "Discount (" + discount + ") cannot exceed total fees (" + totalFees + ").");
}
```

Re-running `mvn test` produced **Tests run: 49, Failures: 0, Errors: 0, BUILD SUCCESS** (**GREEN**).

> *(Insert screenshot: Maven output showing 49 tests passing — GREEN phase)*

---

## 5.3 Test Plan Table

| Test ID | Feature | Input | Expected Outcome | Actual Outcome | Pass? |
|:---:|:---|:---|:---|:---|:---:|
| T-01 | Login – valid credentials | `admin` / `password123` | HTTP 200, session created | HTTP 200, session set | ✅ |
| T-02 | Login – wrong password | `admin` / `wrongPass` | HTTP 401, `success: false` | HTTP 401 returned | ✅ |
| T-03 | Session guard – unauthenticated | No JSESSIONID | HTTP 401 on all protected endpoints | HTTP 401 on `/api/appointments`, `/api/bills` | ✅ |
| T-04 | Appointment – valid booking | All fields valid, free slot | HTTP 201, appointment number returned | `APT-20260910-0001` generated | ✅ |
| T-05 | Appointment – blank patient name | `patientName = ""` | HTTP 400, "Patient name is required." | Validation error in errors list | ✅ |
| T-06 | Appointment – bad phone number | `contactNumber = "077ABC"` | HTTP 400, format error | "Contact number must be 10 digits starting with 0." | ✅ |
| T-07 | Appointment – past date | Date = yesterday | HTTP 400, date rejected | "Appointment date cannot be in the past." | ✅ |
| T-08 | Appointment – out-of-hours time | `appointmentTime = "07:30"` | HTTP 400, boundary error | "Appointment time must be between 08:00 and 17:00." | ✅ |
| T-09 | Double-booking prevention | Dentist already booked | HTTP 409 Conflict | "Dentist is already booked for the selected date and time." | ✅ |
| T-10 | Bill – generate with discount | Treatment=4500, Consult=1500, Disc=500 | HTTP 201, total = **5500.00** | LKR 5,500.00 | ✅ |
| T-11 | Bill – negative discount | `discount = -100` | HTTP 400, "Discount cannot be negative." | Rejected before DB call | ✅ |
| T-12 | Bill – discount exceeds total (TDD) | `discount=4000, fees=3000` | HTTP 400, discount rejection message | "Discount cannot exceed total fees." | ✅ |
| T-13 | Bill – 1:1 rule enforcement | Second bill, same appointment | HTTP 409, "1:1 rule" message | Duplicate blocked | ✅ |
| T-14 | Bill – appointment not found | `appointmentId = 999` | HTTP 404, "Appointment not found." | Not found message returned | ✅ |
| T-15 | Mark bill PAID | `POST /api/bills/1/pay` | HTTP 200, "Bill marked as PAID." | Status updated successfully | ✅ |
| T-16 | Appointment number – first booking | First booking today | Format `APT-YYYYMMDD-0001` | Correct format generated | ✅ |
| T-17 | Appointment number – increment | 8 existing today | Format `APT-YYYYMMDD-0009` | Correctly incremented | ✅ |
| T-18 | Billing strategy – standard | Treatment 4500 + Consult 1500 – Disc 500 | Total = 5500.00 | BigDecimal: 5500.00 | ✅ |
| T-19 | Meta API – dentist list | `GET /api/meta/dentists` | Array of `{id, name}` | List with 2 dentists returned | ✅ |

---

## 5.4 Test Data Table

### Valid Test Data

| Field | Set A | Set B |
|:---|:---|:---|
| Patient Name | `Kamal Perera` | `Nimal Fernando` |
| Address | `No 12, Kandy Road, Colombo` | `124 Galle Road, Colombo 03` |
| Contact Number | `0771234567` | `0112345678` |
| Dentist ID | `1` | `2` |
| Treatment ID | `2` | `1` |
| Appointment Date | `2026-09-10` (future) | `2026-09-15` (future) |
| Appointment Time | `10:30` (within 08:00–17:00) | `14:00` (within 08:00–17:00) |
| Discount | `0.00` | `500.00` |

### Invalid / Boundary Test Data

| Field | Invalid A | Invalid B | Invalid C |
|:---|:---|:---|:---|
| Patient Name | *(empty)* | *(blank whitespace only)* | — |
| Contact Number | `077123` *(too short)* | `1771234567` *(no leading 0)* | `077ABC4567` *(letters)* |
| Dentist ID | `0` *(not selected)* | — | — |
| Treatment ID | `-1` *(invalid)* | — | — |
| Appointment Date | `2020-01-01` *(past)* | `05/09/2026` *(wrong format)* | — |
| Appointment Time | `07:30` *(before 08:00)* | `17:30` *(after 17:00)* | `invalid-time` *(malformed)* |
| Discount | `-100.00` *(negative)* | `4000.00` *(exceeds 3000 fees)* | — |

---

## 5.5 Test Automation — JUnit + Maven + GitHub Actions

### Framework Stack

| Tool | Role |
|:---|:---|
| **JUnit 5 (Jupiter)** | Test runner and assertion framework (`@Test`, `@BeforeEach`, `assertFalse`, `assertEquals`) |
| **Mockito 5.3.1** | Mock/stub creation for repositories and services (`@Mock`, `when().thenReturn()`) |
| **Spring MockMvc** | HTTP-layer simulation for integration tests — sends real requests, checks status codes and JSON |
| **Hamcrest 2.2** | Enhanced assertion matchers used by MockMvc result handlers |
| **JSON Path 2.9.0** | JSON response body assertions (`jsonPath("$.success").value(true)`) |

### Automated Run Result

```
mvn -B test --file clinic-service/pom.xml

Tests run: 49, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS — Total time: 5.2 s
```

Breakdown: **30 unit tests** (service layer, validator, utility, billing strategy) and **19 integration tests** (MockMvc HTTP layer across Auth, Appointment, Bill, and Meta controllers).

### GitHub Actions CI Pipeline (`.github/workflows/ci.yml`)

The workflow triggers automatically on every `git push` to `main`:

```yaml
name: Java CI with Maven
on:
  push:
    branches: [ "main" ]
jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      - run: mvn -B test --file clinic-service/pom.xml
      - run: mvn -B package -DskipTests --file clinic-service/pom.xml
```

> *(Insert screenshot: GitHub Actions tab showing green ✅ workflow run on main branch)*

---

## 5.6 Traceability Matrix

| Requirement | Design Component | Test ID(s) |
|:---|:---|:---|
| Staff must log in before accessing the system | `AuthController`, `AuthService`, `HttpSession` | T-01, T-02, T-03 |
| Contact numbers must be 10 digits starting with 0 | `AppointmentValidator.doValidate()` | T-06 |
| Appointments cannot be in the past or outside clinic hours (08:00–17:00) | `AppointmentValidator.doValidate()` | T-07, T-08 |
| A dentist cannot be double-booked for the same date and time | `AppointmentService.register()` + DB trigger `trg_prevent_double_booking` | T-09 |
| Each appointment generates a unique formatted appointment number | `AppointmentNumberGenerator.next()` | T-16, T-17 |
| Bill total = treatment fee + consultation fee − discount | `BillingStrategy.calculateTotal()` + DB trigger `trg_bills_before_insert` | T-10, T-18 |
| Discount must not be negative | `BillingService.generate()` pre-check | T-11 |
| Discount must not exceed total fees (TDD rule) | `BillingService.generate()` guard added via TDD | T-12 |
| Only one bill may exist per appointment (1:1 constraint) | `BillingService.generate()` + `UNIQUE(appointment_id)` on `bills` table | T-13 |
| Cashier can mark a bill as PAID | `BillingService.markPaid()` + `BillController.pay()` | T-15 |

---

## 5.7 Evaluation of Success & Lessons Learned

### Test Outcomes

All 49 automated tests — 30 unit tests and 19 integration tests — passed with zero failures on every local run. The GitHub Actions CI pipeline confirmed identical results on a clean Ubuntu Linux environment, demonstrating that the suite is not dependent on any local machine configuration.

### What Worked Well

**TDD discipline for the discount guard** proved directly effective. Writing `testDiscountExceedsTotalFees` before the production guard forced a precise specification of the expected response: rejection with an error message and no call to `billRepository.save()`. The implementation was correct on the first attempt because the test had already defined the contract precisely.

**Mockito dependency isolation** allowed the entire service layer to be tested without a running database, keeping the full 49-test suite under six seconds and ensuring deterministic results regardless of data state.

**MockMvc integration tests** caught a session-handling edge case during development: the `/api/auth/me` endpoint was accessible even when the `loggedIn` session attribute was absent. The integration test exposed this before any frontend testing began.

### Lessons Learned

1. **Test boundary conditions, not just the happy path.** The most valuable tests were those verifying invalid inputs (T-05 through T-12) — these confirmed that all five validator failure modes and three billing pre-checks returned clean 400/409 responses rather than unhandled HTTP 500 errors.

2. **CI removes "works on my machine" problems.** Running the full suite in GitHub Actions revealed a duplicate `maven-compiler-plugin` declaration in `pom.xml` that raised a warning on the CI server. This was corrected before submission.

3. **Integration tests are worth the setup cost.** MockMvc tests verified the complete HTTP contract — URL routing, JSON serialisation, session cookie propagation, and HTTP status codes — in a way that unit tests alone cannot. This substantially increased confidence in the API layer before any manual browser testing.
