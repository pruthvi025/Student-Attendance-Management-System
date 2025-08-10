package com.attendance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.attendance.model.Student;
import com.attendance.util.DBConnection;

public class StudentDAO {
    private Connection connection;
    
    public StudentDAO() {
        this.connection = DBConnection.getConnection();
    }
    
    public Student getStudentByUserId(int userId) {
        Student student = null;
        String query = "SELECT * FROM students WHERE user_id = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                student = new Student();
                student.setId(rs.getInt("id"));
                student.setName(rs.getString("name"));
                student.setRollNo(rs.getString("roll_no"));
                student.setCourse(rs.getString("course"));
                student.setUserId(rs.getInt("user_id"));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error fetching student: " + e.getMessage());
        }
        
        return student;
    }
    
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String query = "SELECT * FROM students";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getInt("id"));
                student.setName(rs.getString("name"));
                student.setRollNo(rs.getString("roll_no"));
                student.setCourse(rs.getString("course"));
                student.setUserId(rs.getInt("user_id"));
                
                students.add(student);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error fetching all students: " + e.getMessage());
        }
        
        return students;
    }
    
    public List<Student> getStudentsBySubject(int subjectId) {
        List<Student> students = new ArrayList<>();
        String query = "SELECT s.* FROM students s " +
                       "JOIN student_subjects ss ON s.id = ss.student_id " +
                       "WHERE ss.subject_id = ?";
        
        System.out.println("Executing query for subject ID: " + subjectId);
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, subjectId);
            
            System.out.println("Query: " + query.replace("?", String.valueOf(subjectId)));
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getInt("id"));
                student.setName(rs.getString("name"));
                student.setRollNo(rs.getString("roll_no"));
                student.setCourse(rs.getString("course"));
                student.setUserId(rs.getInt("user_id"));
                
                students.add(student);
                System.out.println("Found student: " + student.getName() + " (ID: " + student.getId() + ")");
            }
            
            rs.close();
            stmt.close();
            
            System.out.println("Total students found for subject " + subjectId + ": " + students.size());
            
            if (students.isEmpty()) {
                // Try to understand why no students were found
                checkStudentEnrollments(subjectId);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching students by subject: " + e.getMessage());
            e.printStackTrace();
        }
        
        return students;
    }
    
    private void checkStudentEnrollments(int subjectId) {
        try {
            // Check if the subject exists
            String subjectQuery = "SELECT * FROM subjects WHERE id = ?";
            PreparedStatement subjectStmt = connection.prepareStatement(subjectQuery);
            subjectStmt.setInt(1, subjectId);
            ResultSet subjectRs = subjectStmt.executeQuery();
            
            if (!subjectRs.next()) {
                System.out.println("Subject with ID " + subjectId + " not found!");
                subjectRs.close();
                subjectStmt.close();
                return;
            }
            
            String subjectName = subjectRs.getString("name");
            subjectRs.close();
            subjectStmt.close();
            
            // Check if there are enrollments
            String enrollmentQuery = "SELECT COUNT(*) as count FROM student_subjects WHERE subject_id = ?";
            PreparedStatement enrollmentStmt = connection.prepareStatement(enrollmentQuery);
            enrollmentStmt.setInt(1, subjectId);
            ResultSet enrollmentRs = enrollmentStmt.executeQuery();
            
            if (enrollmentRs.next()) {
                int count = enrollmentRs.getInt("count");
                System.out.println("Found " + count + " enrollments for subject " + subjectName + " (ID: " + subjectId + ")");
            }
            
            enrollmentRs.close();
            enrollmentStmt.close();
            
            // Check total students
            String studentQuery = "SELECT COUNT(*) as count FROM students";
            PreparedStatement studentStmt = connection.prepareStatement(studentQuery);
            ResultSet studentRs = studentStmt.executeQuery();
            
            if (studentRs.next()) {
                int count = studentRs.getInt("count");
                System.out.println("Total students in database: " + count);
            }
            
            studentRs.close();
            studentStmt.close();
            
        } catch (SQLException e) {
            System.err.println("Error checking enrollments: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Method to get the highest student ID (for new student creation)
    public int getMaxStudentId() {
        int maxId = 0;
        String query = "SELECT MAX(id) as max_id FROM students";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getObject("max_id") != null) {
                maxId = rs.getInt("max_id");
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting max student ID: " + e.getMessage());
        }
        
        return maxId;
    }
    
    // Method to get the highest user ID (for new user creation)
    public int getMaxUserId() {
        int maxId = 0;
        String query = "SELECT MAX(id) as max_id FROM users";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getObject("max_id") != null) {
                maxId = rs.getInt("max_id");
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting max user ID: " + e.getMessage());
        }
        
        return maxId;
    }
    
    // Method to add a new student
    public boolean addStudent(Student student, String password) {
        // First add a user entry
        String userQuery = "INSERT INTO users (id, username, password, role, name) VALUES (?, ?, ?, 'student', ?)";
        String studentQuery = "INSERT INTO students (id, name, roll_no, course, user_id) VALUES (?, ?, ?, ?, ?)";
        
        try {
            // Start transaction
            connection.setAutoCommit(false);
            
            // Ensure roll number is properly formatted as username (lowercase, no spaces)
            String username = student.getRollNo().toLowerCase().trim().replaceAll("\\s+", "");
            
            // Add user
            PreparedStatement userStmt = connection.prepareStatement(userQuery);
            userStmt.setInt(1, student.getUserId());
            userStmt.setString(2, username); // Use properly formatted roll number as username
            userStmt.setString(3, password);
            userStmt.setString(4, student.getName());
            
            int userResult = userStmt.executeUpdate();
            userStmt.close();
            
            if (userResult <= 0) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            // Add student
            PreparedStatement studentStmt = connection.prepareStatement(studentQuery);
            studentStmt.setInt(1, student.getId());
            studentStmt.setString(2, student.getName());
            studentStmt.setString(3, student.getRollNo());
            studentStmt.setString(4, student.getCourse());
            studentStmt.setInt(5, student.getUserId());
            
            int studentResult = studentStmt.executeUpdate();
            studentStmt.close();
            
            if (studentResult <= 0) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            // Commit transaction
            connection.commit();
            connection.setAutoCommit(true);
            
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding student: " + e.getMessage());
            e.printStackTrace();
            
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            
            return false;
        }
    }
    
    // Method to delete a student
    public boolean deleteStudent(int studentId) {
        // Find the user ID associated with this student first
        String findUserQuery = "SELECT user_id FROM students WHERE id = ?";
        String deleteStudentSubjectsQuery = "DELETE FROM student_subjects WHERE student_id = ?";
        String deleteAttendanceQuery = "DELETE FROM attendance WHERE student_id = ?";
        String deleteStudentQuery = "DELETE FROM students WHERE id = ?";
        String deleteUserQuery = "DELETE FROM users WHERE id = ?";
        
        try {
            // Start transaction
            connection.setAutoCommit(false);
            
            // Find user ID
            PreparedStatement findUserStmt = connection.prepareStatement(findUserQuery);
            findUserStmt.setInt(1, studentId);
            ResultSet rs = findUserStmt.executeQuery();
            
            if (!rs.next()) {
                // Student not found
                rs.close();
                findUserStmt.close();
                connection.setAutoCommit(true);
                return false;
            }
            
            int userId = rs.getInt("user_id");
            rs.close();
            findUserStmt.close();
            
            // Delete student's enrollment from subjects
            PreparedStatement deleteSubjectsStmt = connection.prepareStatement(deleteStudentSubjectsQuery);
            deleteSubjectsStmt.setInt(1, studentId);
            deleteSubjectsStmt.executeUpdate();
            deleteSubjectsStmt.close();
            
            // Delete student's attendance records
            PreparedStatement deleteAttendanceStmt = connection.prepareStatement(deleteAttendanceQuery);
            deleteAttendanceStmt.setInt(1, studentId);
            deleteAttendanceStmt.executeUpdate();
            deleteAttendanceStmt.close();
            
            // Delete student
            PreparedStatement deleteStudentStmt = connection.prepareStatement(deleteStudentQuery);
            deleteStudentStmt.setInt(1, studentId);
            int studentResult = deleteStudentStmt.executeUpdate();
            deleteStudentStmt.close();
            
            if (studentResult <= 0) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            // Delete user
            PreparedStatement deleteUserStmt = connection.prepareStatement(deleteUserQuery);
            deleteUserStmt.setInt(1, userId);
            deleteUserStmt.executeUpdate();
            deleteUserStmt.close();
            
            // Commit transaction
            connection.commit();
            connection.setAutoCommit(true);
            
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
            e.printStackTrace();
            
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            
            return false;
        }
    }
    
    // Get students who are not enrolled in a specific subject
    public List<Student> getStudentsNotInSubject(int subjectId) {
        List<Student> students = new ArrayList<>();
        String query = "SELECT * FROM students WHERE id NOT IN " +
                       "(SELECT student_id FROM student_subjects WHERE subject_id = ?)";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, subjectId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getInt("id"));
                student.setName(rs.getString("name"));
                student.setRollNo(rs.getString("roll_no"));
                student.setCourse(rs.getString("course"));
                student.setUserId(rs.getInt("user_id"));
                
                students.add(student);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error fetching students not in subject: " + e.getMessage());
            e.printStackTrace();
        }
        
        return students;
    }
    
    // Add a student to a subject
    public boolean enrollStudentInSubject(int studentId, int subjectId) {
        // Get the next available student_subjects ID
        int nextId = 1;
        try {
            PreparedStatement idStmt = connection.prepareStatement("SELECT MAX(id) as max_id FROM student_subjects");
            ResultSet idRs = idStmt.executeQuery();
            if (idRs.next() && idRs.getObject("max_id") != null) {
                nextId = idRs.getInt("max_id") + 1;
            }
            idRs.close();
            idStmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting max student_subjects ID: " + e.getMessage());
            return false;
        }
        
        // Insert enrollment record
        String query = "INSERT INTO student_subjects (id, student_id, subject_id) VALUES (?, ?, ?)";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, nextId);
            stmt.setInt(2, studentId);
            stmt.setInt(3, subjectId);
            
            int result = stmt.executeUpdate();
            stmt.close();
            
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error enrolling student in subject: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Remove a student from a subject
    public boolean removeStudentFromSubject(int studentId, int subjectId) {
        String query = "DELETE FROM student_subjects WHERE student_id = ? AND subject_id = ?";
        
        try {
            // Also remove attendance records
            PreparedStatement attendanceStmt = connection.prepareStatement(
                "DELETE FROM attendance WHERE student_id = ? AND subject_id = ?");
            attendanceStmt.setInt(1, studentId);
            attendanceStmt.setInt(2, subjectId);
            attendanceStmt.executeUpdate();
            attendanceStmt.close();
            
            // Remove from subject
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, studentId);
            stmt.setInt(2, subjectId);
            
            int result = stmt.executeUpdate();
            stmt.close();
            
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error removing student from subject: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 