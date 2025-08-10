package com.attendance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.attendance.model.Faculty;
import com.attendance.util.DBConnection;

public class FacultyDAO {
    private Connection connection;
    
    public FacultyDAO() {
        this.connection = DBConnection.getConnection();
    }
    
    public Faculty getFacultyByUserId(int userId) {
        Faculty faculty = null;
        String query = "SELECT * FROM faculty WHERE user_id = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                faculty = new Faculty();
                faculty.setId(rs.getInt("id"));
                faculty.setName(rs.getString("name"));
                faculty.setDepartment(rs.getString("department"));
                faculty.setUserId(rs.getInt("user_id"));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error fetching faculty: " + e.getMessage());
        }
        
        return faculty;
    }
    
    public List<Faculty> getAllFaculty() {
        List<Faculty> facultyList = new ArrayList<>();
        String query = "SELECT * FROM faculty";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Faculty faculty = new Faculty();
                faculty.setId(rs.getInt("id"));
                faculty.setName(rs.getString("name"));
                faculty.setDepartment(rs.getString("department"));
                faculty.setUserId(rs.getInt("user_id"));
                
                facultyList.add(faculty);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error fetching all faculty: " + e.getMessage());
        }
        
        return facultyList;
    }
    
    public Faculty getFacultyById(int id) {
        Faculty faculty = null;
        String query = "SELECT * FROM faculty WHERE id = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, id);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                faculty = new Faculty();
                faculty.setId(rs.getInt("id"));
                faculty.setName(rs.getString("name"));
                faculty.setDepartment(rs.getString("department"));
                faculty.setUserId(rs.getInt("user_id"));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error fetching faculty by id: " + e.getMessage());
        }
        
        return faculty;
    }
    
    // Add new faculty
    public boolean addFaculty(Faculty faculty) {
        String query = "INSERT INTO faculty (id, name, department, user_id) VALUES (?, ?, ?, ?)";
        
        try {
            // Get the next available ID
            int nextId = getNextId();
            faculty.setId(nextId);
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, faculty.getId());
            stmt.setString(2, faculty.getName());
            stmt.setString(3, faculty.getDepartment());
            stmt.setInt(4, faculty.getUserId());
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding faculty: " + e.getMessage());
            return false;
        }
    }
    
    // Update existing faculty
    public boolean updateFaculty(Faculty faculty) {
        String query = "UPDATE faculty SET name = ?, department = ? WHERE id = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, faculty.getName());
            stmt.setString(2, faculty.getDepartment());
            stmt.setInt(3, faculty.getId());
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating faculty: " + e.getMessage());
            return false;
        }
    }
    
    // Delete faculty
    public boolean deleteFaculty(int id) {
        String query = "DELETE FROM faculty WHERE id = ?";
        
        try {
            // First, check for any subjects taught by this faculty
            String checkSubjectsQuery = "SELECT COUNT(*) FROM subjects WHERE faculty_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkSubjectsQuery);
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                // Update or delete subjects before deleting faculty
                String updateSubjectsQuery = "DELETE FROM subjects WHERE faculty_id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateSubjectsQuery);
                updateStmt.setInt(1, id);
                updateStmt.executeUpdate();
                updateStmt.close();
            }
            
            rs.close();
            checkStmt.close();
            
            // Now delete the faculty
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting faculty: " + e.getMessage());
            return false;
        }
    }
    
    // Get the next available ID for new faculty
    private int getNextId() throws SQLException {
        String query = "SELECT MAX(id) FROM faculty";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        int nextId = 1; // Default starting ID
        if (rs.next() && rs.getInt(1) > 0) {
            nextId = rs.getInt(1) + 1;
        }
        
        rs.close();
        stmt.close();
        
        return nextId;
    }
} 