package com.attendance.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.dao.SubjectDAO;
import com.attendance.dao.UserDAO;
import com.attendance.model.Attendance;
import com.attendance.model.Student;
import com.attendance.model.Subject;
import com.attendance.model.User;

public class StudentDashboard extends JFrame {
    private User user;
    private Student student;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    // Card names
    private static final String WELCOME_PANEL = "Welcome";
    private static final String ATTENDANCE_PANEL = "Attendance";
    private static final String DETAILED_ATTENDANCE_PANEL = "DetailedAttendance";
    private static final String CHANGE_PASSWORD_PANEL = "ChangePassword";
    
    // Menu width
    private static final int MENU_WIDTH = 220;
    
    // Reference to the selected subject for detailed view
    private Subject selectedSubject;
    
    public StudentDashboard(User user) {
        this.user = user;
        loadStudentData();
        setupUI();
    }
    
    private void loadStudentData() {
        StudentDAO studentDAO = new StudentDAO();
        student = studentDAO.getStudentByUserId(user.getId());
        
        if (student == null) {
            JOptionPane.showMessageDialog(this, "Error loading student data", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    private void setupUI() {
        setTitle("Student Dashboard - " + student.getName());
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
        mainPanel.add(createAttendancePanel(), ATTENDANCE_PANEL);
        mainPanel.add(createDetailedAttendancePanel(), DETAILED_ATTENDANCE_PANEL);
        mainPanel.add(createChangePasswordPanel(), CHANGE_PASSWORD_PANEL);
        
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
        userInfoPanel.setMaximumSize(new Dimension(MENU_WIDTH, 120));
        userInfoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel nameLabel = new JLabel("Welcome, " + student.getName());
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel courseLabel = new JLabel("Course: " + student.getCourse());
        courseLabel.setForeground(Color.WHITE);
        courseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel rollNoLabel = new JLabel("Roll No: " + student.getRollNo());
        rollNoLabel.setForeground(Color.WHITE);
        rollNoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        userInfoPanel.add(nameLabel);
        userInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        userInfoPanel.add(courseLabel);
        userInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        userInfoPanel.add(rollNoLabel);
        
        // Menu buttons
        JButton homeBtn = createMenuButton("Home");
        JButton attendanceBtn = createMenuButton("My Attendance");
        JButton detailedAttendanceBtn = createMenuButton("Detailed Attendance");
        JButton changePasswordBtn = createMenuButton("Change Password");
        JButton logoutBtn = createMenuButton("Logout");
        
        // Add action listeners
        homeBtn.addActionListener(e -> cardLayout.show(mainPanel, WELCOME_PANEL));
        attendanceBtn.addActionListener(e -> cardLayout.show(mainPanel, ATTENDANCE_PANEL));
        detailedAttendanceBtn.addActionListener(e -> {
            refreshDetailedAttendancePanel();
            cardLayout.show(mainPanel, DETAILED_ATTENDANCE_PANEL);
        });
        changePasswordBtn.addActionListener(e -> cardLayout.show(mainPanel, CHANGE_PASSWORD_PANEL));
        logoutBtn.addActionListener(e -> logout());
        
        // Add components to panel with spacing
        panel.add(userInfoPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(homeBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(attendanceBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(detailedAttendanceBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(changePasswordBtn);
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
        
        JLabel titleLabel = new JLabel("Student Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JLabel welcomeLabel = new JLabel("Welcome to the Attendance Management System!");
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(welcomeLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("My Attendance");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Fetch subjects for this student
        SubjectDAO subjectDAO = new SubjectDAO();
        List<Subject> subjects = subjectDAO.getSubjectsByStudent(student.getId());
        
        // Create attendance table
        String[] columns = {"Subject", "Total Classes", "Present", "Absent", "Percentage", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable attendanceTable = new JTable(model);
        
        // Set properties for better UI
        attendanceTable.setRowHeight(25);
        attendanceTable.setShowGrid(true);
        attendanceTable.setGridColor(Color.LIGHT_GRAY);
        attendanceTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Set column widths
        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Subject
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Total Classes
        attendanceTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Present
        attendanceTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Absent
        attendanceTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Percentage
        attendanceTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Status
        
        // Add data to the table
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        
        for (Subject subject : subjects) {
            List<Attendance> attendanceList = attendanceDAO.getAttendanceByStudentAndSubject(student.getId(), subject.getId());
            
            int totalClasses = attendanceList.size();
            int presentCount = 0;
            
            for (Attendance attendance : attendanceList) {
                if (attendance.isPresent()) {
                    presentCount++;
                }
            }
            
            int absentCount = totalClasses - presentCount;
            double percentage = (totalClasses > 0) ? ((double) presentCount / totalClasses) * 100 : 0.0;
            String status = percentage >= 75 ? "Good Standing" : "Low Attendance";
            
            Object[] row = {
                subject.getName(),
                String.valueOf(totalClasses),
                String.valueOf(presentCount),
                String.valueOf(absentCount),
                String.format("%.2f%%", percentage),
                status
            };
            model.addRow(row);
        }
        
        // Custom renderer for the status column
        attendanceTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                String status = (String) value;
                if (status.equals("Good Standing")) {
                    c.setForeground(new Color(0, 128, 0)); // Dark green
                } else if (status.equals("Low Attendance")) {
                    c.setForeground(Color.RED);
                } else {
                    c.setForeground(Color.GRAY);
                }
                
                return c;
            }
        });
        
        // Custom renderer for the percentage column
        attendanceTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                String percentageStr = (String) value;
                double percentage = Double.parseDouble(percentageStr.replace("%", ""));
                
                if (percentage < 75) {
                    c.setForeground(Color.RED);
                } else {
                    c.setForeground(new Color(0, 128, 0)); // Dark green
                }
                
                return c;
            }
        });
        
        // Add double-click handler to view detailed attendance
        attendanceTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = attendanceTable.getSelectedRow();
                    if (row >= 0 && row < subjects.size()) {
                        selectedSubject = subjects.get(row);
                        refreshDetailedAttendancePanel();
                        cardLayout.show(mainPanel, DETAILED_ATTENDANCE_PANEL);
                    }
                }
            }
        });
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setPreferredSize(new Dimension(600, 350));
        
        // Add note for double-click
        JLabel noteLabel = new JLabel("Double-click on a subject to view detailed attendance by date");
        noteLabel.setHorizontalAlignment(JLabel.CENTER);
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        noteLabel.setForeground(Color.GRAY);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(noteLabel, BorderLayout.SOUTH);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createDetailedAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title section
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Detailed Attendance");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Create controls panel with subject selection and time range options
        JPanel controlsPanel = new JPanel(new BorderLayout(10, 0));
        
        // Subject selection
        JPanel subjectSelectionPanel = new JPanel(new BorderLayout(5, 0));
        JLabel subjectSelectLabel = new JLabel("Select Subject: ");
        
        // Fetch subjects for student
        SubjectDAO subjectDAO = new SubjectDAO();
        List<Subject> subjects = subjectDAO.getSubjectsByStudent(student.getId());
        
        JComboBox<Subject> subjectComboBox = new JComboBox<>();
        for (Subject subject : subjects) {
            subjectComboBox.addItem(subject);
        }
        
        // Custom renderer for subject dropdown
        subjectComboBox.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Subject) {
                    setText(((Subject) value).getName());
                }
                return this;
            }
        });
        
        // Set initial selection if coming from the attendance panel
        if (selectedSubject != null) {
            for (int i = 0; i < subjectComboBox.getItemCount(); i++) {
                if (subjectComboBox.getItemAt(i).getId() == selectedSubject.getId()) {
                    subjectComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        subjectSelectionPanel.add(subjectSelectLabel, BorderLayout.WEST);
        subjectSelectionPanel.add(subjectComboBox, BorderLayout.CENTER);
        
        // Time range selection
        JPanel timeRangePanel = new JPanel(new BorderLayout(5, 0));
        JLabel timeRangeLabel = new JLabel("Time Range: ");
        String[] timeRanges = {"Daily", "Weekly", "Monthly", "All Time"};
        JComboBox<String> timeRangeComboBox = new JComboBox<>(timeRanges);
        timeRangeComboBox.setSelectedItem("All Time"); // Default to all time view
        
        timeRangePanel.add(timeRangeLabel, BorderLayout.WEST);
        timeRangePanel.add(timeRangeComboBox, BorderLayout.CENTER);
        
        // View button
        JButton viewButton = new JButton("View Attendance");
        viewButton.addActionListener(e -> {
            selectedSubject = (Subject) subjectComboBox.getSelectedItem();
            String selectedTimeRange = (String) timeRangeComboBox.getSelectedItem();
            refreshDetailedAttendancePanel(selectedTimeRange);
        });
        
        // Add components to controls panel
        JPanel selectionPanel = new JPanel(new BorderLayout(10, 0));
        selectionPanel.add(subjectSelectionPanel, BorderLayout.CENTER);
        selectionPanel.add(timeRangePanel, BorderLayout.EAST);
        
        controlsPanel.add(selectionPanel, BorderLayout.CENTER);
        controlsPanel.add(viewButton, BorderLayout.EAST);
        
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.CENTER);
        titlePanel.add(controlsPanel, BorderLayout.SOUTH);
        
        // Content panel - will be populated in refreshDetailedAttendancePanel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void refreshDetailedAttendancePanel() {
        refreshDetailedAttendancePanel("All Time");
    }
    
    private void refreshDetailedAttendancePanel(String timeRange) {
        JPanel detailedPanel = (JPanel) mainPanel.getComponent(
            mainPanel.getComponentZOrder(mainPanel.getComponent(2))); // 2 is the index of DETAILED_ATTENDANCE_PANEL
        
        // Get the content panel (the CENTER of the detailed panel)
        JPanel contentPanel = (JPanel) ((BorderLayout) detailedPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        contentPanel.removeAll();
        
        if (selectedSubject == null) {
            JLabel noSubjectLabel = new JLabel("Please select a subject to view detailed attendance");
            noSubjectLabel.setHorizontalAlignment(JLabel.CENTER);
            contentPanel.add(noSubjectLabel, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
            return;
        }
        
        // Fetch attendance records
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        List<Attendance> attendanceList = attendanceDAO.getAttendanceByStudentAndSubject(student.getId(), selectedSubject.getId());
        
        if (attendanceList.isEmpty()) {
            JLabel noDataLabel = new JLabel("No attendance records found for " + selectedSubject.getName());
            noDataLabel.setHorizontalAlignment(JLabel.CENTER);
            contentPanel.add(noDataLabel, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
            return;
        }
        
        // Filter records by date range
        Date currentDate = new Date();
        Date startDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        
        switch (timeRange) {
            case "Daily":
                // Only today's records
                startDate = new Date(currentDate.getTime());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTime();
                break;
            case "Weekly":
                // Last 7 days
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                startDate = calendar.getTime();
                break;
            case "Monthly":
                // Last 30 days
                calendar.add(Calendar.MONTH, -1);
                startDate = calendar.getTime();
                break;
            case "All Time":
            default:
                // All records, set a very old date
                calendar.set(2000, 0, 1); // January 1, 2000
                startDate = calendar.getTime();
                break;
        }
        
        // Filter attendance list by date range
        List<Attendance> filteredAttendanceList = new ArrayList<>();
        for (Attendance attendance : attendanceList) {
            Date attendanceDate = attendance.getDate();
            if (!attendanceDate.before(startDate) && !attendanceDate.after(currentDate)) {
                filteredAttendanceList.add(attendance);
            }
        }
        
        if (filteredAttendanceList.isEmpty()) {
            JLabel noDataLabel = new JLabel("No attendance records found for " + selectedSubject.getName() + " in the selected time range");
            noDataLabel.setHorizontalAlignment(JLabel.CENTER);
            contentPanel.add(noDataLabel, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
            return;
        }
        
        // Sort attendance records by date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, Boolean> attendanceByDate = new TreeMap<>();
        
        for (Attendance attendance : filteredAttendanceList) {
            String dateStr = dateFormat.format(attendance.getDate());
            attendanceByDate.put(dateStr, attendance.isPresent());
        }
        
        // Create table
        String[] columns = {"Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        // Summary values
        int totalClasses = filteredAttendanceList.size();
        int presentCount = 0;
        
        // Add data to model
        for (Map.Entry<String, Boolean> entry : attendanceByDate.entrySet()) {
            String dateStr = entry.getKey();
            boolean present = entry.getValue();
            
            if (present) {
                presentCount++;
            }
            
            Object[] row = {
                dateStr,
                present ? "Present" : "Absent"
            };
            model.addRow(row);
        }
        
        int absentCount = totalClasses - presentCount;
        double percentage = (totalClasses > 0) ? ((double) presentCount / totalClasses) * 100 : 0.0;
        
        JTable detailTable = new JTable(model);
        detailTable.setRowHeight(25);
        detailTable.setShowGrid(true);
        detailTable.setGridColor(Color.LIGHT_GRAY);
        detailTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Set custom renderer for status column
        detailTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                String status = (String) value;
                if ("Present".equals(status)) {
                    c.setForeground(new Color(0, 128, 0)); // Dark green
                } else {
                    c.setForeground(Color.RED);
                }
                
                return c;
            }
        });
        
        JScrollPane tableScrollPane = new JScrollPane(detailTable);
        
        // Create summary panel
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Attendance Summary - " + timeRange));
        
        JLabel dateRangeLabel = new JLabel("Date Range: " + formatDateRange(startDate, currentDate, timeRange));
        JLabel totalClassesLabel = new JLabel("Total Classes: " + totalClasses);
        JLabel presentLabel = new JLabel("Present: " + presentCount);
        JLabel absentLabel = new JLabel("Absent: " + absentCount);
        JLabel percentageLabel = new JLabel(String.format("Attendance Percentage: %.2f%%", percentage));
        
        // Set color for percentage
        if (percentage < 75) {
            percentageLabel.setForeground(Color.RED);
        } else {
            percentageLabel.setForeground(new Color(0, 128, 0)); // Dark green
        }
        
        summaryPanel.add(dateRangeLabel);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        summaryPanel.add(totalClassesLabel);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        summaryPanel.add(presentLabel);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        summaryPanel.add(absentLabel);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        summaryPanel.add(percentageLabel);
        
        // Create split pane
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            tableScrollPane,
            summaryPanel
        );
        splitPane.setResizeWeight(0.7); // Give table more space
        
        contentPanel.add(splitPane, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private String formatDateRange(Date startDate, Date endDate, String timeRange) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        if (timeRange.equals("Daily")) {
            return dateFormat.format(startDate);
        } else if (timeRange.equals("All Time")) {
            return "All recorded dates";
        } else {
            return dateFormat.format(startDate) + " to " + dateFormat.format(endDate);
        }
    }
    
    private JPanel createChangePasswordPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        JLabel titleLabel = new JLabel("Change Password");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Create form
        JPanel formPanel = new JPanel(new BorderLayout(0, 20));
        
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Current Password
        JPanel currentPassPanel = new JPanel(new BorderLayout(10, 0));
        JLabel currentPassLabel = new JLabel("Current Password:");
        currentPassLabel.setPreferredSize(new Dimension(150, 25));
        JPasswordField currentPassField = new JPasswordField();
        currentPassPanel.add(currentPassLabel, BorderLayout.WEST);
        currentPassPanel.add(currentPassField, BorderLayout.CENTER);
        
        // New Password
        JPanel newPassPanel = new JPanel(new BorderLayout(10, 0));
        JLabel newPassLabel = new JLabel("New Password:");
        newPassLabel.setPreferredSize(new Dimension(150, 25));
        JPasswordField newPassField = new JPasswordField();
        newPassPanel.add(newPassLabel, BorderLayout.WEST);
        newPassPanel.add(newPassField, BorderLayout.CENTER);
        
        // Confirm New Password
        JPanel confirmPassPanel = new JPanel(new BorderLayout(10, 0));
        JLabel confirmPassLabel = new JLabel("Confirm New Password:");
        confirmPassLabel.setPreferredSize(new Dimension(150, 25));
        JPasswordField confirmPassField = new JPasswordField();
        confirmPassPanel.add(confirmPassLabel, BorderLayout.WEST);
        confirmPassPanel.add(confirmPassField, BorderLayout.CENTER);
        
        // Status message
        JLabel statusLabel = new JLabel("");
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setForeground(Color.RED);
        
        // Add to fields panel
        fieldsPanel.add(currentPassPanel);
        fieldsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        fieldsPanel.add(newPassPanel);
        fieldsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        fieldsPanel.add(confirmPassPanel);
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        JButton updateButton = new JButton("Update Password");
        updateButton.setPreferredSize(new Dimension(150, 35));
        updateButton.addActionListener(e -> {
            String currentPass = new String(currentPassField.getPassword());
            String newPass = new String(newPassField.getPassword());
            String confirmPass = new String(confirmPassField.getPassword());
            
            // Validate inputs
            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                statusLabel.setText("All fields are required");
                return;
            }
            
            if (!newPass.equals(confirmPass)) {
                statusLabel.setText("New passwords do not match");
                return;
            }
            
            // Verify current password
            UserDAO userDAO = new UserDAO();
            User currentUser = userDAO.validateUser(user.getUsername(), currentPass);
            
            if (currentUser == null) {
                statusLabel.setText("Current password is incorrect");
                return;
            }
            
            // Change password
            boolean success = userDAO.changePassword(user.getId(), newPass);
            
            if (success) {
                // Update user object
                user.setPassword(newPass);
                
                // Clear fields
                currentPassField.setText("");
                newPassField.setText("");
                confirmPassField.setText("");
                
                // Show success message
                statusLabel.setForeground(new Color(0, 128, 0));
                statusLabel.setText("Password updated successfully");
            } else {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Failed to update password");
            }
        });
        
        buttonPanel.add(updateButton);
        
        // Add to form panel
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add to main panel
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        
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