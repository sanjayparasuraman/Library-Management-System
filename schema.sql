CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

CREATE TABLE IF NOT EXISTS books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    author VARCHAR(100) NOT NULL,
    available BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15)
);

CREATE TABLE IF NOT EXISTS issues (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    member_id INT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (member_id) REFERENCES members(id)
);

INSERT INTO books (title, author) VALUES
('Clean Code', 'Robert C. Martin'),
('Effective Java', 'Joshua Bloch'),
('Head First Java', 'Kathy Sierra'),
('Introduction to Algorithms', 'Thomas H. Cormen'),
('Database System Concepts', 'Abraham Silberschatz'),
('Operating System Concepts', 'Abraham Silberschatz'),
('The Pragmatic Programmer', 'Andrew Hunt'),
('Cracking the Coding Interview', 'Gayle Laakmann McDowell');

INSERT INTO members (name, phone) VALUES
('Arun Kumar', '9876543210'),
('Priya Sharma', '9123456780'),
('Karthik Raja', '9988776655');