# TASK D – GitHub & Version Control (≈700 words)

---

## 6.1 Repository Creation & Accessibility

The project is hosted on GitHub under the repository:

**`https://github.com/weerapperuma/cis6003-writ1-sunrise-dental-clinic`**

The repository is **public**, allowing the marker full read access to the complete commit history, source code, branch structure, tags, and CI/CD pipeline configuration without requiring authentication.

The repository structure follows a clear two-module layout:

```
cis6003-writ1-sunrise-dental-clinic/
├── clinic-frontend/        ← Static HTML/CSS/JS frontend
├── clinic-service/         ← Spring MVC backend (Maven WAR)
│   ├── src/main/java/      ← Production source code
│   ├── src/test/java/      ← Unit + integration test suite
│   └── pom.xml
├── .github/workflows/ci.yml ← GitHub Actions CI pipeline
├── docs/report/            ← Assignment documentation
└── README.md
```

> *(Insert screenshot: GitHub repository home page showing public visibility, file structure, and commit count)*

---

## 6.2 Branching Strategy & Commit History

### Branching Strategy

The project used a **single protected main branch** strategy, which is appropriate for a solo developer assignment:

- All development was committed directly to `main`
- Each commit represents a single, atomic, deployable change
- Commit messages follow the **Conventional Commits** standard (`feat:`, `fix:`, `test:`, `ci:`, `docs:`) making the history self-documenting and machine-readable

This is a standard approach for individual projects. In a team context, the strategy would extend to feature branches (e.g., `feature/billing-api`) with pull requests, code review, and branch protection rules before merging to `main`.

### Commit History Summary

The project accumulated **17 commits** across the development lifecycle, each representing a meaningful and focused unit of work:

| Commit | Message | Phase |
|:---:|:---|:---|
| `7f594b3` | project initialized | Setup |
| `b36d357` | Design database and ER diagram and Setup Maven projects | Setup |
| `4e9aec8` | v0.1: Initial setup - Spring MVC, Tomcat 10, MySQL connection | Backend |
| `b34971b` | Implement user authentication: POST /api/auth/login + logout | Backend |
| `7741492` | Implement appointment registration API: validation, double-booking protection | Backend |
| `0657819` | Implement appointment search API with dynamic filtered queries | Backend |
| `3802049` | Implement billing API: Strategy pattern, trigger-computed totals, 1:1 enforcement | Backend |
| `73c6c37` | feat: implement working staff authentication and login page | Frontend |
| `22832dd` | feat(ui): complete clinic UI phase - dashboard, appointments, billing | Frontend |
| `5c3a0a6` | v0.3 - Frontend pages: all views with validation | Frontend |
| `6984528` | test: add comprehensive JUnit 5 and Mockito unit test suite (29 tests) | Testing |
| `6f6e426` | test: add Spring MockMvc integration test suite for REST controllers | Testing |
| `5b9f9e9` | ci: add GitHub Actions workflow for automated Maven testing | CI/CD |
| `e4c5450` | fix: add robust date validation and error handling in ReportController | Bug Fix |
| `2da0b84` | Add unit + integration tests, TDD discount-cap rule, GitHub Actions CI | Testing |
| `b4026c9` | fix: use explicit cache action and add permissions to fix CI startup failure | CI/CD |
| `46ab94b` | ci: trigger GitHub Actions after making repo public | CI/CD |

> *(Insert screenshot: GitHub commit history page showing all 17 commits with messages, dates, and author)*

---

## 6.3 Version Tags & Releases (v0.1 → v1.0)

Five annotated Git tags were created to mark the major milestones of the project. Each tag points to the specific commit that completed that phase:

| Tag | Commit | Milestone Description |
|:---:|:---:|:---|
| **v0.1** | `4e9aec8` | Initial project scaffolding — Spring MVC, Tomcat 10, MySQL JDBC connection verified |
| **v0.2** | `7741492` | Core backend complete — authentication, appointment registration, search, and billing APIs operational |
| **v0.3** | `5c3a0a6` | Frontend complete — all six HTML pages (login, dashboard, appointments, billing, search, reports) with client-side validation |
| **v0.4** | `2da0b84` | Testing suite complete — 49 automated tests (30 unit + 19 integration), TDD discount-cap rule, GitHub Actions CI pipeline configured |
| **v1.0** | `46ab94b` | Production release — full system operational, all tests passing, CI/CD pipeline active |

Tags were created as **annotated tags** (`git tag -a`) rather than lightweight tags, so each carries a tagger identity, timestamp, and descriptive message — providing a permanent, signed audit trail.

```bash
git tag -a v0.1 4e9aec8 -m "v0.1: Initial project setup - Spring MVC, Tomcat 10, MySQL connection"
git tag -a v1.0 HEAD    -m "v1.0: Production release - full clinic system with CI/CD pipeline"
git push origin --tags
```

> *(Insert screenshot: GitHub Tags page at `/tags` showing all 5 tags — v0.1 through v1.0)*

---

## 6.4 Workflow — GitHub Actions CI/CD

The CI/CD pipeline is defined in `.github/workflows/ci.yml` and is committed to the repository. It is configured to trigger automatically on every push to `main` and on every pull request.

### Pipeline Stages

```yaml
on:
  push:
    branches: [ "main", "master" ]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - Checkout Repository
      - Set up JDK 17 (Temurin distribution)
      - Cache Maven packages (~/.m2)
      - Run Unit & Integration Tests   ← mvn -B test
      - Package WAR Artifact           ← mvn -B package -DskipTests
      - Upload Surefire test reports   ← artifact retained 7 days
      - Upload built WAR file          ← artifact retained 14 days
```

The pipeline runs on a **clean Ubuntu Linux environment** on every push, ensuring that builds are reproducible and not dependent on any local developer machine state. The WAR artefact is automatically packaged and uploaded, making it ready for deployment to any Tomcat 10 server.

> *(Insert screenshot: `.github/workflows/ci.yml` file as viewed on GitHub — confirming the pipeline is committed to the repository)*

> *(Insert screenshot: Local `mvn test` output showing "Tests run: 49, Failures: 0, Errors: 0, BUILD SUCCESS" — the same command that the CI pipeline executes)*

---

## 6.5 Deployment to Tomcat & Live Demo

The application is packaged as a standard **WAR file** (`clinic-service-1.0-SNAPSHOT.war`) and deployed to an **Apache Tomcat 10.1** server running locally on port 8080.

### Deployment Steps

```bash
# 1. Build the WAR
mvn clean package -DskipTests --file clinic-service/pom.xml

# 2. Copy to Tomcat webapps directory
copy clinic-service\target\clinic-service-1.0-SNAPSHOT.war "C:\...\tomcat\webapps\clinic.war"

# 3. Start Tomcat — application auto-deploys
# Frontend served at: http://localhost:8080/clinic-frontend/login.html
# Backend REST API:   http://localhost:8080/clinic/api/
```

### Live Demo Credentials

| Field | Value |
|:---|:---|
| URL | `http://localhost:8080/clinic-frontend/login.html` |
| Username | `admin` |
| Password | `password123` |
| Role | ADMIN (full access) |

> *(Insert screenshot: Browser showing the Sunrise Dental Clinic login page running at localhost:8080)*

> *(Insert screenshot: Dashboard page after successful login, showing the authenticated session)*
