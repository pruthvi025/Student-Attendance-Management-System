package com.attendance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.attendance.model.User;
import com.attendance.util.DBConnection;

public class UserDAO {
    private Connection connection;
    
    public UserDAO() {
        this.connection = DBConnection.getConnection();
    }
    
    public User validateUser(String username, String password) {
        User user = null;
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setName(rs.getString("name"));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error validating user: " + e.getMessage());
        }
        
        return user;
    }
    
    public boolean changePassword(int userId, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE id = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error changing password: " + e.getMessage());
            return false;
        }
    }
    
    // Get user by ID
    public User getUserById(int id) {
        User user = null;
        String query = "SELECT * FROM users WHERE id = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, id);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setName(rs.getString("name"));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
        }
        
        return user;
    }
    
    // Add a new user
    public int addUser(User user) {
        String query = "INSERT INTO users (id, username, password, role, name) VALUES (?, ?, ?, ?, ?)";
        
        try {
            // Check if username already exists
            if (isUsernameExists(user.getUsername())) {
                return -1; // Username already taken
            }
            
            // Get the next available ID
            int nextId = getNextId();
            user.setId(nextId);
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.getName());
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            if (rowsAffected > 0) {
                return user.getId(); // Return the new user ID
            } else {
                return -1;
            }
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            return -1;
        }
    }
    
    // Update an existing user
    public boolean updateUser(User user) {
        String query = "UPDATE users SET username = ?, password = ?, role = ?, name = ? WHERE id = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getName());
            stmt.setInt(5, user.getId());
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
    
    // Delete a user
    public boolean deleteUser(int id) {
        String query = "DELETE FROM users WHERE id = ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
    
    // Check if a username already exists
    private boolean isUsernameExists(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, username);
        
        ResultSet rs = stmt.executeQuery();
        boolean exists = rs.next() && rs.getInt(1) > 0;
        
        rs.close();
        stmt.close();
        
        return exists;
    }
    
    // Get the next available ID for a new user
    private int getNextId() throws SQLException {
        String query = "SELECT MAX(id) FROM users";
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