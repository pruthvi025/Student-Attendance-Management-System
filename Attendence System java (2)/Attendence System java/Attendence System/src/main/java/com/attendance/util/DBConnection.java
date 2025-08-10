package com.attendance.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/attendance_db?createDatabaseIfNotExist=true";
    private static final String USER = "root";
    private static final String PASSWORD = "Pruthvi2311@"; // Replace with your MySQL password if needed
    
    private static Connection connection = null;
    private static long lastConnectionTime = 0;
    private static final long CONNECTION_TIMEOUT = 3600000; // 1 hour in milliseconds
    private static final long CONNECTION_VALIDATION_INTERVAL = 300000; // 5 minutes in milliseconds
    private static long lastValidationTime = 0;
    
    public static synchronized Connection getConnection() {
        try {
            long currentTime = System.currentTimeMillis();
            
            // Check if connection exists and is valid
            if (connection != null && !connection.isClosed()) {
                // Periodically validate connection to ensure it's still working
                boolean shouldValidate = (currentTime - lastValidationTime) > CONNECTION_VALIDATION_INTERVAL;
                boolean connectionTooOld = (currentTime - lastConnectionTime) > CONNECTION_TIMEOUT;
                
                if (connectionTooOld) {
                    System.out.println("Connection is too old, creating a new one");
                    closeConnection();
                    return createFreshConnection();
                } else if (shouldValidate) {
                    try {
                        // Verify connection is valid with a simple query
                        Statement stmt = connection.createStatement();
                        stmt.execute("SELECT 1");
                        stmt.close();
                        
                        // Update validation time
                        lastValidationTime = currentTime;
                        
                        System.out.println("Connection validated successfully");
                        return connection;
                    } catch (SQLException e) {
                        System.err.println("Connection validation failed: " + e.getMessage());
                        closeConnection();
                        return createFreshConnection();
                    }
                } else {
                    // Connection seems valid and was recently validated
                    return connection;
                }
            } else {
                // No connection or closed connection
                return createFreshConnection();
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection state: " + e.getMessage());
            closeConnection();
            return createFreshConnection();
        }
    }
    
    private static synchronized Connection createFreshConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Creating a new database connection...");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            lastConnectionTime = System.currentTimeMillis();
            lastValidationTime = lastConnectionTime;
            System.out.println("Database connection established successfully");
            
            // Make sure tables exist
            validateDatabaseStructure();
            
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    private static void validateDatabaseStructure() {
        try {
            Statement stmt = connection.createStatement();
            // Check if attendance table exists
            try {
                stmt.executeQuery("SELECT COUNT(*) FROM attendance");
                System.out.println("Attendance table exists");
            } catch (SQLException e) {
                System.err.println("Attendance table check failed: " + e.getMessage());
            }
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Database validation error: " + e.getMessage());
        }
    }
    
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    // Add method to force refresh the connection
    public static synchronized void refreshConnection() {
        closeConnection();
        getConnection();
    }
} 