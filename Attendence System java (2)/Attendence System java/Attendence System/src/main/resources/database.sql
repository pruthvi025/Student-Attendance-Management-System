-- Drop existing tables if they exist (to ensure clean state)
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS student_subjects;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS faculty;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,  -- 'student' or 'faculty' or 'admin'
    name VARCHAR(100) NOT NULL
);

-- Create faculty table
CREATE TABLE IF NOT EXISTS faculty (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create students table
CREATE TABLE IF NOT EXISTS students (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    roll_no VARCHAR(20) NOT NULL UNIQUE,
    course VARCHAR(50) NOT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create subjects table
CREATE TABLE IF NOT EXISTS subjects (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    faculty_id INT NOT NULL,
    FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE CASCADE
);

-- Create student_subjects table for many-to-many relationship
CREATE TABLE IF NOT EXISTS student_subjects (
    id INT PRIMARY KEY,
    student_id INT NOT NULL,
    subject_id INT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    UNIQUE (student_id, subject_id)
);

-- Create attendance table with explicit ID handling
CREATE TABLE IF NOT EXISTS attendance (
    id INT PRIMARY KEY,
    student_id INT NOT NULL,
    subject_id INT NOT NULL,
    date DATE NOT NULL,
    present BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    UNIQUE (student_id, subject_id, date)
);

-- Add a few sample attendance records to initialize the table
INSERT INTO attendance (id, student_id, subject_id, date, present) VALUES
(1, 1, 1, '2023-05-01', true),  -- Alice was present in DBMS on May 1
(2, 2, 1, '2023-05-01', false); -- Bob was absent in DBMS on May 1

-- Insert sample data

-- Sample users (clean IDs starting from 1)
INSERT INTO users (id, username, password, role, name) VALUES
(1, 'faculty1', 'password', 'faculty', 'John Smith'),
(2, 'faculty2', 'password', 'faculty', 'Jane Doe'),
(3, 'student1', 'password', 'student', 'Alice Johnson'),
(4, 'student2', 'password', 'student', 'Bob Miller'),
(5, 'student3', 'password', 'student', 'Charlie Davis'),
(6, 'admin', 'admin123', 'admin', 'System Administrator');

-- Sample faculty
INSERT INTO faculty (id, name, department, user_id) VALUES
(1, 'John Smith', 'Computer Science', 1),
(2, 'Jane Doe', 'Mathematics', 2);

-- Sample students
INSERT INTO students (id, name, roll_no, course, user_id) VALUES
(1, 'Alice Johnson', 'CS001', 'B.Tech Computer Science', 3),
(2, 'Bob Miller', 'CS002', 'B.Tech Computer Science', 4),
(3, 'Charlie Davis', 'CS003', 'B.Tech Computer Science', 5);

-- Sample subjects
INSERT INTO subjects (id, name, code, faculty_id) VALUES
(1, 'Database Management Systems', 'CS-301', 1),
(2, 'Data Structures and Algorithms', 'CS-201', 1),
(3, 'Linear Algebra', 'MA-101', 2);

-- Assign subjects to students (ensuring clean IDs)
INSERT INTO student_subjects (id, student_id, subject_id) VALUES
(1, 1, 1), -- Alice takes DBMS
(2, 1, 2), -- Alice takes DSA
(3, 1, 3), -- Alice takes Linear Algebra
(4, 2, 1), -- Bob takes DBMS
(5, 2, 2), -- Bob takes DSA
(6, 3, 2), -- Charlie takes DSA
(7, 3, 3); -- Charlie takes Linear Algebra 