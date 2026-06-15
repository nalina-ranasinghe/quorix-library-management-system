USE LibraryDB;
GO

-- Disable foreign key constraints temporarily to allow clearing tables
EXEC sp_MSforeachtable "ALTER TABLE ? NOCHECK CONSTRAINT all"
GO

DELETE FROM Waitlist;
DELETE FROM Report;
DELETE FROM StaffAttendance;
DELETE FROM Notifications;
DELETE FROM Borrowings;
DELETE FROM Reservations;
DELETE FROM UserRoles;
DELETE FROM Users;
DELETE FROM Books;
DELETE FROM Roles;
IF OBJECT_ID('SystemLogs', 'U') IS NOT NULL DELETE FROM SystemLogs;
GO

-- Re-enable foreign key constraints
EXEC sp_MSforeachtable "ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all"
GO

-- Reseed identity columns if necessary (optional but good practice)
IF OBJECT_ID('SystemLogs', 'U') IS NOT NULL DBCC CHECKIDENT ('SystemLogs', RESEED, 0);
DBCC CHECKIDENT ('Notifications', RESEED, 0);
DBCC CHECKIDENT ('Borrowings', RESEED, 0);
DBCC CHECKIDENT ('Reservations', RESEED, 0);
DBCC CHECKIDENT ('Users', RESEED, 0);
DBCC CHECKIDENT ('Books', RESEED, 0);
DBCC CHECKIDENT ('Roles', RESEED, 0);
GO

IF OBJECT_ID('SystemLogs', 'U') IS NULL
BEGIN
    CREATE TABLE SystemLogs (
        log_id INT PRIMARY KEY IDENTITY(1,1),
        user_id INT NULL,
        action NVARCHAR(100) NOT NULL,
        details NVARCHAR(255),
        created_at DATETIME NOT NULL DEFAULT GETDATE(),
        FOREIGN KEY (user_id) REFERENCES Users(user_id)
    );
END
GO

-- ====================================
-- SAMPLE DATA INSERTS
-- ====================================

-- Roles
INSERT INTO Roles (role_name) VALUES 
('ADMIN'), 
('STAFF'), 
('END_USER');

-- Users
-- Replaced Password123! with BCrypt hash: $2a$10$AgYCcSa0nDkqtIuOfkRKvuaHLrGFZMhvC..zmk6JfC9MXWXg13Jz2
INSERT INTO Users (username, full_name, email, phone, password_hash, role)
VALUES
('nimal.perera', 'Nimal Perera', 'nimal@example.com', '+94771234567', '$2a$10$AgYCcSa0nDkqtIuOfkRKvuaHLrGFZMhvC..zmk6JfC9MXWXg13Jz2', 'ADMIN'),
('dilani.jayawardena', 'Dilani Jayawardena', 'dilani@example.com', '+94771234568', '$2a$10$AgYCcSa0nDkqtIuOfkRKvuaHLrGFZMhvC..zmk6JfC9MXWXg13Jz2', 'ADMIN'),
('roshan.silva', 'Roshan Silva', 'roshan@example.com', '+94771234569', '$2a$10$AgYCcSa0nDkqtIuOfkRKvuaHLrGFZMhvC..zmk6JfC9MXWXg13Jz2', 'STAFF'),
('harshani.fernando', 'Harshani Fernando', 'harshani@example.com', '+94771234570', '$2a$10$AgYCcSa0nDkqtIuOfkRKvuaHLrGFZMhvC..zmk6JfC9MXWXg13Jz2', 'STAFF'),
('sanjeewa.kumara', 'Sanjeewa Kumara', 'sanjeewa@example.com', '+94771234571', '$2a$10$AgYCcSa0nDkqtIuOfkRKvuaHLrGFZMhvC..zmk6JfC9MXWXg13Jz2', 'END_USER'),
('tharindu.jayasena', 'Tharindu Jayasena', 'tharindu@example.com', '+94771234572', '$2a$10$AgYCcSa0nDkqtIuOfkRKvuaHLrGFZMhvC..zmk6JfC9MXWXg13Jz2', 'END_USER');

-- UserRoles (linking Users to Roles)
INSERT INTO UserRoles (user_id, role_id)
SELECT user_id, (SELECT role_id FROM Roles WHERE role_name = Users.role)
FROM Users;

