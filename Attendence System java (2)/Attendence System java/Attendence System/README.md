# Attendance Management System

A Java Swing application for managing student attendance in educational institutions.

## Features

- Faculty can mark attendance for students enrolled in their subjects
- Faculty can view attendance reports
- Students can view their own attendance records
- Secure login system for students and faculty

## Setup Instructions

1. Ensure you have Java JDK 8 or higher installed
2. Make sure MySQL is installed and running
3. Update database connection settings in `src/main/java/com/attendance/util/DBConnection.java` if necessary
4. Run the application using your IDE or with the command:
   ```
   javac -cp ".:lib/*" src/main/java/com/attendance/AttendanceManagementSystem.java
   java -cp ".:lib/*" com.attendance.AttendanceManagementSystem
   ```

## Database Setup

The application automatically sets up the database with sample data when it first runs:
- Sample faculty accounts: `faculty1/password` and `faculty2/password`
- Sample student accounts: `student1/password`, `student2/password`, and `student3/password`

## Troubleshooting

If you're experiencing issues with the "Show Students" button not displaying any students:

1. Check the database connection settings in `DBConnection.java`
2. Use the "Debug DB" button on the Mark Attendance panel to diagnose database issues
3. Verify that the database is properly initialized by checking the console output
4. Ensure students are properly enrolled in subjects in the database

## Known Issues and Fixes

- **Students not appearing in Mark Attendance panel**: Click the "SHOW STUDENTS" button after selecting a subject and date. If students still don't appear, use the "Debug DB" button to diagnose the issue.
- **Empty table in attendance panel**: This usually indicates a database connection issue or missing student enrollments.

## License

This project is licensed under the MIT License. 