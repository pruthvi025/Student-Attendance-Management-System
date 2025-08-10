# Attendance Management System - Troubleshooting Guide

## Common Issues and Solutions

### Database Connection Issues

#### Error: "Access denied for user 'root'@'localhost'"

**Cause:** Your MySQL server requires a password, but none is provided in the code.

**Solution:**
1. Open `src/main/java/com/attendance/util/DBConnection.java`
2. Update the password field with your actual MySQL root password:
   ```java
   private static final String PASSWORD = "your_actual_password_here";
   ```
3. Save the file and rerun the application

#### Error: "Communications link failure"

**Cause:** MySQL server is not running or is not accessible.

**Solution:**
1. Make sure your MySQL server is running
2. Verify the server port (default is 3306) and update if necessary in:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/attendance_db?createDatabaseIfNotExist=true";
   ```

### UI Display Issues

#### Issue: "Buttons are hidden behind panels in non-maximized window"

**Cause:** Layout issues with JPanel components and sizing.

**Solution:**
- We've implemented a JSplitPane that should fix this issue
- If you're still experiencing problems:
  1. Resize the window to be larger
  2. Adjust the divider position by dragging it
  3. Maximize the window for optimal viewing

#### Issue: "Students not showing in attendance panel"

**Cause:** Database connection issue or no student enrollments in the database.

**Solution:**
1. Click the "SHOW STUDENTS" button after selecting subject and date
2. If no students appear, click the "Debug DB" button to see diagnostic information
3. Verify that your database has correct student enrollments by checking the student_subjects table
4. Ensure that the faculty ID matches the subjects they should be teaching

### Login Issues

#### Issue: "Can't log in with default accounts"

**Cause:** Database has not been properly initialized with sample data.

**Solution:**
1. Make sure you've updated the database connection settings
2. Run the application - it should automatically initialize the database
3. Check console output for any database errors
4. Default accounts are:
   - Faculty: username=`faculty1`, password=`password`
   - Student: username=`student1`, password=`password`

### Runtime Errors

#### Error: "NullPointerException" in UserDAO or AttendanceDAO

**Cause:** Database connection failed and returned null.

**Solution:**
1. Check your database connection settings
2. Make sure MySQL is running
3. Update the password if needed
4. Restart the application

#### Error: "Table 'attendance_db.users' doesn't exist"

**Cause:** Database tables were not created successfully.

**Solution:**
1. Drop the attendance_db database manually if it exists
2. Ensure MySQL permissions allow creating databases
3. Check for errors in the database.sql script
4. Restart the application

## How to Debug

1. Use the "Debug DB" button in the Mark Attendance panel
2. Check console output for database connection messages
3. Examine database tables directly using MySQL CLI or a GUI tool
4. If all else fails, manually run the SQL script from `src/main/resources/database.sql`

## Contact Support

If you continue experiencing issues after trying these solutions, please open an issue in the project repository or contact the development team. 