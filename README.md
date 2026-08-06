# 🚀 MULTI-ROLE-JOB-PORTAL
### *Empowering Careers Through Seamless Role Connections*

<p align="center">
  <img src="https://img.shields.io/github/last-commit/Priyanshu39145/multi-role-job-portal" alt="Last commit">
  <img src="https://img.shields.io/github/languages/top/Priyanshu39145/multi-role-job-portal" alt="Primary language">
  <img src="https://img.shields.io/badge/Java-21-blue" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen" alt="Spring Boot 3.4.1">
</p>

---

## 📌 Overview

**Multi-Role Job Portal** is a Spring Boot REST API for a recruiter-and-candidate hiring workflow. Recruiters create profiles, publish jobs, review ranked applications, and make the final hiring decision. Candidates create profiles, find open jobs, apply, monitor their application history, and withdraw active applications.

The application includes JWT authentication, role-based access control, job search, a weighted ATS-style matching workflow, an auditable application state machine, and safeguards for concurrent job changes.

---

## 🧩 Architecture

```text
Client / Swagger UI
        │
        ▼
Controllers ──► Services ──► Repositories ──► MySQL
        │            │
        │            ├── MatchingService (stateless score calculation)
        │            └── Application state transitions + audit records
        ▼
Spring Security ──► JWTAuthFilter ──► authenticated UserDetails
```

The project follows a layered design:

- **Controllers** expose REST endpoints and validate incoming request bodies.
- **Services** contain authorization checks and business rules. `JobApplicationService` owns application lifecycle changes; `MatchingService` only calculates compatibility scores.
- **Repositories** use Spring Data JPA for persistence and query methods.
- **Entities and DTOs** separate the database model from API request and response payloads.
- **Security** uses stateless Spring Security with JWT and role authorities (`ADMIN`, `RECRUITER`, and `CANDIDATE`).

---

## 🛠️ Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.4.1 |
| Web/API | Spring Web, Bean Validation |
| Security | Spring Security, JWT (JJWT) |
| Database | MySQL |
| Persistence | Spring Data JPA / Hibernate |
| API documentation | springdoc OpenAPI / Swagger UI |
| Mapping | ModelMapper |
| Build tool | Maven |

---

## 🔐 Authentication Flow

1. Register as a **Recruiter** or **Candidate**.
2. Sign in with email and password at `POST /auth/login`.
3. Use the returned JWT in protected requests:

   ```http
   Authorization: Bearer <your_token_here>
   ```

4. The JWT filter authenticates the request and Spring Security enforces endpoint and service-level role/ownership checks.

Newly registered accounts are enabled. The `User` entity implements `UserDetails` and exposes the persisted `enabled` flag through `isEnabled()`, so disabled accounts are not authenticated by Spring Security. The administrative users response also includes this flag. This codebase currently has no public account-enable/account-disable endpoint.

---

## ✨ Features

### 🔐 Authentication & Security

- JWT-based, stateless authentication
- Password hashing with Spring Security
- Role-based authorization for recruiters, candidates, and administrators
- Ownership checks for recruiter jobs and candidate applications
- Account enabled/disabled support through `UserDetails.isEnabled()`
- Centralized exception handling and OpenAPI JWT bearer support

### 👤 Profiles and User Management

- Recruiter and candidate registration
- Recruiter profiles with company association; a company is created when needed
- Candidate profiles with resume URL, skills, experience, location, and salary expectation
- Administrator-only user listing, including each account's `enabled` state

### 💼 Job Management

- Recruiters can create, update, close, and delete their own job postings
- Jobs include required skills, experience, location, salary, employment type, and a configurable minimum match score
- Public job browsing and dynamic search with pagination and sorting
- Search filters: location, minimum experience, employment type, company, minimum salary, and required skill
- `OPEN` and `CLOSED` job states; search results include open jobs only
- Optimistic locking with `@Version` on `Job` to detect concurrent job modifications. The close and hire workflows return a conflict response when a concurrent job modification is detected.

### 📄 Application Lifecycle, Matching, and Audit

- One application per candidate/job while an application is active, hired, or withdrawn
- Weighted ATS-style match scoring, stored as `matchScore` on each `JobApplication`
- Applications for a recruiter job are returned in descending match-score order
- Recruiter confirmation remains required: a qualifying application becomes `SUGGESTED`, then the recruiter explicitly confirms it as `SHORTLISTED`
- Candidate withdrawal with the terminal `WITHDRAWN` status
- Immutable-style application status audit trail through `ApplicationStatusHistory`
- Configurable rejection cooldown before a rejected candidate may apply to the same job again

---

## 🎯 Weighted Candidate Matching

`MatchingService` calculates a score from **0 to 100** without changing persistence or application state itself. It normalizes skills by trimming and comparing case-insensitively.

