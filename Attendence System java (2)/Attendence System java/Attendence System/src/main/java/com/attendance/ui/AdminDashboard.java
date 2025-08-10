package com.attendance.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.attendance.dao.FacultyDAO;
import com.attendance.dao.SubjectDAO;
import com.attendance.dao.UserDAO;
import com.attendance.model.Faculty;
import com.attendance.model.Subject;
import com.attendance.model.User;

public class AdminDashboard extends JFrame {
    private User user;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    // Card names
    private static final String WELCOME_PANEL = "Welcome";
    private static final String MANAGE_FACULTY_PANEL = "ManageFaculty";
    private static final String MANAGE_SUBJECTS_PANEL = "ManageSubjects";
    
    // Menu width
    private static final int MENU_WIDTH = 220;
    
    // Components for faculty management
    private JTable facultyTable;
    private DefaultTableModel facultyTableModel;
    private JTextField nameField, departmentField, usernameField;
    private JPasswordField passwordField;
    private JButton addButton, updateButton, deleteButton;
    private int selectedFacultyId = -1;
    
    // Components for subject management
    private JTable subjectTable;
    private DefaultTableModel subjectTableModel;
    private JTextField subjectNameField, subjectCodeField, subjectSemesterField, subjectDepartmentField;
    private JComboBox<Faculty> facultyComboBox;
    private JButton addSubjectButton, updateSubjectButton, deleteSubjectButton;
    private int selectedSubjectId = -1;
    
    public AdminDashboard(User user) {
        this.user = user;
        setupUI();
    }
    
