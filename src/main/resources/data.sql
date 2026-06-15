-- =============================================================================
-- Quorix Library Management System — Seed Data
-- Run this AFTER schema.sql
-- =============================================================================
-- DEFAULT LOGIN CREDENTIALS (change these after first login):
--
--   Admin  → username: admin      password: Admin@123
--   Staff  → username: staff      password: Staff@123
--   User   → username: john_doe   password: User@123
--
-- Passwords are BCrypt-hashed (strength 10). To change them, update the
-- password_hash column or use the app's "Forgot Password" / account settings.
-- =============================================================================

USE LibraryDB;
GO

-- -------------------------------------------------------
-- 1. Roles (must be inserted before Users/UserRoles)
-- -------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM Roles WHERE role_name = 'ADMIN')
    INSERT INTO Roles (role_name) VALUES ('ADMIN');

IF NOT EXISTS (SELECT 1 FROM Roles WHERE role_name = 'STAFF')
    INSERT INTO Roles (role_name) VALUES ('STAFF');

IF NOT EXISTS (SELECT 1 FROM Roles WHERE role_name = 'END_USER')
    INSERT INTO Roles (role_name) VALUES ('END_USER');
GO

-- -------------------------------------------------------
-- 2. Seed Users
-- BCrypt hash for "Admin@123"  → $2a$10$7t5z0HaXQdyaNIRf7QY9E.vqtl0WZVRsXBWfCFG.NbJKOlHWPHwAy
-- BCrypt hash for "Staff@123"  → $2a$10$N7vCFd2BVCY6S6PJ0w/kZ.JATW.VtpKUqfq.J4/sGRFJ5ydJUWEG
-- BCrypt hash for "User@123"   → $2a$10$DowJonesXGQqOhSm9lM8mO2F3IyODi5B5DWD5ZMXiHq3hZhDj6qOm
-- -------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM Users WHERE username = 'admin')
    INSERT INTO Users (username, full_name, email, phone, password_hash, role, status)
    VALUES (
        'admin',
        'System Administrator',
        'admin@quorixlibrary.com',
        '+94771234567',
        '$2a$10$7t5z0HaXQdyaNIRf7QY9E.vqtl0WZVRsXBWfCFG.NbJKOlHWPHwAy',
        'ADMIN',
        'ACTIVE'
    );

IF NOT EXISTS (SELECT 1 FROM Users WHERE username = 'staff')
    INSERT INTO Users (username, full_name, email, phone, password_hash, role, status)
    VALUES (
        'staff',
        'Library Staff',
        'staff@quorixlibrary.com',
        '+94777654321',
        '$2a$10$N7vCFd2BVCY6S6PJ0w/kZ.JATW.VtpKUqfq.J4/sGRFJ5ydJUWEG',
        'STAFF',
        'ACTIVE'
    );

IF NOT EXISTS (SELECT 1 FROM Users WHERE username = 'john_doe')
    INSERT INTO Users (username, full_name, email, phone, password_hash, role, status)
    VALUES (
        'john_doe',
        'John Doe',
        'john.doe@example.com',
        '+94770000001',
        '$2a$10$DowJonesXGQqOhSm9lM8mO2F3IyODi5B5DWD5ZMXiHq3hZhDj6qOm',
        'END_USER',
        'ACTIVE'
    );
GO

-- -------------------------------------------------------
-- 3. Link Users to Roles
-- -------------------------------------------------------
-- Admin role
IF NOT EXISTS (
    SELECT 1 FROM UserRoles ur
    JOIN Users u ON ur.user_id = u.user_id
    JOIN Roles r ON ur.role_id = r.role_id
    WHERE u.username = 'admin' AND r.role_name = 'ADMIN'
)
    INSERT INTO UserRoles (user_id, role_id)
    SELECT u.user_id, r.role_id
    FROM Users u, Roles r
    WHERE u.username = 'admin' AND r.role_name = 'ADMIN';

-- Staff role
IF NOT EXISTS (
    SELECT 1 FROM UserRoles ur
    JOIN Users u ON ur.user_id = u.user_id
    JOIN Roles r ON ur.role_id = r.role_id
    WHERE u.username = 'staff' AND r.role_name = 'STAFF'
)
    INSERT INTO UserRoles (user_id, role_id)
    SELECT u.user_id, r.role_id
    FROM Users u, Roles r
    WHERE u.username = 'staff' AND r.role_name = 'STAFF';