| Criterion | Weight | Rule |
| --- | ---: | --- |
| Required skills | 60 | Percentage of required skills present in the candidate profile |
| Experience | 25 | Full score when the requirement is met; otherwise a proportional score |
| Location | 10 | Full score for a case-insensitive exact location match |
| Salary | 5 | Full score when the candidate expectation is not above the job salary |

Each job has `minMatchScore`, constrained to `0`–`100`. It defaults to **70.0** when omitted on creation. Recruiters may also update it using the job update endpoint.

When the owning recruiter calls `PATCH /applications/{applicationId}`, the service recalculates and saves the score:

- Score **at or above** the job threshold → `SUGGESTED`
- Score **below** the job threshold → `REJECTED`

`SUGGESTED` is deliberately not a final shortlist. The recruiter reviews the suggested candidate and calls `PATCH /applications/{applicationId}/confirm-shortlist` to move the application to `SHORTLISTED`.

---

## 🔄 Application State Machine

All application status changes go through the private, centralized transition method in `JobApplicationService`. It validates the transition, saves the new status, and writes an `ApplicationStatusHistory` record in the same workflow.

```text
New application ──► APPLIED

APPLIED ──► SUGGESTED ──► SHORTLISTED ──► HIRED
   │             │              │
   ├─────────────┴──────────────┼──► REJECTED
   └────────────────────────────┴──► WITHDRAWN
```

Allowed transitions are:

| Current status | Allowed next status |
| --- | --- |
| `APPLIED` | `SUGGESTED`, `SHORTLISTED`, `REJECTED`, `WITHDRAWN` |
| `SUGGESTED` | `SHORTLISTED`, `REJECTED`, `WITHDRAWN` |
| `SHORTLISTED` | `HIRED`, `REJECTED`, `WITHDRAWN` |
| `HIRED`, `REJECTED`, `WITHDRAWN` | Terminal; no further transitions |

Hiring a shortlisted candidate closes the job and rejects every other non-terminal application for that job. A candidate may withdraw only their own application; the transition rules prevent withdrawal after a terminal decision.

### Reapplication Rules

- A candidate cannot create another application for the same job while a prior application is `APPLIED`, `SUGGESTED`, `SHORTLISTED`, `HIRED`, or `WITHDRAWN`.
- A `REJECTED` application can be retried only after the configured cooldown expires.
- Applications are never accepted for a closed job.

Set the cooldown in `src/main/resources/application.properties`:

```properties
application.rejection-cooldown-days=30
```

`ApplicationProperties` binds this setting as `application.rejection-cooldown-days`; it defaults to `30` and validates that the value is not negative.

---

## 🌐 API Endpoints

Unless noted as public, endpoints require a JWT. Recruiter and candidate operations also verify the caller's role and ownership where appropriate.

| Area | Method | Endpoint | Access / purpose |
| --- | --- | --- | --- |
| Authentication | `POST` | `/register/recruiter` | Public — register a recruiter |
| Authentication | `POST` | `/register/candidate` | Public — register a candidate |
| Authentication | `POST` | `/auth/login` | Public — receive a JWT |
| Users | `GET` | `/users` | Admin — list users, including `enabled` |
| Recruiter profile | `POST` | `/recruiter/profile` | Recruiter — create profile |
| Recruiter profile | `GET` | `/recruiter/profile` | Recruiter — get own profile |
| Recruiter profile | `PUT` | `/recruiter/profile` | Recruiter — update own profile |
| Candidate profile | `POST` | `/candidate/profile` | Candidate — create profile |
| Candidate profile | `GET` | `/candidate/profile` | Candidate — get own profile |
| Candidate profile | `PUT` | `/candidate/profile` | Candidate — update own profile |
| Jobs | `POST` | `/jobs` | Recruiter — create a job |
| Jobs | `GET` | `/jobs?page={page}&size={size}` | Public — paginated job list |
| Jobs | `GET` | `/jobs/{jobId}` | Public — job details |
| Jobs | `PUT` | `/jobs/{jobId}` | Owning recruiter — partial field update payload |
| Jobs | `DELETE` | `/jobs/{jobId}` | Owning recruiter — delete a job |
| Jobs | `PATCH` | `/jobs/{jobId}/close` | Owning recruiter — close a job |
| Jobs | `GET` | `/jobs/search` | Public — filtered, paginated, sorted open jobs |
| Applications | `POST` | `/jobs/{jobId}/apply` | Candidate — apply to an open job |
| Applications | `GET` | `/candidate/applications` | Candidate — own applications |
| Applications | `PATCH` | `/candidate/applications/{applicationId}/withdraw` | Candidate — withdraw own eligible application |
| Applications | `GET` | `/applications/{applicationId}/history` | Candidate owner or owning recruiter — audit history |
| Applications | `GET` | `/recruiter/jobs/{jobId}/applications` | Owning recruiter — all job applications, ranked by score |
| Applications | `PATCH` | `/applications/{applicationId}` | Owning recruiter — calculate match score and suggest/reject |
| Applications | `PATCH` | `/applications/{applicationId}/confirm-shortlist` | Owning recruiter — confirm a suggested shortlist |
| Applications | `GET` | `/applications/shortListed/{jobId}` | Owning recruiter — shortlisted applications |
| Applications | `PATCH` | `/recruiter/applications/{applicationId}/hire` | Owning recruiter — hire shortlisted candidate and close job |

