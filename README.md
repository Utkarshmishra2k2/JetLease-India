# ✈️ JetLease India

### Private Aviation Booking & Aircraft Lease Management System

JetLease India is a multi-technology software project for managing **private aircraft booking, payments, lease agreements, customer accounts, and administrative operations**.

The project demonstrates the same business problem through four different implementations.

---

## 📌 Problem Statement

Private aviation booking and aircraft leasing involve multiple activities such as aircraft selection, customer registration, booking, document verification, payment verification, lease generation, and administrative approval.

Managing these activities manually or through disconnected systems can lead to data inconsistency, difficult tracking, and inefficient operations.

**JetLease India provides a centralized system to manage the complete booking-to-lease lifecycle.**

---

## 🎯 Objectives

* Provide an easy aircraft browsing and booking system.
* Manage customer registration and authentication.
* Manage aircraft and availability.
* Implement booking and passenger management.
* Provide Aadhaar/pilot-license mock verification.
* Manage payment submission and verification.
* Generate and manage lease agreements.
* Allow customers to sign leases.
* Provide admin approval/rejection workflows.
* Provide customer dashboards and notifications.
* Provide reports and administrative management.

---

## 🔄 Main Workflow

```text
Register
   ↓
Login
   ↓
Browse Aircraft
   ↓
Create Booking
   ↓
Document Verification
   ↓
Payment
   ↓
Admin Payment Verification
   ↓
Lease Generation
   ↓
Customer Signs Lease
   ↓
Admin Approval / Rejection
   ↓
Final Booking Status
```

---

## 🧩 Project Implementations

### 1. Python Full Stack

**Frontend:** Angular + TypeScript
**Backend:** Python + FastAPI
**Database:** SQLite + SQLAlchemy

```text
Angular → FastAPI → SQLAlchemy → SQLite
```

---

### 2. Java Full Stack

**Frontend:** Angular + TypeScript
**Backend:** Java + Spring Boot
**Build Tool:** Maven
**Database:** Relational Database

```text
Angular → Spring Boot → JPA/Repository → Database
```

---

### 3. Java JDBC Console Application

**Technology:** Java + JDBC + SQL

```text
Java Console → JDBC → Database
```

This implementation demonstrates the core JetLease operations through a console-based application.

---

### 4. Frontend Prototype

**Technology:** HTML + CSS + Vanilla JavaScript + JSON + localStorage

```text
HTML/CSS/JS → localStorage
```

This was the original frontend-only prototype used to demonstrate the UI and business workflow.

---

## ⭐ Key Features

* ✈️ Aircraft Fleet Management
* 👤 Customer Registration & Login
* 📅 Flight Booking
* 👥 Passenger Management
* 🪪 Mock Aadhaar Verification
* 🧑‍✈️ Pilot License Verification
* 💰 Payment Management
* 📄 Lease Generation
* ✍️ Lease Signing
* 🔔 Notifications
* 📊 Customer Dashboard
* 🛠️ Admin Portal
* 👨‍✈️ Crew Management
* 📈 Reports & CSV Export
* ❌ Booking Cancellation
* ✅ Lease Approval / Rejection

---

## 🛠️ Technologies

| Area            | Technologies                               |
| --------------- | ------------------------------------------ |
| Frontend        | HTML, CSS, JavaScript, Angular, TypeScript |
| Python Backend  | FastAPI, SQLAlchemy, Pydantic              |
| Java Backend    | Spring Boot, JPA, Maven                    |
| Database        | SQLite / Relational Database               |
| Database Access | SQLAlchemy / JDBC / JPA                    |
| API             | REST                                       |

---

## 📁 Repository Structure

```text
JetLease-India/
│
├── 01-Python-FullStack/
│   ├── Backend/
│   └── Frontend/
│
├── 02-Java-FullStack/
│   ├── backend/
│   └── frontend/
│
├── 03-Java-JDBC-Console/
│
├── 04-Frontend-Prototype/
│
└── README.md
```

---

## ▶️ Running the Projects

### Python Full Stack

Backend:

```bash
cd 01-Python-FullStack/Backend
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8080
```

Frontend:

```bash
cd 01-Python-FullStack/Frontend
npm install
ng serve
```

Backend API:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/docs
```

The Python backend uses FastAPI, Uvicorn, SQLAlchemy and SQLite.

### Java Full Stack

Backend:

```bash
cd 02-Java-FullStack/backend
mvn spring-boot:run
```

Frontend:

```bash
cd 02-Java-FullStack/frontend
npm install
ng serve
```

### Frontend Prototype

```bash
cd 04-Frontend-Prototype
python3 -m http.server 8000
```

Open:

```text
http://localhost:8000
```

The original prototype can also be opened directly through `index.html`.

### Java JDBC

Configure the database connection and run the Java console application from:

```text
03-Java-JDBC-Console/
```

### ✈️ JetLease India

**One Business Problem • Four Implementations • Multiple Technology Stacks**