    private void setupUI() {
        setTitle("Admin Dashboard - " + user.getName());
        setSize(900, 600);
        setMinimumSize(new Dimension(750, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create menu panel
        JPanel menuPanel = createMenuPanel();
        menuPanel.setMinimumSize(new Dimension(MENU_WIDTH, 300));
        menuPanel.setPreferredSize(new Dimension(MENU_WIDTH, 600));
        
        // Create content panel with card layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setMinimumSize(new Dimension(400, 300));
        
        // Add cards to main panel
        mainPanel.add(createWelcomePanel(), WELCOME_PANEL);
        mainPanel.add(createManageFacultyPanel(), MANAGE_FACULTY_PANEL);
        mainPanel.add(createManageSubjectsPanel(), MANAGE_SUBJECTS_PANEL);
        
        // Create a JSplitPane
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
        
        JLabel nameLabel = new JLabel("Welcome, " + user.getName());
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel roleLabel = new JLabel("Role: Administrator");
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        userInfoPanel.add(nameLabel);
        userInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        userInfoPanel.add(roleLabel);
        
        // Menu buttons
        JButton homeBtn = createMenuButton("Home");
        JButton manageFacultyBtn = createMenuButton("Manage Faculty");
        JButton manageSubjectsBtn = createMenuButton("Manage Subjects");
        JButton logoutBtn = createMenuButton("Logout");
        
        // Add action listeners
        homeBtn.addActionListener(e -> cardLayout.show(mainPanel, WELCOME_PANEL));
        manageFacultyBtn.addActionListener(e -> {
            refreshFacultyTable();
            cardLayout.show(mainPanel, MANAGE_FACULTY_PANEL);
        });
        manageSubjectsBtn.addActionListener(e -> {
            refreshSubjectTable();
            refreshFacultyComboBox();
            cardLayout.show(mainPanel, MANAGE_SUBJECTS_PANEL);
        });
        logoutBtn.addActionListener(e -> logout());
        
        // Add components to panel with spacing
        panel.add(userInfoPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(homeBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(manageFacultyBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(manageSubjectsBtn);
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
        
        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JLabel welcomeLabel = new JLabel("Welcome to the Administrator Dashboard");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel descriptionLabel = new JLabel("Here you can manage faculty members and system settings");
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        infoPanel.add(welcomeLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        infoPanel.add(descriptionLabel);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createManageFacultyPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Manage Faculty");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Faculty Table
        String[] columns = {"ID", "Name", "Department", "Username"};
        facultyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        facultyTable = new JTable(facultyTableModel);
        JScrollPane tableScrollPane = new JScrollPane(facultyTable);
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Faculty Details"));
        
        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField(20);
        formPanel.add(nameField);
        
        formPanel.add(new JLabel("Department:"));
        departmentField = new JTextField(20);
        formPanel.add(departmentField);
        
        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField(20);
        formPanel.add(usernameField);
        
        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField);
        
        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        addButton = new JButton("Add Faculty");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear Form");
        
        buttonsPanel.add(addButton);
        buttonsPanel.add(updateButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(clearButton);
        
        // Right Panel (Form + Buttons)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(formPanel, BorderLayout.CENTER);
        rightPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Split panel between table and form
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            tableScrollPane,
            rightPanel
        );
        splitPane.setResizeWeight(0.6);
        panel.add(splitPane, BorderLayout.CENTER);
        
        // Add table selection listener
        facultyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && facultyTable.getSelectedRow() != -1) {
                int row = facultyTable.getSelectedRow();
                selectedFacultyId = (int) facultyTable.getValueAt(row, 0);
                nameField.setText((String) facultyTable.getValueAt(row, 1));
                departmentField.setText((String) facultyTable.getValueAt(row, 2));
                usernameField.setText((String) facultyTable.getValueAt(row, 3));
                passwordField.setText(""); // Clear password field for security
                
                // Enable update/delete buttons
                updateButton.setEnabled(true);
                deleteButton.setEnabled(true);
            }
        });
        
        // Add action listeners for buttons
        addButton.addActionListener(e -> addFaculty());
        updateButton.addActionListener(e -> updateFaculty());
        deleteButton.addActionListener(e -> deleteFaculty());
        clearButton.addActionListener(e -> clearForm());
        
        // Initial state
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        refreshFacultyTable();
        
        return panel;
    }
    
    private void refreshFacultyTable() {
        // Clear table
        facultyTableModel.setRowCount(0);
        
        // Get all faculty and populate table
        FacultyDAO facultyDAO = new FacultyDAO();
        List<Faculty> facultyList = facultyDAO.getAllFaculty();
        
        UserDAO userDAO = new UserDAO();
        
        for (Faculty faculty : facultyList) {
            User facultyUser = userDAO.getUserById(faculty.getUserId());
            String username = facultyUser != null ? facultyUser.getUsername() : "Unknown";
            
            Object[] row = {
                faculty.getId(),
                faculty.getName(),
                faculty.getDepartment(),
                username
            };
            facultyTableModel.addRow(row);
        }
    }
    
    private void addFaculty() {
        String name = nameField.getText().trim();
        String department = departmentField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (name.isEmpty() || department.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Create new user
            UserDAO userDAO = new UserDAO();
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setRole("faculty");
            newUser.setName(name);
            
            int userId = userDAO.addUser(newUser);
            
            if (userId > 0) {
                // Create faculty record
                Faculty faculty = new Faculty();
                faculty.setName(name);
                faculty.setDepartment(department);
                faculty.setUserId(userId);
                
                FacultyDAO facultyDAO = new FacultyDAO();
                boolean success = facultyDAO.addFaculty(faculty);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Faculty added successfully!");
                    clearForm();
                    refreshFacultyTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add faculty record", "Error", JOptionPane.ERROR_MESSAGE);
                    // Clean up the user we just added
                    userDAO.deleteUser(userId);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Username may already exist", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateFaculty() {
        if (selectedFacultyId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a faculty to update", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String name = nameField.getText().trim();
        String department = departmentField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (name.isEmpty() || department.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and department cannot be empty", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            FacultyDAO facultyDAO = new FacultyDAO();
            Faculty faculty = facultyDAO.getFacultyById(selectedFacultyId);
            
            if (faculty != null) {
                // Update faculty info
                faculty.setName(name);
                faculty.setDepartment(department);
                boolean success = facultyDAO.updateFaculty(faculty);
                
                // If password was provided, update user password
                if (!password.isEmpty()) {
                    UserDAO userDAO = new UserDAO();
                    User user = userDAO.getUserById(faculty.getUserId());
                    if (user != null) {
                        user.setPassword(password);
                        user.setName(name); // Keep user name in sync with faculty name
                        userDAO.updateUser(user);
                    }
                }
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Faculty updated successfully!");
                    clearForm();
                    refreshFacultyTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update faculty", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteFaculty() {
        if (selectedFacultyId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a faculty to delete", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this faculty?\nThis will also delete the user account and course assignments.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                FacultyDAO facultyDAO = new FacultyDAO();
                Faculty faculty = facultyDAO.getFacultyById(selectedFacultyId);
                
                if (faculty != null) {
                    boolean success = facultyDAO.deleteFaculty(selectedFacultyId);
                    
                    if (success) {
                        // Delete the user account too
                        UserDAO userDAO = new UserDAO();
                        userDAO.deleteUser(faculty.getUserId());
                        
                        JOptionPane.showMessageDialog(this, "Faculty deleted successfully!");
                        clearForm();
                        refreshFacultyTable();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete faculty", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearForm() {
        nameField.setText("");
        departmentField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        selectedFacultyId = -1;
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        facultyTable.clearSelection();
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
    
    private JPanel createManageSubjectsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Manage Subjects");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Subjects Table
        String[] columns = {"ID", "Subject Name", "Code", "Faculty", "Semester", "Department"};
        subjectTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        subjectTable = new JTable(subjectTableModel);
        JScrollPane tableScrollPane = new JScrollPane(subjectTable);
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Subject Details"));
        
        // Subject name field
        formPanel.add(new JLabel("Subject Name:"));
        subjectNameField = new JTextField(20);
        formPanel.add(subjectNameField);
        
        // Subject code field
        formPanel.add(new JLabel("Subject Code:"));
        subjectCodeField = new JTextField(20);
        formPanel.add(subjectCodeField);
        
        // Faculty selection
        formPanel.add(new JLabel("Assigned Faculty:"));
        facultyComboBox = new JComboBox<>();
        formPanel.add(facultyComboBox);
        
        // Semester field
        formPanel.add(new JLabel("Semester:"));
        subjectSemesterField = new JTextField(20);
        formPanel.add(subjectSemesterField);
        
        // Department field
        formPanel.add(new JLabel("Department:"));
        subjectDepartmentField = new JTextField(20);
        formPanel.add(subjectDepartmentField);
        
        // Spacer for grid layout
        formPanel.add(new JLabel(""));
        formPanel.add(new JLabel(""));
        
        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        addSubjectButton = new JButton("Add Subject");
        updateSubjectButton = new JButton("Update");
        deleteSubjectButton = new JButton("Delete");
        JButton clearSubjectButton = new JButton("Clear Form");
        
        buttonsPanel.add(addSubjectButton);
        buttonsPanel.add(updateSubjectButton);
        buttonsPanel.add(deleteSubjectButton);
        buttonsPanel.add(clearSubjectButton);
        
        // Right Panel (Form + Buttons)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(formPanel, BorderLayout.CENTER);
        rightPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Split panel between table and form
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            tableScrollPane,
            rightPanel
        );
        splitPane.setResizeWeight(0.6);
        panel.add(splitPane, BorderLayout.CENTER);
        
        // Add table selection listener
        subjectTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && subjectTable.getSelectedRow() != -1) {
                int row = subjectTable.getSelectedRow();
                selectedSubjectId = (int) subjectTable.getValueAt(row, 0);
                
                SubjectDAO subjectDAO = new SubjectDAO();
                Subject subject = subjectDAO.getSubjectById(selectedSubjectId);
                
                if (subject != null) {
                    subjectNameField.setText(subject.getName());
                    subjectCodeField.setText(subject.getCode());
                    
                    // Set the selected faculty in combobox
                    FacultyDAO facultyDAO = new FacultyDAO();
                    Faculty faculty = facultyDAO.getFacultyById(subject.getFacultyId());
                    if (faculty != null) {
                        for (int i = 0; i < facultyComboBox.getItemCount(); i++) {
                            Faculty f = facultyComboBox.getItemAt(i);
                            if (f.getId() == faculty.getId()) {
                                facultyComboBox.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                    
                    subjectSemesterField.setText(subject.getSemester());
                    subjectDepartmentField.setText(subject.getDepartment());
                    
                    // Enable update/delete buttons
                    updateSubjectButton.setEnabled(true);
                    deleteSubjectButton.setEnabled(true);
                }
            }
        });
        
        // Add action listeners for buttons
        addSubjectButton.addActionListener(e -> addSubject());
        updateSubjectButton.addActionListener(e -> updateSubject());
        deleteSubjectButton.addActionListener(e -> deleteSubject());
        clearSubjectButton.addActionListener(e -> clearSubjectForm());
        
        // Initial state
        updateSubjectButton.setEnabled(false);
        deleteSubjectButton.setEnabled(false);
        refreshSubjectTable();
        refreshFacultyComboBox();
        
        return panel;
    }
    
    private void refreshSubjectTable() {
        // Clear table
        subjectTableModel.setRowCount(0);
        
        // Get all subjects
        SubjectDAO subjectDAO = new SubjectDAO();
        List<Subject> subjects = subjectDAO.getAllSubjects();
        
        FacultyDAO facultyDAO = new FacultyDAO();
        
        for (Subject subject : subjects) {
            Faculty faculty = facultyDAO.getFacultyById(subject.getFacultyId());
            String facultyName = faculty != null ? faculty.getName() : "Not Assigned";
            
            Object[] row = {
                subject.getId(),
                subject.getName(),
                subject.getCode(),
                facultyName,
                subject.getSemester(),
                subject.getDepartment()
            };
            subjectTableModel.addRow(row);
        }
    }
    
    private void refreshFacultyComboBox() {
        // Clear combo box
        facultyComboBox.removeAllItems();
        
        // Get all faculty
        FacultyDAO facultyDAO = new FacultyDAO();
        List<Faculty> facultyList = facultyDAO.getAllFaculty();
        
        DefaultComboBoxModel<Faculty> model = new DefaultComboBoxModel<>();
        for (Faculty faculty : facultyList) {
            model.addElement(faculty);
        }
        
        facultyComboBox.setModel(model);
        facultyComboBox.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Faculty) {
                    Faculty faculty = (Faculty) value;
                    setText(faculty.getName() + " (" + faculty.getDepartment() + ")");
                }
                return this;
            }
        });
    }
    
    private void addSubject() {
        String name = subjectNameField.getText().trim();
        String code = subjectCodeField.getText().trim();
        String semester = subjectSemesterField.getText().trim();
        String department = subjectDepartmentField.getText().trim();
        
        Faculty selectedFaculty = (Faculty) facultyComboBox.getSelectedItem();
        
        if (name.isEmpty() || code.isEmpty() || selectedFaculty == null) {
            JOptionPane.showMessageDialog(this, "Subject name, code, and faculty are required", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            SubjectDAO subjectDAO = new SubjectDAO();
            
            // Create new subject
            Subject subject = new Subject();
            subject.setName(name);
            subject.setCode(code);
            subject.setFacultyId(selectedFaculty.getId());
            subject.setSemester(semester);
            subject.setDepartment(department);
            
            // Generate new ID
            int nextId = subjectDAO.getMaxSubjectId() + 1;
            subject.setId(nextId);
            
            boolean success = subjectDAO.addSubject(subject);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Subject added successfully!");
                clearSubjectForm();
                refreshSubjectTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add subject. Code may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateSubject() {
        if (selectedSubjectId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subject to update", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String name = subjectNameField.getText().trim();
        String code = subjectCodeField.getText().trim();
        String semester = subjectSemesterField.getText().trim();
        String department = subjectDepartmentField.getText().trim();
        
        Faculty selectedFaculty = (Faculty) facultyComboBox.getSelectedItem();
        
        if (name.isEmpty() || code.isEmpty() || selectedFaculty == null) {
            JOptionPane.showMessageDialog(this, "Subject name, code, and faculty are required", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            SubjectDAO subjectDAO = new SubjectDAO();
            Subject subject = subjectDAO.getSubjectById(selectedSubjectId);
            
            if (subject != null) {
                // Update subject
                subject.setName(name);
                subject.setCode(code);
                subject.setFacultyId(selectedFaculty.getId());
                subject.setSemester(semester);
                subject.setDepartment(department);
                
                boolean success = subjectDAO.updateSubject(subject);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Subject updated successfully!");
                    clearSubjectForm();
                    refreshSubjectTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update subject", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteSubject() {
        if (selectedSubjectId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subject to delete", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this subject?\nThis will also delete student enrollments and attendance records.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                SubjectDAO subjectDAO = new SubjectDAO();
                boolean success = subjectDAO.deleteSubject(selectedSubjectId);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Subject deleted successfully!");
                    clearSubjectForm();
                    refreshSubjectTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete subject", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearSubjectForm() {
        subjectNameField.setText("");
        subjectCodeField.setText("");
        subjectSemesterField.setText("");
        subjectDepartmentField.setText("");
        selectedSubjectId = -1;
        updateSubjectButton.setEnabled(false);
        deleteSubjectButton.setEnabled(false);
        subjectTable.clearSelection();
        
        if (facultyComboBox.getItemCount() > 0) {
            facultyComboBox.setSelectedIndex(0);
        }
    }
} 