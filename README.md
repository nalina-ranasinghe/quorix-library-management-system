<div align="center">

# 📚 Quorix Library Management System

**A full-stack web application for managing a digital library — built by a team of six university students.**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.5-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![SQL Server](https://img.shields.io/badge/SQL_Server-2019+-CC2927?style=for-the-badge&logo=microsoftsqlserver&logoColor=white)](https://www.microsoft.com/sql-server)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)](https://getbootstrap.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

</div>

---

## 📖 Overview

**Quorix** is a comprehensive, role-based Library Management System developed as a second-year group project at the **Sri Lanka Institute of Information Technology (SLIIT)**. It provides a complete solution for managing library operations — from book cataloging and member borrowing to automated notifications and administrative reporting.

The system supports three distinct user roles, each with a dedicated workflow and interface, backed by a secure Spring Security authentication layer.

---

## ✨ Features

### 🔴 Admin Portal
| Feature | Description |
|---|---|
| **Dashboard** | Summary cards showing total borrowings and new users (last 30 days) |
| **User Management** | Create, edit, delete, view, and approve/reject user registrations |
| **Operational Reports** | Overdue book logs, book availability, and today's staff attendance |
| **Usage Reports** | Most popular books and top borrowing users |
| **Email Reports** | Send full operational reports directly to any email |
| **Announcements** | Broadcast system-wide announcements to all users via in-app + email |
| **Report Audit Log** | Every generated report is timestamped and logged |

### 🟡 Staff Portal
| Feature | Description |
|---|---|
| **Library Management** | Central dashboard for all day-to-day operations |
| **Book Catalog** | Add, update, delete, and search the full book inventory |
| **Borrow a Book** | Issue loans to members — enforces a 5-book borrowing limit |
| **Return a Book** | Process returns, update quantity, and trigger waitlist notifications |
| **Renew a Loan** | Extend loan due dates directly from the management interface |
| **Search Books** | Find books by title, author, or category |
| **Missing Books** | View all books with status "Missing" |

### 🟢 End User Portal
| Feature | Description |
|---|---|
| **Browse Books** | Browse and search the full book catalog as a guest or member |
| **Reserve a Book** | Place a hold on any available book |
| **Join Waitlist** | Auto-join the waitlist when a book is at zero quantity |
| **My Books** | View all active loans, reservations, and waitlist positions |
| **Self-Renew** | Renew personal loans directly from the "My Books" page |
| **Cancel** | Cancel active reservations or waitlist positions |
| **Notifications** | Receive due-date reminders, overdue alerts, and availability alerts |
| **Account Settings** | Update profile details and change password |
| **Forgot Password** | Self-service password reset via username + email verification |

### ⚙️ System Features
- 🔒 **Spring Security** with BCrypt password hashing and role-based access control
- 📧 **Automated Email Notifications** via Gmail SMTP (App Password)
- ⏰ **Scheduled Jobs** — daily 8 AM cron to send due-date and overdue alerts
- 📋 **Waitlist Queue** — FIFO; auto-notifies the next user when a book is returned
- 📊 **Report Email Delivery** — sends full operational reports as HTML emails

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Web Layer (Thymeleaf + Bootstrap)        │
│   index.html | home.html | library-management.html | admin/*   │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP
┌──────────────────────────▼──────────────────────────────────────┐
│                     Spring MVC Controllers                      │
│  ViewController | BookController | LibraryController            │
│  AdminController | UserController | NotificationController      │
│  AuthController | SelfEndUserRenewController                    │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                        Service Layer                            │
│  BookService | BorrowService | BorrowingService | UserService   │
│  ReservationService | WaitlistService | NotificationService     │
│  ReportService | EmailService | NotificationSchedulerService    │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│               Repository Layer (JdbcTemplate)                   │
│  BookRepository | BorrowingRepository | UserRepository          │
│  ReservationRepository | WaitlistRepository | NotificationRepo  │
│  ReportRepository | ReportLogRepository | RoleRepository        │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│              Microsoft SQL Server — LibraryDB                   │
│  Users | Roles | UserRoles | Books | Borrowings | Reservations  │
│  Waitlist | Notifications | Report | StaffAttendance            │
└─────────────────────────────────────────────────────────────────┘
```

> **Note:** The project uses raw `JdbcTemplate` with custom `RowMapper`s rather than JPA/Hibernate entity mapping — a deliberate learning choice.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend Framework | Spring Boot 3.5.5 |
| Language | Java 21 |
| Security | Spring Security 6 (BCrypt, Role-Based) |
| Template Engine | Thymeleaf 3 + `thymeleaf-extras-springsecurity6` |
| Database | Microsoft SQL Server 2019+ |
| DB Access | Spring `JdbcTemplate` (no ORM) |
| Email | Spring Mail + Gmail SMTP |
| Frontend | Bootstrap 5.3.3 + Vanilla CSS & JS |
| Build Tool | Maven |
| Lombok | For boilerplate reduction |

---

## 🚀 Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **[Java JDK 21](https://adoptium.net/)** or later
- **[Apache Maven 3.9+](https://maven.apache.org/download.cgi)**
- **[Microsoft SQL Server](https://www.microsoft.com/en-us/sql-server/sql-server-downloads)** (Developer or Express edition)
- **[SQL Server Management Studio (SSMS)](https://learn.microsoft.com/en-us/sql/ssms/download-sql-server-management-studio-ssms)** (recommended for DB setup)

---

### Installation

#### Step 1 — Clone the repository

```bash
git clone https://github.com/nalina-ranasinghe/quorix-library-management-system.git
cd quorix-library-management-system
```

#### Step 2 — Set up the database

1. Open **SQL Server Management Studio (SSMS)** and connect to your local SQL Server instance.
2. Create a new database called `LibraryDB`:
   ```sql
   CREATE DATABASE LibraryDB;
   ```
3. In SSMS, select the `LibraryDB` database and open a **New Query** window.
4. Open and run `src/main/resources/schema.sql` — this creates all required tables.
5. Then run `src/main/resources/data.sql` — this seeds the initial users and sample books.

#### Step 3 — Configure the application

Copy the example configuration file and fill in your local settings:

```bash
# Windows
copy src\main\resources\application.properties.example src\main\resources\application.properties

# macOS / Linux
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Then open `src/main/resources/application.properties` and update:

```properties
# Your SQL Server credentials
spring.datasource.username=YOUR_DB_USERNAME   # e.g., sa
spring.datasource.password=YOUR_DB_PASSWORD   # your SA or Windows Auth password

# Optional: Gmail for email notifications
spring.mail.username=YOUR_GMAIL_ADDRESS
spring.mail.password=YOUR_GMAIL_APP_PASSWORD  # See: https://support.google.com/accounts/answer/185833
```

> **Tip:** If you don't configure email, all core features (borrowing, reservations, admin) still work. Only email notifications and password-reset emails will be disabled.

#### Step 4 — Run the application

```bash
mvn spring-boot:run
```

The application will start at: **http://localhost:8080**

---

### Default Login Credentials

After running `data.sql`, use these accounts to explore the system:

| Role | Username | Password | Access |
|---|---|---|---|
| **Admin** | `admin` | `Admin@123` | `/admin` — full admin dashboard |
| **Staff** | `staff` | `Staff@123` | `/staff` — library management |
| **End User** | `john_doe` | `User@123` | `/home` — user portal |

> ⚠️ **Change these passwords** after your first login via Account Settings.

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/library/app/
│   │   ├── config/          # Security config, role-based login handler
│   │   ├── controller/      # All MVC + REST controllers
│   │   ├── dto/             # Data Transfer Objects for views and reports
│   │   ├── entity/          # Plain Java model classes
│   │   ├── repository/      # JdbcTemplate-based data access layer
│   │   └── service/         # Business logic, scheduling, email
│   └── resources/
│       ├── static/
│       │   ├── css/         # login.css, home.css, dashboard.css
│       │   ├── js/          # auth.js, home.js, dashboard.js
│       │   └── images/      # Quorix logo and assets
│       ├── templates/       # Thymeleaf HTML templates
│       │   ├── admin/       # Admin-only pages
│       │   └── emails/      # Email HTML templates
│       ├── schema.sql           # Database DDL — create all tables
│       ├── data.sql             # Seed data — default users & sample books
│       └── application.properties.example  # Config template (copy → .properties)
```

---

## 👥 Team & Contributions

This project was developed by **Group 16** as part of the *IT2080 — Object Oriented Programming* module in Year 2, Semester 1 at SLIIT.

| Member | Role |
|---|---|
| **Nalina Ranasinghe** (Team Lead) | User Account Management — registration flow, role-based authentication, approval system, password reset, account settings |
| Member 2 | *(Contribution area)* |
| Member 3 | *(Contribution area)* |
| Member 4 | *(Contribution area)* |
| Member 5 | *(Contribution area)* |
| Member 6 | *(Contribution area)* |

> 📄 Full project documentation and final report are available in the [`Documentation/`](Documentation/) folder.

---

## 📸 Screenshots

> *Screenshots and demo recordings can be found in the [`Documentation/`](Documentation/) folder.*

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

Made with ❤️ by Group 16 — SLIIT, 2025

</div>
