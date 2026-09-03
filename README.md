# 🦷 Sunrise Dental Clinic – Management System

> **CIS6003 Advanced Programming | ICBT Campus | Written Assignment 1**
> Student: weerapperuma

A full-stack clinic management web application built with **Spring MVC**, **MySQL**, and **vanilla HTML/CSS/JS**, demonstrating three-tier architecture, REST API design, Test-Driven Development, and CI/CD automation.

---

## 📋 Table of Contents

- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Project Structure](#-project-structure)
- [Database Setup](#-database-setup)
- [Running the Application](#-running-the-application)
- [Test Suite](#-test-suite)
- [Version History](#-version-history)
- [CI/CD Pipeline](#-cicd-pipeline)

---

## ✨ Features

| Module | Description |
|:---|:---|
| 🔐 **Authentication** | Secure staff login with BCrypt password hashing and HTTP session management |
| 📅 **Appointments** | Register, search, and manage patient appointments with double-booking prevention |
| 🧾 **Billing** | Generate bills with treatment + consultation fee calculation, discount validation, and PAID/PENDING status |
| 📊 **Reports** | Date-range revenue reports with daily appointment and billing summaries |
| ✅ **Validation** | Server-side and client-side validation for all forms — contact numbers, date/time ranges, and clinic hours |

---

## 🛠 Technology Stack

| Layer | Technology |
|:---|:---|
| **Frontend** | HTML5, CSS3, JavaScript (Fetch API) |
| **Backend** | Java 17, Spring MVC 6, Apache Tomcat 10.1 |
| **Database** | MySQL 8.0, JDBC Template, Stored Triggers |
| **Build** | Apache Maven 3.9 (WAR packaging) |
| **Testing** | JUnit 5, Mockito 5, Spring MockMvc, Hamcrest, JSONPath |
| **CI/CD** | GitHub Actions |
| **Security** | Spring Security Crypto (BCrypt), HttpSession |

---

## 📁 Project Structure

```
cis6003-writ1-sunrise-dental-clinic/
│
├── clinic-frontend/                  # Static HTML/CSS/JS frontend
│   ├── css/style.css
│   ├── login.html
│   ├── dashboard.html
│   ├── appointment.html
│   ├── search.html
│   ├── bill.html
│   └── reports.html
│
├── clinic-service/                   # Spring MVC backend (Maven WAR)
│   ├── src/main/java/lk/clinic/service/
│   │   ├── config/                   # Spring & Web configuration
│   │   ├── controller/               # REST controllers
│   │   ├── service/                  # Business logic
│   │   ├── repository/               # JDBC data access
│   │   ├── model/                    # Domain entities
│   │   ├── dto/                      # Request/Response DTOs
│   │   ├── billing/                  # Strategy pattern for billing
│   │   └── validation/               # Input validation
│   ├── src/test/java/                # Unit + Integration tests (49 tests)
│   └── pom.xml
│
├── .github/workflows/ci.yml          # GitHub Actions CI pipeline
├── docs/report/                      # Assignment report sections
└── README.md
```

---

## 🗄 Database Setup

1. Create a MySQL 8.0 database named `clinic_db`
2. Run the schema script located at `clinic-service/src/main/resources/schema.sql`
3. Update the connection details in `clinic-service/src/main/resources/application.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/clinic_db
db.username=root
db.password=your_password
```

The schema includes:
- Tables: `users`, `dentists`, `treatments`, `appointments`, `bills`, `bill_items`
- Triggers: `trg_prevent_double_booking`, `trg_bills_before_insert`
- Unique constraints enforcing the 1:1 appointment-to-bill rule

---

## 🚀 Running the Application

### Prerequisites
- Java 17+
- Apache Maven 3.9+
- Apache Tomcat 10.1
- MySQL 8.0

### Build & Deploy

```bash
# 1. Build the WAR file
mvn clean package -DskipTests --file clinic-service/pom.xml

# 2. Copy WAR to Tomcat webapps
copy clinic-service\target\clinic-service-1.0-SNAPSHOT.war <TOMCAT_HOME>\webapps\clinic.war

# 3. Start Tomcat, then open in browser:
# Frontend:  http://localhost:8080/clinic-frontend/login.html
# API Base:  http://localhost:8080/clinic/api/
```

### Default Login Credentials

| Username | Password | Role |
|:---|:---|:---|
| `admin` | `password123` | ADMIN |
| `reception` | `password123` | RECEPTION |

---

## 🧪 Test Suite

49 automated tests across 10 test classes:

```bash
mvn test --file clinic-service/pom.xml
```

```
Tests run: 49, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Category | Classes | Tests |
|:---|:---|:---:|
| Unit Tests | `BillingServiceTest`, `AuthServiceTest`, `AppointmentServiceTest`, `AppointmentValidatorTest`, `AppointmentNumberGeneratorTest`, `StandardBillingStrategyTest` | 30 |
| Integration Tests | `AuthControllerIntegrationTest`, `AppointmentControllerIntegrationTest`, `BillControllerIntegrationTest`, `MetaControllerIntegrationTest` | 19 |

### TDD Example
The discount-cap rule (`discount must not exceed total fees`) was implemented using strict TDD:
- **RED** — test written first, failed with `Errors: 1`
- **GREEN** — guard added to `BillingService.generate()`, all 49 tests pass

---

## 🏷 Version History

| Tag | Milestone |
|:---:|:---|
| `v0.1` | Initial project setup — Spring MVC, Tomcat 10, MySQL connection |
| `v0.2` | Core backend APIs — authentication, appointments, search, billing |
| `v0.3` | Frontend complete — all pages with client-side validation |
| `v0.4` | Testing suite — 49 tests, TDD discount-cap rule, GitHub Actions CI |
| `v1.0` | Production release — full system operational |

---

## ⚙️ CI/CD Pipeline

The `.github/workflows/ci.yml` pipeline runs automatically on every push to `main`:

1. ✅ Checkout repository
2. ✅ Set up JDK 17 (Temurin)
3. ✅ Cache Maven dependencies
4. ✅ Run full test suite (`mvn test`)
5. ✅ Package WAR artifact (`mvn package`)
6. ✅ Upload Surefire test reports (retained 7 days)
7. ✅ Upload built WAR (retained 14 days)

---

## 📄 Report Documentation

Assignment report sections are in `docs/report/`:

| File | Content |
|:---|:---|
| `TASK_B_Architecture_Implementation.md` | Three-tier architecture, design patterns, REST API |
| `TASK_C_Testing_TDD.md` | TDD walkthrough, test plan, traceability matrix |
| `TASK_D_GitHub_VersionControl.md` | Branching strategy, commit history, version tags |
| `TASK_E_Critical_Reflection.md` | What went well, limitations, future work |
| `TASK_F_Conclusion.md` | Project conclusion |
| `References.md` | Harvard-style references |

---

*CIS6003 Advanced Programming — ICBT Campus*
