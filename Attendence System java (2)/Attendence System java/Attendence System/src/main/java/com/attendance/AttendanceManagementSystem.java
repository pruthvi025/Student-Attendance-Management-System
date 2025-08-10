package com.attendance;

import com.attendance.ui.LoginFrame;
import com.attendance.util.DBConnection;
import com.attendance.util.DatabaseSetup;

import javax.swing.*;

public class AttendanceManagementSystem {
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Initialize database connection and setup database
        System.out.println("Initializing Attendance Management System...");
        DBConnection.getConnection();
        
        // Setup database schema and sample data
        DatabaseSetup.initialize();
        
        // Start application with login screen
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("Starting application UI...");
                new LoginFrame();
            }
        });
    }
} 