-- Books
INSERT INTO Books (title, author, isbn, category, location, quantity)
VALUES 
('Atomic Habits', 'James Clear', '9780735211292', 'Self-Help', 'Shelf A1', 5),
('Think Like a Monk', 'Jay Shetty', '9781982134488', 'Self-Help', 'Shelf A2', 4),
('48 Laws of Power', 'Robert Greene', '9780140280197', 'Strategy', 'Shelf A3', 3),
('Dopamine Detox', 'Thibaut Meurisse', '9781774922228', 'Self-Help', 'Shelf A4', 2),
('Rich Dad Poor Dad', 'Robert Kiyosaki', '9781612680194', 'Finance', 'Shelf A5', 6),
('The Subtle Art of Not Giving a F*ck', 'Mark Manson', '9780062457714', 'Self-Help', 'Shelf A6', 4),
('The Psychology of Money', 'Morgan Housel', '9780857197689', 'Finance', 'Shelf A7', 5),
('5 a.m Club', 'Robin Sharma', '9781443456623', 'Self-Help', 'Shelf A8', 3),
('Mindset', 'Carol Dweck', '9780345472328', 'Self-Help', 'Shelf B1', 4),
('The Power of Habit', 'Charles Duhigg', '9780812981605', 'Self-Help', 'Shelf B2', 5),
('Deep Work', 'Cal Newport', '9781455586691', 'Productivity', 'Shelf B3', 3),
('Grit', 'Angela Duckworth', '9781501111112', 'Self-Help', 'Shelf B4', 4),
('The 7 Habits of Highly Effective People', 'Stephen R. Covey', '9780743269513', 'Self-Help', 'Shelf B5', 6),
('Principles', 'Ray Dalio', '9781501124020', 'Finance', 'Shelf B6', 3),
('The One Thing', 'Gary Keller', '9781885167774', 'Productivity', 'Shelf B7', 4),
('Essentialism', 'Greg McKeown', '9780804137386', 'Productivity', 'Shelf B8', 3),
('Thinking, Fast and Slow', 'Daniel Kahneman', '9780374533557', 'Psychology', 'Shelf B9', 5),
('The Millionaire Next Door', 'Thomas J. Stanley', '9781589795471', 'Finance', 'Shelf B10', 4),
('Tools of Titans', 'Tim Ferriss', '9781328683786', 'Self-Help', 'Shelf B11', 3),
('The War of Art', 'Steven Pressfield', '9781936891023', 'Self-Help', 'Shelf B12', 3);

-- Borrowings
INSERT INTO Borrowings (user_id, book_id, borrow_date, due_date, status)
VALUES
(5, 1, GETDATE(), DATEADD(DAY, 14, GETDATE()), 'BORROWED'),  
(6, 2, GETDATE(), DATEADD(DAY, 14, GETDATE()), 'BORROWED'),  
(5, 5, GETDATE(), DATEADD(DAY, 14, GETDATE()), 'BORROWED'),  
(6, 6, GETDATE(), DATEADD(DAY, 14, GETDATE()), 'BORROWED');  

-- Reservations
INSERT INTO Reservations (user_id, book_id, reserved_at, status)
VALUES
(5, 3, GETDATE(), 'ACTIVE'),   
(6, 4, GETDATE(), 'ACTIVE'),   
(5, 7, GETDATE(), 'ACTIVE'),   
(6, 10, GETDATE(), 'ACTIVE');  

-- Notifications
INSERT INTO Notifications (user_id, message, type, status)
VALUES
(5, 'Your book "Atomic Habits" is due in 3 days', 'DUE_DATE_REMINDER', 'UNREAD'),
(6, 'Your reservation for "Think Like a Monk" is confirmed', 'RESERVATION_UPDATE', 'UNREAD');

-- System Logs
INSERT INTO SystemLogs (user_id, action, details)
VALUES
(5, 'BORROW_BOOK', 'Borrowed Atomic Habits'),
(6, 'BORROW_BOOK', 'Borrowed Think Like a Monk'),
(5, 'RESERVE_BOOK', 'Reserved 48 Laws of Power'),
(6, 'RESERVE_BOOK', 'Reserved Dopamine Detox');
GO
