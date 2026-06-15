-- =============================================================================
-- Quorix Library Management System — Database Schema
-- Database: LibraryDB (Microsoft SQL Server)
-- =============================================================================
-- SETUP INSTRUCTIONS:
--   1. Open SQL Server Management Studio (SSMS)
--   2. Create a new database named "LibraryDB"
--   3. Select LibraryDB and open a New Query window
--   4. Run this entire script (schema.sql) first
--   5. Then run data.sql to seed initial users and sample books
-- =============================================================================

USE LibraryDB;
GO

-- -----------------------------------------------------------------------------
-- Roles Table
-- Stores the three application roles: ADMIN, STAFF, END_USER
-- -----------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Roles' AND xtype='U')
CREATE TABLE Roles (
    role_id   INT IDENTITY(1,1) PRIMARY KEY,
    role_name NVARCHAR(50) NOT NULL UNIQUE
);
GO

-- -----------------------------------------------------------------------------
-- Users Table
-- Core user account table. Passwords are BCrypt-hashed.
-- Status values: ACTIVE, PENDING, INACTIVE
-- Role values:   ADMIN, STAFF, END_USER
-- -----------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Users' AND xtype='U')
CREATE TABLE Users (
    user_id       INT IDENTITY(1,1) PRIMARY KEY,
    username      NVARCHAR(100) NOT NULL UNIQUE,
    full_name     NVARCHAR(200) NOT NULL,
    email         NVARCHAR(200) NOT NULL UNIQUE,
    phone         NVARCHAR(20),
    password_hash NVARCHAR(255) NOT NULL,
    role          NVARCHAR(50)  NOT NULL DEFAULT 'END_USER',
    status        NVARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME2     NOT NULL DEFAULT GETDATE(),
    updated_at    DATETIME2
);
GO

-- -----------------------------------------------------------------------------
-- UserRoles Join Table
-- Links users to their application roles (for Spring Security authority loading)
-- -----------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='UserRoles' AND xtype='U')
CREATE TABLE UserRoles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_userroles_user FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT fk_userroles_role FOREIGN KEY (role_id) REFERENCES Roles(role_id)
);
GO

-- -----------------------------------------------------------------------------
-- Books Table
-- Core book catalog. Status values: Available, Borrowed, Missing
-- -----------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Books' AND xtype='U')
CREATE TABLE Books (
    book_id    INT IDENTITY(1,1) PRIMARY KEY,
    title      NVARCHAR(300) NOT NULL,
    author     NVARCHAR(200) NOT NULL,
    isbn       NVARCHAR(13)  NOT NULL UNIQUE,
    category   NVARCHAR(100),
    location   NVARCHAR(100),
    status     NVARCHAR(50)  NOT NULL DEFAULT 'Available',
    quantity   INT           NOT NULL DEFAULT 1,
    created_at DATETIME2     NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2
);
GO

-- -----------------------------------------------------------------------------
-- Borrowings Table
-- Tracks active and historical book loans.
-- Status values: BORROWED, RETURNED
-- -----------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Borrowings' AND xtype='U')
CREATE TABLE Borrowings (
    borrowing_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id      INT       NOT NULL,
    book_id      INT       NOT NULL,
    borrow_date  DATETIME2 NOT NULL DEFAULT GETDATE(),
    due_date     DATETIME2 NOT NULL,
    return_date  DATETIME2,
    status       NVARCHAR(20) NOT NULL DEFAULT 'BORROWED',
    CONSTRAINT fk_borrowings_user FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT fk_borrowings_book FOREIGN KEY (book_id) REFERENCES Books(book_id)
);
GO

-- -----------------------------------------------------------------------------
-- Reservations Table
-- Holds on available books made by end users.
-- Status values: ACTIVE, CANCELLED, FULFILLED
-- -----------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Reservations' AND xtype='U')
CREATE TABLE Reservations (
    reservation_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id        INT          NOT NULL,
    book_id        INT          NOT NULL,
    reserved_at    DATETIME2    NOT NULL DEFAULT GETDATE(),
    status         NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_reservations_user FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT fk_reservations_book FOREIGN KEY (book_id) REFERENCES Books(book_id)
);
GO

-- -----------------------------------------------------------------------------
-- Waitlist Table
-- Queue for unavailable (quantity = 0) books. FIFO by waitlisted_at.
-- -----------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Waitlist' AND xtype='U')
CREATE TABLE Waitlist (
    waitlist_id   INT IDENTITY(1,1) PRIMARY KEY,
    user_id       INT       NOT NULL,
    book_id       INT       NOT NULL,
    waitlisted_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    notified      BIT       NOT NULL DEFAULT 0,
    CONSTRAINT fk_waitlist_user FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT fk_waitlist_book FOREIGN KEY (book_id) REFERENCES Books(book_id)
);
GO

-- -----------------------------------------------------------------------------
-- Notifications Table
-- In-app notification messages for end users.
-- Status values: UNREAD, READ
-- Type examples: DUE_DATE_REMINDER, OVERDUE_ALERT, RESERVATION_CONFIRMATION,
--                WAITLIST_JOIN, WAITLIST_AVAILABILITY, ANNOUNCEMENT
-- -----------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Notifications' AND xtype='U')
CREATE TABLE Notifications (
    notification_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id         INT           NOT NULL,
    message         NVARCHAR(500) NOT NULL,
    type            NVARCHAR(50)  NOT NULL,
    sent_at         DATETIME2     NOT NULL DEFAULT GETDATE(),
    status          NVARCHAR(10)  NOT NULL DEFAULT 'UNREAD',
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES Users(user_id)
);
GO

-- -----------------------------------------------------------------------------
-- Report Table
-- Audit log of reports generated by admin users.
-- delivery_status values: SUCCESS, PENDING
-- -----------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Report' AND xtype='U')
CREATE TABLE Report (
    report_id              INT IDENTITY(1,1) PRIMARY KEY,
    report_name            NVARCHAR(200) NOT NULL,
    generated_by_user_id   INT           NOT NULL,
    generation_timestamp   DATETIME2     NOT NULL DEFAULT GETDATE(),
    delivery_status        NVARCHAR(20)  NOT NULL DEFAULT 'SUCCESS',
    CONSTRAINT fk_report_user FOREIGN KEY (generated_by_user_id) REFERENCES Users(user_id)
);
GO

-- -----------------------------------------------------------------------------
-- StaffAttendance Table
-- Daily check-in/check-out records for staff and admin users.
-- -----------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='StaffAttendance' AND xtype='U')
CREATE TABLE StaffAttendance (
    attendance_id  INT IDENTITY(1,1) PRIMARY KEY,
    user_id        INT       NOT NULL,
    check_in_time  DATETIME2 NOT NULL DEFAULT GETDATE(),
    check_out_time DATETIME2,
    CONSTRAINT fk_attendance_user FOREIGN KEY (user_id) REFERENCES Users(user_id)
);
GO

PRINT 'Schema created successfully. Run data.sql next to seed initial data.';
GO
