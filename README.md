# 🚀 MULTI-ROLE-JOB-PORTAL
### *Empowering Careers Through Seamless Role Connections*

<p align="center">
  <img src="https://img.shields.io/github/last-commit/Priyanshu39145/multi-role-job-portal">
  <img src="https://img.shields.io/github/languages/top/Priyanshu39145/multi-role-job-portal">
  <img src="https://img.shields.io/github/languages/count/Priyanshu39145/multi-role-job-portal">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-Framework-brightgreen">
  <img src="https://img.shields.io/badge/Java-Backend-blue">
  <img src="https://img.shields.io/badge/JWT-Security-orange">
  <img src="https://img.shields.io/badge/Swagger-API%20Docs-green">
  <img src="https://img.shields.io/badge/JPA-Hibernate-red">
</p>

---

## 📌 Overview

**Multi-Role Job Portal** is a production-ready backend system that enables **Recruiters** and **Candidates** to interact through a complete hiring workflow.

It provides:
- Secure authentication
- Role-based access
- Job posting & applications
- Shortlisting and hiring
- Search, filtering & pagination
- Swagger-based API testing

This system is designed to mimic how **real hiring platforms** work.

---

## 🧩 Architecture
Controller → Service → Repository → Database  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;↓  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Security  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;↓  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;JWT Filter

Each layer has a single responsibility, making the system:
- Testable
- Scalable
- Maintainable

---

## 🛠️ Tech Stack

| Layer | Technology |
|------|------------|
| Language | Java |
| Framework | Spring Boot |
| Security | Spring Security + JWT |
| Database | MySQL / PostgreSQL |
| ORM | JPA (Hibernate) |
| API Docs | Swagger (OpenAPI 3) |
| Build Tool | Maven |

---

## 🔐 Authentication Flow

1. User registers as **Recruiter** or **Candidate**
2. Logs in using email & password
3. Receives a **JWT Token**
4. Sends token in every protected request  Authorization: Bearer <your_token_here>


---

## ✨ Features

### 🔐 Authentication & Security
- JWT based authentication
- Role-based authorization (RECRUITER & CANDIDATE)
- Secure password hashing
- Stateless API security
- Custom JWT filter
- Global exception handling

### 👤 User Management
- Recruiter registration
- Candidate registration
- Login with JWT token
- Role based access control

### 🧑‍💼 Recruiter Module
- Create & update recruiter profile
- Company creation & assignment
- Post jobs
- Edit & delete jobs
- Close job postings
- View job applications
- Shortlist candidates
- Hire a candidate

### 🧑‍🎓 Candidate Module
- Create & update candidate profile
- Upload resume URL
- Add skills and experience
- Browse jobs
- Search jobs with filters
- Apply to jobs
- View application status

### 💼 Job Management
- Job posting with:
- Title
- Description
- Skills
- Salary
- Experience
- Location
- Employment type
- Pagination & sorting
- Dynamic job search
- Job status: `OPEN / CLOSED`

### 📄 Job Application System
- Apply once per job
- Shortlist candidates
- Hire a candidate
- Automatic job closing when hired
- Application status tracking

---

## 🔍 Job Search Filters

Jobs can be filtered by:
- Location
- Experience
- Employment type
- Company
- Salary
- Skill
- Pagination & sorting

---

## 🔁 Recruiter Flow

1. Register as Recruiter  
2. Login → Receive JWT  
3. Create Recruiter Profile  
4. Create Company (auto created if not exists)  
5. Post Jobs  
6. View Applications  
7. Shortlist Candidates  
8. Hire Candidate  
9. Job closes automatically  

---

## 🔁 Candidate Flow

1. Register as Candidate  
2. Login → Receive JWT  
3. Create Candidate Profile  
4. Browse & Search Jobs  
5. Apply to Job  
6. Track application status  

---

## 📚 Swagger API

Interactive API documentation is available at: http://localhost:8080/swagger-ui/index.html

From here you can:
- Authenticate
- Call all endpoints
- Test the full system

---

## How to Run

1. Configure database in `application.properties`
2. Run:
```bash
mvn clean install
mvn spring-boot:run
```
## Major Entities

- User  
- RecruiterProfile  
- CandidateProfile  
- Company  
- Job  
- JobApplication  


## Security Highlights

- JWT is verified on every request  
- Roles are injected from JWT into Spring Security  
- Endpoints are protected using:
  - Authentication  
  - Role-based access  

- `GlobalExceptionHandler` handles:
  - Access denied  
  - Invalid requests  
  - Duplicate records  
  - Resource not found  
  - Illegal operations  


## Why this project is production-grade

- Clean layered architecture  
- Secure JWT authentication  
- Role-based access control  
- Real business workflow (Hiring)  
- Pagination, filtering, and search  
- Swagger API documentation  
- Proper global exception handling  
- Scalable entity design  


## Author

**Priyanshu Karmakar**  
Backend Developer  
*Java • Spring Boot • Security • REST APIs*




