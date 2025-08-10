package com.attendance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.attendance.model.Attendance;
import com.attendance.util.DBConnection;

public class AttendanceDAO {
    private Connection connection;
    
    public AttendanceDAO() {
        // Get a fresh connection each time
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                // Connection exists and is valid, no need to create a new one
            } else {
                this.connection = DBConnection.getConnection();
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection state: " + e.getMessage());
            // Force a new connection
            DBConnection.closeConnection();
            this.connection = DBConnection.getConnection();
        }
    }
    
    public boolean markAttendance(int studentId, int subjectId, Date date, boolean present) {
        // First check if attendance record already exists
        String checkQuery = "SELECT id FROM attendance WHERE student_id = ? AND subject_id = ? AND date = ?";
        String insertQuery = "INSERT INTO attendance (student_id, subject_id, date, present) VALUES (?, ?, ?, ?)";
        String updateQuery = "UPDATE attendance SET present = ? WHERE student_id = ? AND subject_id = ? AND date = ?";
        
        try {
            // Ensure connection is still valid
            if (connection == null || connection.isClosed()) {
                connection = DBConnection.getConnection();
                if (connection == null) {
                    System.err.println("Failed to establish database connection");
                    return false;
                }
            }
            
            // Check if record exists
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, studentId);
            checkStmt.setInt(2, subjectId);
            checkStmt.setDate(3, new java.sql.Date(date.getTime()));
            
            ResultSet rs = checkStmt.executeQuery();
            boolean exists = rs.next();
            int existingId = exists ? rs.getInt("id") : -1;
            rs.close();
            checkStmt.close();
            
            // Debug output
            System.out.println("Marking attendance for student ID: " + studentId + 
                              ", subject ID: " + subjectId + 
                              ", date: " + new java.sql.Date(date.getTime()) + 
                              ", present: " + present + 
                              ", record exists: " + exists +
                              (exists ? " (ID: " + existingId + ")" : ""));
            
            int rowsAffected = 0;
            
            if (exists) {
                // Update existing record
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setBoolean(1, present);
                updateStmt.setInt(2, studentId);
                updateStmt.setInt(3, subjectId);
                updateStmt.setDate(4, new java.sql.Date(date.getTime()));
                
                rowsAffected = updateStmt.executeUpdate();
                updateStmt.close();
                System.out.println("Updated existing attendance record, rows affected: " + rowsAffected);
            } else {
                // Try with explicit ID generation
                try {
                    // Get the next available ID
                    Statement idStmt = connection.createStatement();
                    ResultSet idRs = idStmt.executeQuery("SELECT MAX(id) as max_id FROM attendance");
                    int nextId = 1; // Default if no records exist
                    
                    if (idRs.next() && idRs.getObject("max_id") != null) {
                        nextId = idRs.getInt("max_id") + 1;
                    }
                    idRs.close();
                    idStmt.close();
                    
                    // Insert with explicit ID
                    String explicitInsert = "INSERT INTO attendance (id, student_id, subject_id, date, present) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement insertWithIdStmt = connection.prepareStatement(explicitInsert);
                    insertWithIdStmt.setInt(1, nextId);
                    insertWithIdStmt.setInt(2, studentId);
                    insertWithIdStmt.setInt(3, subjectId);
                    insertWithIdStmt.setDate(4, new java.sql.Date(date.getTime()));
                    insertWithIdStmt.setBoolean(5, present);
                    
                    rowsAffected = insertWithIdStmt.executeUpdate();
                    insertWithIdStmt.close();
                    System.out.println("Inserted new attendance record with ID " + nextId + ", rows affected: " + rowsAffected);
                    
                } catch (SQLException explicitIdEx) {
                    System.err.println("Error with explicit ID insertion: " + explicitIdEx.getMessage());
                    
                    // Fall back to standard insert
                    PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                    insertStmt.setInt(1, studentId);
                    insertStmt.setInt(2, subjectId);
                    insertStmt.setDate(3, new java.sql.Date(date.getTime()));
                    insertStmt.setBoolean(4, present);
                    
                    rowsAffected = insertStmt.executeUpdate();
                    insertStmt.close();
                    System.out.println("Inserted new attendance record (fallback method), rows affected: " + rowsAffected);
                }
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error marking attendance: " + e.getMessage());
            e.printStackTrace();
            
            // Try to recover by resetting the connection
            try {
                DBConnection.closeConnection();
                connection = DBConnection.getConnection();
            } catch (Exception ex) {
                System.err.println("Failed to reset connection: " + ex.getMessage());
            }
            
            return false;
        }
    }
    
    public List<Attendance> getAttendanceByStudentAndSubject(int studentId, int subjectId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE student_id = ? AND subject_id = ?";
        
        try {
            // Check if connection is valid and refresh if needed
            if (connection == null || connection.isClosed()) {
                connection = DBConnection.getConnection();
                if (connection == null) {
                    System.err.println("Failed to establish database connection in getAttendanceByStudentAndSubject");
                    return attendanceList;
                }
            }
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, studentId);
            stmt.setInt(2, subjectId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Attendance attendance = new Attendance();
                attendance.setId(rs.getInt("id"));
                attendance.setStudentId(rs.getInt("student_id"));
                attendance.setSubjectId(rs.getInt("subject_id"));
                attendance.setDate(rs.getDate("date"));
                attendance.setPresent(rs.getBoolean("present"));
                
                attendanceList.add(attendance);
            }
            
            rs.close();
            stmt.close();
            System.out.println("Found " + attendanceList.size() + " attendance records for student ID: " + studentId + ", subject ID: " + subjectId);
        } catch (SQLException e) {
            System.err.println("Error fetching attendance: " + e.getMessage());
            // Try to recover by resetting the connection
            try {
                DBConnection.closeConnection();
                connection = DBConnection.getConnection();
            } catch (Exception ex) {
                System.err.println("Failed to reset connection: " + ex.getMessage());
            }
        }
        
        return attendanceList;
    }
    
    public List<Attendance> getAttendanceBySubjectAndDate(int subjectId, Date date) {
        List<Attendance> attendanceList = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE subject_id = ? AND date = ?";
        
        try {
            // Check if connection is valid and refresh if needed
            if (connection == null || connection.isClosed()) {
                connection = DBConnection.getConnection();
                if (connection == null) {
                    System.err.println("Failed to establish database connection in getAttendanceBySubjectAndDate");
                    return attendanceList;
                }
            }
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, subjectId);
            stmt.setDate(2, new java.sql.Date(date.getTime()));
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Attendance attendance = new Attendance();
                attendance.setId(rs.getInt("id"));
                attendance.setStudentId(rs.getInt("student_id"));
                attendance.setSubjectId(rs.getInt("subject_id"));
                attendance.setDate(rs.getDate("date"));
                attendance.setPresent(rs.getBoolean("present"));
                
                attendanceList.add(attendance);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error fetching attendance by date: " + e.getMessage());
            // Try to recover by resetting the connection
            try {
                DBConnection.closeConnection();
                connection = DBConnection.getConnection();
            } catch (Exception ex) {
                System.err.println("Failed to reset connection: " + ex.getMessage());
            }
        }
        
        return attendanceList;
    }
    
    public double getAttendancePercentage(int studentId, int subjectId) {
        String query = "SELECT COUNT(*) as total, SUM(CASE WHEN present = true THEN 1 ELSE 0 END) as present " +
                      "FROM attendance WHERE student_id = ? AND subject_id = ?";
        
        try {
            // Check if connection is valid and refresh if needed
            if (connection == null || connection.isClosed()) {
                connection = DBConnection.getConnection();
                if (connection == null) {
                    System.err.println("Failed to establish database connection in getAttendancePercentage");
                    return 0;
                }
            }
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, studentId);
            stmt.setInt(2, subjectId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int total = rs.getInt("total");
                int present = rs.getInt("present");
                
                System.out.println("Attendance percentage calculation - Student ID: " + studentId + 
                                  ", Subject ID: " + subjectId + 
                                  ", Total classes: " + total + 
                                  ", Present: " + present);
                
                if (total > 0) {
                    return ((double) present / total) * 100;
                }
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error calculating attendance percentage: " + e.getMessage());
            // Try to recover by resetting the connection
            try {
                DBConnection.closeConnection();
                connection = DBConnection.getConnection();
            } catch (Exception ex) {
                System.err.println("Failed to reset connection: " + ex.getMessage());
            }
        }
        
        return 0;
    }
} 