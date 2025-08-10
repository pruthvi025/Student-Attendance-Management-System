# Attendance Management System - Usage Guide

This guide will help you understand how to use the Attendance Management System.

## Getting Started

1. Make sure you have MySQL database set up and running
2. Run the SQL script in `src/main/resources/database.sql` to create the database and tables
3. Ensure the database connection settings in `com.attendance.util.DBConnection` match your MySQL setup
4. Run the application using Maven or the generated JAR file

## Login

- Use one of the following sample login credentials:
  - **Faculty**: username: `faculty1`, password: `password`
  - **Student**: username: `student1`, password: `password`

## Faculty Features

### Mark Attendance

1. From the Faculty Dashboard, click the "Mark Attendance" menu item
2. Select a subject from the dropdown menu
3. Choose a date using the date selection dropdowns
4. Click "Show Students" to display the list of students enrolled in that subject
5. Check the "Present" box for students who are present
6. Click "Save Attendance" to record the attendance

### View Attendance Reports

1. From the Faculty Dashboard, click the "View Attendance Reports" menu item
2. Select a subject from the dropdown menu
3. Choose a report type (Daily, Weekly, or Monthly)
4. Click "Generate Report" to view the attendance data
5. The report will show student details, including:
   - Roll number
   - Name
   - Total classes held
   - Number of classes attended
   - Attendance percentage
   - Status (Good Standing or Low Attendance)
6. Students with attendance below 75% will be highlighted in red
7. You can export the report to a CSV file by clicking "Export to CSV"

## Student Features

### View Personal Attendance

1. From the Student Dashboard, click the "My Attendance" menu item
2. The system will display your attendance for all subjects you're enrolled in
3. You can see your attendance percentage and status for each subject
4. Subjects where your attendance is below 75% will be highlighted in red

## Database Structure

The system uses the following main tables:
- `users`: Stores login credentials and basic user information
- `faculty`: Stores faculty details linked to user accounts
- `students`: Stores student details linked to user accounts
- `subjects`: Stores course/subject information
- `student_subjects`: Links students to the subjects they're enrolled in
- `attendance`: Records attendance data (student, subject, date, present/absent)

## Need Help?

If you encounter any issues:
1. Check that your database connection is working correctly
2. Verify that you've run the SQL script to create the necessary tables
3. Make sure you're using the correct login credentials
4. Check that the JCalendar library is properly included in your classpath

For more technical information, refer to the main README.md file. 