-- End user role
IF NOT EXISTS (
    SELECT 1 FROM UserRoles ur
    JOIN Users u ON ur.user_id = u.user_id
    JOIN Roles r ON ur.role_id = r.role_id
    WHERE u.username = 'john_doe' AND r.role_name = 'END_USER'
)
    INSERT INTO UserRoles (user_id, role_id)
    SELECT u.user_id, r.role_id
    FROM Users u, Roles r
    WHERE u.username = 'john_doe' AND r.role_name = 'END_USER';
GO

-- -------------------------------------------------------
-- 4. Sample Books (10 entries covering different categories)
-- -------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM Books WHERE isbn = '9780132350884')
    INSERT INTO Books (title, author, isbn, category, location, status, quantity)
    VALUES ('Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', '9780132350884', 'Software Engineering', 'Shelf A-1', 'Available', 3);

IF NOT EXISTS (SELECT 1 FROM Books WHERE isbn = '9780201633610')
    INSERT INTO Books (title, author, isbn, category, location, status, quantity)
    VALUES ('Design Patterns: Elements of Reusable Object-Oriented Software', 'Gang of Four', '9780201633610', 'Software Engineering', 'Shelf A-2', 'Available', 2);

IF NOT EXISTS (SELECT 1 FROM Books WHERE isbn = '9780596517748')
    INSERT INTO Books (title, author, isbn, category, location, status, quantity)
    VALUES ('JavaScript: The Good Parts', 'Douglas Crockford', '9780596517748', 'Web Development', 'Shelf B-1', 'Available', 4);

IF NOT EXISTS (SELECT 1 FROM Books WHERE isbn = '9781491950357')
    INSERT INTO Books (title, author, isbn, category, location, status, quantity)
    VALUES ('Learning Python', 'Mark Lutz', '9781491950357', 'Programming', 'Shelf B-2', 'Available', 3);

IF NOT EXISTS (SELECT 1 FROM Books WHERE isbn = '9780201485677')
    INSERT INTO Books (title, author, isbn, category, location, status, quantity)
    VALUES ('The Mythical Man-Month', 'Frederick P. Brooks Jr.', '9780201485677', 'Software Engineering', 'Shelf A-3', 'Available', 2);

IF NOT EXISTS (SELECT 1 FROM Books WHERE isbn = '9781484200056')
    INSERT INTO Books (title, author, isbn, category, location, status, quantity)
    VALUES ('Spring Boot in Action', 'Craig Walls', '9781484200056', 'Java', 'Shelf C-1', 'Available', 2);

IF NOT EXISTS (SELECT 1 FROM Books WHERE isbn = '9781492056355')
    INSERT INTO Books (title, author, isbn, category, location, status, quantity)
    VALUES ('Fundamentals of Software Architecture', 'Mark Richards & Neal Ford', '9781492056355', 'Software Engineering', 'Shelf A-4', 'Available', 2);

IF NOT EXISTS (SELECT 1 FROM Books WHERE isbn = '9780134685991')
    INSERT INTO Books (title, author, isbn, category, location, status, quantity)
    VALUES ('Effective Java', 'Joshua Bloch', '9780134685991', 'Java', 'Shelf C-2', 'Available', 3);

IF NOT EXISTS (SELECT 1 FROM Books WHERE isbn = '9781491927282')
    INSERT INTO Books (title, author, isbn, category, location, status, quantity)
    VALUES ('Introducing GitHub', 'Brent Beer', '9781491927282', 'Version Control', 'Shelf D-1', 'Available', 1);

IF NOT EXISTS (SELECT 1 FROM Books WHERE isbn = '9780596803360')
    INSERT INTO Books (title, author, isbn, category, location, status, quantity)
    VALUES ('Beautiful Code: Leading Programmers Explain How They Think', 'Andy Oram & Greg Wilson', '9780596803360', 'Programming', 'Shelf B-3', 'Available', 2);
GO

PRINT 'Seed data inserted successfully.';
PRINT '';
PRINT 'Default login credentials:';
PRINT '  Admin  → username: admin    | password: Admin@123';
PRINT '  Staff  → username: staff    | password: Staff@123';
PRINT '  User   → username: john_doe | password: User@123';
GO
