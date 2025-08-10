package com.attendance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.attendance.model.Subject;
import com.attendance.util.DBConnection;

public class SubjectDAO {
    private Connection connection;
    
    public SubjectDAO() {
        this.connection = DBConnection.getConnection();
    }
    
    public List<Subject> getAllSubjects() {
        List<Subject> subjects = new ArrayList<>();
        String query = "SELECT * FROM subjects";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Subject subject = new Subject();
                subject.setId(rs.getInt("id"));
                subject.setName(rs.getString("name"));
                subject.setCode(rs.getString("code"));
                subject.setFacultyId(rs.getInt("faculty_id"));
                
                // Check if semester and department columns exist before trying to get them
                try {
                    subject.setSemester(rs.getString("semester"));
                    subject.setDepartment(rs.getString("department"));
                } catch (SQLException e) {
                    // These columns might not exist in older database schemas
                    subject.setSemester("");
                    subject.setDepartment("");
                }
                
                subjects.add(subject);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error fetching subjects: " + e.getMessage());
        }
        
        return subjects;
    }
    
    public List<Subject> getSubjectsByFacultyId(int facultyId) {
        List<Subject> subjects = new ArrayList<>();
        String query = "SELECT * FROM subjects WHERE faculty_id = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, facultyId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Subject subject = new Subject();
                subject.setId(rs.getInt("id"));
                subject.setName(rs.getString("name"));
                subject.setCode(rs.getString("code"));
                subject.setFacultyId(rs.getInt("faculty_id"));
                
                // Check if semester and department columns exist before trying to get them
                try {
                    subject.setSemester(rs.getString("semester"));
                    subject.setDepartment(rs.getString("department"));
                } catch (SQLException e) {
                    // These columns might not exist in older database schemas
                    subject.setSemester("");
                    subject.setDepartment("");
                }
                
                subjects.add(subject);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error fetching subjects by faculty: " + e.getMessage());
        }
        
        return subjects;
    }
    
    public Subject getSubjectById(int id) {
        Subject subject = null;
        String query = "SELECT * FROM subjects WHERE id = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                subject = new Subject();
                subject.setId(rs.getInt("id"));
                subject.setName(rs.getString("name"));
                subject.setCode(rs.getString("code"));
                subject.setFacultyId(rs.getInt("faculty_id"));
                
                // Check if semester and department columns exist before trying to get them
                try {
                    subject.setSemester(rs.getString("semester"));
                    subject.setDepartment(rs.getString("department"));
                } catch (SQLException e) {
                    // These columns might not exist in older database schemas
                    subject.setSemester("");
                    subject.setDepartment("");
                }
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error fetching subject by id: " + e.getMessage());
        }
        
        return subject;
    }
    
    // Method to get the maximum subject ID
    public int getMaxSubjectId() {
        int maxId = 0;
        String query = "SELECT MAX(id) as max_id FROM subjects";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getObject("max_id") != null) {
                maxId = rs.getInt("max_id");
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting max subject ID: " + e.getMessage());
        }
        
        return maxId;
    }
    
    // Method to add a new subject
    public boolean addSubject(Subject subject) {
        // First, check if the semester and department columns exist in the database
        ensureSubjectColumnsExist();
        
        String query = "INSERT INTO subjects (id, name, code, faculty_id, semester, department) VALUES (?, ?, ?, ?, ?, ?)";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, subject.getId());
            stmt.setString(2, subject.getName());
            stmt.setString(3, subject.getCode());
            stmt.setInt(4, subject.getFacultyId());
            stmt.setString(5, subject.getSemester() != null ? subject.getSemester() : "");
            stmt.setString(6, subject.getDepartment() != null ? subject.getDepartment() : "");
            
            int result = stmt.executeUpdate();
            stmt.close();
            
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error adding subject: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Method to update an existing subject
    public boolean updateSubject(Subject subject) {
        // First, check if the semester and department columns exist in the database
        ensureSubjectColumnsExist();
        
        String query = "UPDATE subjects SET name = ?, code = ?, faculty_id = ?, semester = ?, department = ? WHERE id = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, subject.getName());
            stmt.setString(2, subject.getCode());
            stmt.setInt(3, subject.getFacultyId());
            stmt.setString(4, subject.getSemester() != null ? subject.getSemester() : "");
            stmt.setString(5, subject.getDepartment() != null ? subject.getDepartment() : "");
            stmt.setInt(6, subject.getId());
            
            int result = stmt.executeUpdate();
            stmt.close();
            
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error updating subject: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Method to delete a subject
    public boolean deleteSubject(int subjectId) {
        // First check if there are any students enrolled in this subject or attendance records
        String deleteStudentSubjectsQuery = "DELETE FROM student_subjects WHERE subject_id = ?";
        String deleteAttendanceQuery = "DELETE FROM attendance WHERE subject_id = ?";
        String deleteSubjectQuery = "DELETE FROM subjects WHERE id = ?";
        
        try {
            // Start transaction
            connection.setAutoCommit(false);
            
            // Remove student enrollments
            PreparedStatement deleteStudentSubjectsStmt = connection.prepareStatement(deleteStudentSubjectsQuery);
            deleteStudentSubjectsStmt.setInt(1, subjectId);
            deleteStudentSubjectsStmt.executeUpdate();
            deleteStudentSubjectsStmt.close();
            
            // Remove attendance records
            PreparedStatement deleteAttendanceStmt = connection.prepareStatement(deleteAttendanceQuery);
            deleteAttendanceStmt.setInt(1, subjectId);
            deleteAttendanceStmt.executeUpdate();
            deleteAttendanceStmt.close();
            
            // Delete the subject
            PreparedStatement deleteSubjectStmt = connection.prepareStatement(deleteSubjectQuery);
            deleteSubjectStmt.setInt(1, subjectId);
            int result = deleteSubjectStmt.executeUpdate();
            deleteSubjectStmt.close();
            
            // Commit transaction
            connection.commit();
            connection.setAutoCommit(true);
            
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting subject: " + e.getMessage());
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
    
    // Method to get subjects for a specific student
    public List<Subject> getSubjectsByStudent(int studentId) {
        List<Subject> subjects = new ArrayList<>();
        String query = "SELECT s.* FROM subjects s " +
                       "JOIN student_subjects ss ON s.id = ss.subject_id " +
                       "WHERE ss.student_id = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Subject subject = new Subject();
                subject.setId(rs.getInt("id"));
                subject.setName(rs.getString("name"));
                subject.setCode(rs.getString("code"));
                subject.setFacultyId(rs.getInt("faculty_id"));
                
                // Check if semester and department columns exist before trying to get them
                try {
                    subject.setSemester(rs.getString("semester"));
                    subject.setDepartment(rs.getString("department"));
                } catch (SQLException e) {
                    // These columns might not exist in older database schemas
                    subject.setSemester("");
                    subject.setDepartment("");
                }
                
                subjects.add(subject);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error fetching subjects by student: " + e.getMessage());
        }
        
        return subjects;
    }
    
    // Helper method to ensure the semester and department columns exist in the subjects table
    private void ensureSubjectColumnsExist() {
        try {
            // Check for semester column
            boolean semesterExists = columnExists("subjects", "semester");
            if (!semesterExists) {
                // Add semester column
                PreparedStatement stmt = connection.prepareStatement(
                    "ALTER TABLE subjects ADD COLUMN semester VARCHAR(50) DEFAULT ''");
                stmt.executeUpdate();
                stmt.close();
                System.out.println("Added semester column to subjects table");
            }
            
            // Check for department column
            boolean departmentExists = columnExists("subjects", "department");
            if (!departmentExists) {
                // Add department column
                PreparedStatement stmt = connection.prepareStatement(
                    "ALTER TABLE subjects ADD COLUMN department VARCHAR(100) DEFAULT ''");
                stmt.executeUpdate();
                stmt.close();
                System.out.println("Added department column to subjects table");
            }
        } catch (SQLException e) {
            System.err.println("Error ensuring subject columns exist: " + e.getMessage());
        }
    }
    
    // Helper method to check if a column exists in a table
    private boolean columnExists(String tableName, String columnName) {
        try {
            // This query checks in the database's schema information if the column exists
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?");
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next();
            rs.close();
            stmt.close();
            return exists;
        } catch (SQLException e) {
            // If this query fails, try a simpler approach
            try {
                // Try to select the column from the table
                PreparedStatement stmt = connection.prepareStatement(
                    "SELECT " + columnName + " FROM " + tableName + " LIMIT 1");
                stmt.executeQuery();  // If this doesn't throw an exception, the column exists
                stmt.close();
                return true;
            } catch (SQLException ex) {
                // If this throws an exception, the column doesn't exist
                return false;
            }
        }
    }
} 