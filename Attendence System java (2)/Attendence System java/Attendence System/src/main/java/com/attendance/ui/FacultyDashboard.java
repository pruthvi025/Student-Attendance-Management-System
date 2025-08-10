package com.attendance.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.attendance.dao.FacultyDAO;
import com.attendance.dao.SubjectDAO;
import com.attendance.model.Faculty;
import com.attendance.model.Subject;
import com.attendance.model.User;
import com.attendance.ui.panels.AttendanceReportPanel;
import com.attendance.ui.panels.ManageStudentsPanel;
import com.attendance.ui.panels.MarkAttendancePanel;

public class FacultyDashboard extends JFrame {
    private User user;
    private Faculty faculty;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    // Card names
    private static final String WELCOME_PANEL = "Welcome";
    private static final String MARK_ATTENDANCE_PANEL = "MarkAttendance";
    private static final String VIEW_REPORTS_PANEL = "ViewReports";
    private static final String MANAGE_STUDENTS_PANEL = "ManageStudents";
    
    // Menu width
    private static final int MENU_WIDTH = 220;
    
    public FacultyDashboard(User user) {
        this.user = user;
        loadFacultyData();
        setupUI();
    }
    
    private void loadFacultyData() {
        FacultyDAO facultyDAO = new FacultyDAO();
        faculty = facultyDAO.getFacultyByUserId(user.getId());
        
        if (faculty == null) {
            JOptionPane.showMessageDialog(this, "Error loading faculty data", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    private void setupUI() {
        setTitle("Faculty Dashboard - " + faculty.getName());
        setSize(900, 600);
        setMinimumSize(new Dimension(750, 500)); // Set minimum frame size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create menu panel
        JPanel menuPanel = createMenuPanel();
        // Set minimum and preferred size to ensure it's always visible
        menuPanel.setMinimumSize(new Dimension(MENU_WIDTH, 300));
        menuPanel.setPreferredSize(new Dimension(MENU_WIDTH, 600));
        
        // Create content panel with card layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setMinimumSize(new Dimension(400, 300));
        
        // Add cards to main panel
        mainPanel.add(createWelcomePanel(), WELCOME_PANEL);
        mainPanel.add(new MarkAttendancePanel(faculty.getId()), MARK_ATTENDANCE_PANEL);
        mainPanel.add(new AttendanceReportPanel(faculty.getId()), VIEW_REPORTS_PANEL);
        mainPanel.add(new ManageStudentsPanel(faculty.getId()), MANAGE_STUDENTS_PANEL);
        
        // Create a JSplitPane to control layout between menu and content
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            menuPanel,
            mainPanel
        );
        splitPane.setDividerLocation(MENU_WIDTH);
        splitPane.setDividerSize(5);
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.0); // Fix left component size
        
        // Add split pane to frame
        add(splitPane, BorderLayout.CENTER);
        
        // Show welcome panel by default
        cardLayout.show(mainPanel, WELCOME_PANEL);
        
        setVisible(true);
    }
    
    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        panel.setBackground(new Color(50, 50, 50));
        
        // User info section
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setOpaque(false);
        userInfoPanel.setMaximumSize(new Dimension(MENU_WIDTH, 100));
        userInfoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel nameLabel = new JLabel("Welcome, " + faculty.getName());
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel deptLabel = new JLabel("Department: " + faculty.getDepartment());
        deptLabel.setForeground(Color.WHITE);
        deptLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        userInfoPanel.add(nameLabel);
        userInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        userInfoPanel.add(deptLabel);
        
        // Menu buttons
        JButton homeBtn = createMenuButton("Home");
        JButton markAttendanceBtn = createMenuButton("Mark Attendance");
        JButton viewReportsBtn = createMenuButton("View Reports");
        JButton manageStudentsBtn = createMenuButton("Manage Students");
        JButton logoutBtn = createMenuButton("Logout");
        
        // Add action listeners
        homeBtn.addActionListener(e -> cardLayout.show(mainPanel, WELCOME_PANEL));
        markAttendanceBtn.addActionListener(e -> cardLayout.show(mainPanel, MARK_ATTENDANCE_PANEL));
        viewReportsBtn.addActionListener(e -> cardLayout.show(mainPanel, VIEW_REPORTS_PANEL));
        manageStudentsBtn.addActionListener(e -> cardLayout.show(mainPanel, MANAGE_STUDENTS_PANEL));
        logoutBtn.addActionListener(e -> logout());
        
        // Add components to panel with spacing
        panel.add(userInfoPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(homeBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(markAttendanceBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(viewReportsBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(manageStudentsBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(logoutBtn);
        panel.add(Box.createVerticalGlue()); // Push everything to the top
        
        return panel;
    }
    
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(MENU_WIDTH - 20, 40));
        button.setPreferredSize(new Dimension(MENU_WIDTH - 20, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(new Color(70, 70, 70));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }
    
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Faculty Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Subjects taught by this faculty
        JPanel subjectsPanel = new JPanel();
        subjectsPanel.setLayout(new BoxLayout(subjectsPanel, BoxLayout.Y_AXIS));
        subjectsPanel.setBorder(BorderFactory.createTitledBorder("Subjects You Teach"));
        
        SubjectDAO subjectDAO = new SubjectDAO();
        List<Subject> subjects = subjectDAO.getSubjectsByFacultyId(faculty.getId());
        
        if (subjects.isEmpty()) {
            subjectsPanel.add(new JLabel("No subjects assigned"));
        } else {
            for (Subject subject : subjects) {
                subjectsPanel.add(new JLabel(subject.toString()));
            }
        }
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(subjectsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", "Confirm Logout", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new LoginFrame();
        }
    }
} 