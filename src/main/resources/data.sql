-- =============================================================================
-- Quorix Library Management System — Seed Data
-- Run this AFTER schema.sql
-- =============================================================================
-- DEFAULT LOGIN CREDENTIALS (change these after first login):
--
--   Admin  → username: admin      password: admin
--   Staff  → username: staff      password: staff
--   User   → username: john_doe   password: password
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
-- BCrypt hash for "admin"      → $2a$10$b/13EiKzAAMt40FaOLGuJ.6Ynhy2bDaSuFm1m4ddPmw2J2JGRfTCS
-- BCrypt hash for "staff"      → $2a$10$SRkGyPo0n7P1dc81DkKTo.AjHAv6GBUBH7B5woobFgOEHRZfW/67q
-- BCrypt hash for "password"   → $2a$10$aHG7IkhCcQnA8lmR6JiI3exYeDoCHXo6MVzheH4iLjsJ8HEdjqB1y
-- -------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM Users WHERE username = 'admin')
    INSERT INTO Users (username, full_name, email, phone, password_hash, role, status)
    VALUES (
        'admin',
        'System Administrator',
        'admin@quorixlibrary.com',
        '+94771234567',
        '$2a$10$b/13EiKzAAMt40FaOLGuJ.6Ynhy2bDaSuFm1m4ddPmw2J2JGRfTCS',
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
        '$2a$10$SRkGyPo0n7P1dc81DkKTo.AjHAv6GBUBH7B5woobFgOEHRZfW/67q',
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
        '$2a$10$aHG7IkhCcQnA8lmR6JiI3exYeDoCHXo6MVzheH4iLjsJ8HEdjqB1y',
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
PRINT '  Admin  → username: admin    | password: admin';
PRINT '  Staff  → username: staff    | password: staff';
PRINT '  User   → username: john_doe | password: password';
GO
