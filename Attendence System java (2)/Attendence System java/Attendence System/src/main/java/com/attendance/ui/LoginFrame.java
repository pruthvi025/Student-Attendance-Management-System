package com.attendance.ui;

import com.attendance.dao.UserDAO;
import com.attendance.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;
    
    public LoginFrame() {
        // Set up the frame
        setTitle("Attendance Management System - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create components
        JLabel titleLabel = new JLabel("Attendance Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        
        JLabel infoLabel = new JLabel("Students: Use your roll number as username");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(new Color(100, 100, 100));
        
        loginButton = new JButton("Login");
        messageLabel = new JLabel("");
        messageLabel.setForeground(Color.RED);
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Add action listener to login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authenticateUser();
            }
        });
        
        // Set up layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel formPanel = new JPanel(new GridLayout(6, 1, 10, 5));
        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(infoLabel);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(loginButton);
        
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(messageLabel, BorderLayout.SOUTH);
        
        // Add panel to frame
        add(mainPanel);
        setVisible(true);
    }
    
    private void authenticateUser() {
        String username = usernameField.getText().trim().toLowerCase(); // Convert to lowercase to ensure case-insensitive matching
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password");
            return;
        }
        
        UserDAO userDAO = new UserDAO();
        User user = userDAO.validateUser(username, password);
        
        if (user != null) {
            // Authentication successful
            this.dispose(); // Close login frame
            
            if (user.getRole().equalsIgnoreCase("faculty")) {
                // Open faculty dashboard
                new FacultyDashboard(user);
            } else if (user.getRole().equalsIgnoreCase("student")) {
                // Open student dashboard
                new StudentDashboard(user);
            } else if (user.getRole().equalsIgnoreCase("admin")) {
                // Open admin dashboard
                new AdminDashboard(user);
            }
        } else {
            // Authentication failed
            messageLabel.setText("Invalid username or password");
        }
    }
    
    public static void main(String[] args) {
        try {
            // Set look and feel to system default
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame();
            }
        });
    }
} 