For `/jobs/search`, use optional query parameters `location`, `minExperience`, `employmentType`, `company`, `minSalary`, `skill`, `page`, `size`, `sortBy`, and `direction`.

---

## 📦 API DTOs and Persistence Model

### Key DTOs

| DTO | Responsibility |
| --- | --- |
| `RegisterRequestDTO`, `RegisterResponseDTO`, `LoginResponseDTO` | Registration and JWT login payloads |
| `RecruiterProfileRequestDTO`, `RecruiterProfileResponseDTO` | Recruiter profile input and output |
| `CandidateProfileRequestDTO`, `CandidateProfileResponseDTO` | Candidate profile input and output, including matching attributes |
| `JobRequestDTO`, `JobResponseDTO` | Job payloads; include `minMatchScore` in addition to standard job details |
| `JobApplicationResponseDTO` | Application ID, job title, status, and calculated `matchScore` |
| `ApplicationStatusHistoryResponseDTO` | Audit event: previous/new status, actor email, timestamp, and note |
| `UserDTO` | User ID, email, role, and `enabled` account state |

### Major Entities

| Entity | Description |
| --- | --- |
| `User` | Login identity, role, password hash, enabled state, and one role profile |
| `RecruiterProfile` | Recruiter contact details, company, and jobs created |
| `CandidateProfile` | Candidate resume, skills, experience, location, salary expectation, and applications |
| `Company` | Company details shared by recruiters and jobs |
| `Job` | Job requirements, configured `minMatchScore`, open/closed state, and `@Version` concurrency column |
| `JobApplication` | Links a candidate and job; holds current status, `matchScore`, timestamps, and status-history collection |
| `ApplicationStatusHistory` | One auditable status change with the previous status, destination status, actor, timestamp, and note |

### Repositories and Configuration

- `ApplicationStatusHistoryRepository` retrieves an application's history in chronological order.
- `JobApplicationRepository` adds status-aware existence checks for reapplication rules, a rejected-application cooldown query, and `findAllByJobOrderByMatchScoreDesc` for recruiter ranking.
- `ApplicationProperties` provides the validated `application.rejection-cooldown-days` setting.

---

## 🔁 Business Workflow

### Recruiter flow

1. Register and log in as a recruiter.
2. Create a recruiter profile and company association.
3. Create a job, optionally choosing its minimum match score.
4. View job applications ranked by `matchScore`.
5. Score an application. A qualifying candidate becomes `SUGGESTED`.
6. Review and confirm a suggestion as `SHORTLISTED`.
7. Hire a shortlisted candidate. The job closes and remaining non-terminal applications are rejected.
8. Review the application audit trail whenever needed.

### Candidate flow

1. Register and log in as a candidate.
2. Create a profile with accurate skills, experience, location, and salary expectation.
3. Browse or search open jobs.
4. Apply to a job.
5. Track the application status and its history.
6. Withdraw an eligible application when necessary, or reapply after a rejection cooldown has passed.

---

## 📚 Swagger API

Interactive API documentation is available while the application is running:

- Swagger UI: <http://localhost:8080/docs>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

Use Swagger's **Authorize** button to supply a JWT bearer token before calling protected endpoints.

---

## How to Run

1. Install Java 21, Maven, and MySQL.
2. Create a MySQL database (the default configuration uses `JOBPORTAL`).
3. Configure datasource and JWT settings in `src/main/resources/application.properties`. Keep credentials and signing secrets outside source control in a real deployment.
4. Optionally set `application.rejection-cooldown-days`.
5. Start the application:

   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

6. Open <http://localhost:8080/docs> to explore the API.

---

## Security Highlights

- JWTs are verified for protected requests by `JWTAuthFilter`.
- The application uses stateless sessions and password hashing.
- Spring Security resolves roles as `ROLE_ADMIN`, `ROLE_RECRUITER`, and `ROLE_CANDIDATE`.
- Disabled users are rejected by the `UserDetails` account-state check.
- Recruiter job/application ownership and candidate application ownership are checked in service logic.
- `GlobalExceptionHandler` provides consistent error responses for authorization failures, invalid requests, duplicates, missing resources, and invalid operations.

---

## Author

**Priyanshu Karmakar**  
Backend Developer  
*Java • Spring Boot • Security • REST APIs*
