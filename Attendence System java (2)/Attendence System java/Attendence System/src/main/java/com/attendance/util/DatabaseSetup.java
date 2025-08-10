package com.attendance.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Utility class to initialize the database schema and sample data
 */
public class DatabaseSetup {
    
    /**
     * Initializes the database with schema and sample data
     */
    public static void initialize() {
        try {
            System.out.println("Setting up database schema and sample data...");
            
            // Get SQL script content from resources
            InputStream is = DatabaseSetup.class.getClassLoader().getResourceAsStream("database.sql");
            if (is == null) {
                System.err.println("Could not find database.sql script in resources!");
                return;
            }
            
            String sqlScript = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));
            
            // Split script by semicolons to execute each statement
            String[] statements = sqlScript.split(";");
            
            // Get database connection
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            
            // Execute each statement
            for (String statement : statements) {
                String trimmedStatement = statement.trim();
                if (!trimmedStatement.isEmpty()) {
                    System.out.println("Executing SQL: " + trimmedStatement);
                    try {
                        stmt.execute(trimmedStatement);
                    } catch (Exception e) {
                        System.err.println("Error executing statement: " + e.getMessage());
                    }
                }
            }
            
            stmt.close();
            System.out.println("Database setup completed successfully");
            
            // Verify the setup by checking some tables
            verifyDatabaseSetup();
            
        } catch (Exception e) {
            System.err.println("Database setup error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verifies that the database was set up correctly
     */
    private static void verifyDatabaseSetup() {
        try {
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            
            // Check users table
            java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) {
                System.out.println("Users in database: " + rs.getInt(1));
            }
            rs.close();
            
            // Check student_subjects table
            rs = stmt.executeQuery("SELECT COUNT(*) FROM student_subjects");
            if (rs.next()) {
                System.out.println("Student-Subject relationships: " + rs.getInt(1));
            }
            rs.close();
            
            // List all student-subject relationships for debugging
            rs = stmt.executeQuery(
                "SELECT s.name AS student_name, su.name AS subject_name " +
                "FROM student_subjects ss " +
                "JOIN students s ON ss.student_id = s.id " +
                "JOIN subjects su ON ss.subject_id = su.id"
            );
            
            System.out.println("Student-Subject relationships:");
            while (rs.next()) {
                System.out.println(" - " + rs.getString("student_name") + 
                                   " takes " + rs.getString("subject_name"));
            }
            rs.close();
            
            stmt.close();
        } catch (Exception e) {
            System.err.println("Verification error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 