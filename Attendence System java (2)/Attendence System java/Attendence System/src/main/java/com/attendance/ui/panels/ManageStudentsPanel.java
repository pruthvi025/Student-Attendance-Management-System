package com.attendance.ui.panels;

import com.attendance.dao.StudentDAO;
import com.attendance.dao.SubjectDAO;
import com.attendance.model.Student;
import com.attendance.model.Subject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ManageStudentsPanel extends JPanel {
    private int facultyId;
    private JComboBox<Subject> subjectComboBox;
    private JTable studentsTable;
    private DefaultTableModel studentsTableModel;
    private JTable availableStudentsTable;
    private DefaultTableModel availableStudentsTableModel;
    private JPanel mainPanel;
    private JButton addSelectedButton;
    private JButton removeSelectedButton;
    private JButton addNewStudentButton;
    private JButton deleteStudentButton;
    
    // Track selected subject
    private Subject selectedSubject;
    
    public ManageStudentsPanel(int facultyId) {
        this.facultyId = facultyId;
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Create title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Manage Students");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(titleLabel);
        
        // Create subject selection panel
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel subjectLabel = new JLabel("Select Subject:");
        subjectComboBox = new JComboBox<>();
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadSubjects());
        
        selectionPanel.add(subjectLabel);
        selectionPanel.add(subjectComboBox);
        selectionPanel.add(refreshButton);
        
        // Create main panel with split layout
        mainPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // Left panel: Students enrolled in subject
        JPanel enrolledPanel = new JPanel(new BorderLayout());
        enrolledPanel.setBorder(BorderFactory.createTitledBorder("Students Enrolled in Subject"));
        
        // Create enrolled students table
        String[] enrolledColumns = {"ID", "Roll No", "Name", "Course"};
        studentsTableModel = new DefaultTableModel(enrolledColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentsTable = new JTable(studentsTableModel);
        studentsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Hide ID column
        studentsTable.getColumnModel().getColumn(0).setMinWidth(0);
        studentsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        studentsTable.getColumnModel().getColumn(0).setWidth(0);
        
        JScrollPane enrolledScrollPane = new JScrollPane(studentsTable);
        
        // Button to remove students from course
        removeSelectedButton = new JButton("Remove Selected Students from Course");
        removeSelectedButton.setEnabled(false);
        removeSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedStudents();
            }
        });
        
        // Button to delete students completely
        deleteStudentButton = new JButton("Delete Selected Students");
        deleteStudentButton.setEnabled(false);
        deleteStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedStudents();
            }
        });
        
        // Panel for buttons
        JPanel enrolledButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        enrolledButtonPanel.add(removeSelectedButton);
        enrolledButtonPanel.add(deleteStudentButton);
        
        enrolledPanel.add(enrolledScrollPane, BorderLayout.CENTER);
        enrolledPanel.add(enrolledButtonPanel, BorderLayout.SOUTH);
        
        // Right panel: Available students not in the course
        JPanel availablePanel = new JPanel(new BorderLayout());
        availablePanel.setBorder(BorderFactory.createTitledBorder("Available Students (Not Enrolled)"));
        
        // Create available students table
        String[] availableColumns = {"ID", "Roll No", "Name", "Course"};
        availableStudentsTableModel = new DefaultTableModel(availableColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        availableStudentsTable = new JTable(availableStudentsTableModel);
        availableStudentsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Hide ID column
        availableStudentsTable.getColumnModel().getColumn(0).setMinWidth(0);
        availableStudentsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        availableStudentsTable.getColumnModel().getColumn(0).setWidth(0);
        
        JScrollPane availableScrollPane = new JScrollPane(availableStudentsTable);
        
        // Button to add students to course
        addSelectedButton = new JButton("Add Selected Students to Course");
        addSelectedButton.setEnabled(false);
        addSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSelectedStudents();
            }
        });
        
        // Button to add new student
        addNewStudentButton = new JButton("Add New Student");
        addNewStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewStudent();
            }
        });
        
        // Panel for buttons
        JPanel availableButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        availableButtonPanel.add(addSelectedButton);
        availableButtonPanel.add(addNewStudentButton);
        
        availablePanel.add(availableScrollPane, BorderLayout.CENTER);
        availablePanel.add(availableButtonPanel, BorderLayout.SOUTH);
        
        // Add panels to main panel
        mainPanel.add(enrolledPanel);
        mainPanel.add(availablePanel);
        
        // Add selection and event listeners
        subjectComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (subjectComboBox.getSelectedItem() != null) {
                    selectedSubject = (Subject) subjectComboBox.getSelectedItem();
                    loadStudentTables();
                }
            }
        });
        
        studentsTable.getSelectionModel().addListSelectionListener(e -> {
            removeSelectedButton.setEnabled(studentsTable.getSelectedRowCount() > 0);
            deleteStudentButton.setEnabled(studentsTable.getSelectedRowCount() > 0);
        });
        
        availableStudentsTable.getSelectionModel().addListSelectionListener(e -> {
            addSelectedButton.setEnabled(availableStudentsTable.getSelectedRowCount() > 0);
        });
        
        // Add everything to main layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(selectionPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        
        // Load subjects AFTER initializing all UI components
        loadSubjects();
    }
    
    private void loadSubjects() {
        SubjectDAO subjectDAO = new SubjectDAO();
        List<Subject> subjects = subjectDAO.getSubjectsByFacultyId(facultyId);
        
        subjectComboBox.removeAllItems();
        
        if (subjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "No subjects assigned to you", 
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Subject subject : subjects) {
                subjectComboBox.addItem(subject);
            }
            
            // Automatically select first subject and load students
            if (subjectComboBox.getItemCount() > 0) {
                selectedSubject = (Subject) subjectComboBox.getItemAt(0);
                loadStudentTables();
            }
        }
    }
    
    private void loadStudentTables() {
        if (selectedSubject == null) {
            return;
        }
        
        // Clear tables
        studentsTableModel.setRowCount(0);
        availableStudentsTableModel.setRowCount(0);
        
        // Load enrolled students
        StudentDAO studentDAO = new StudentDAO();
        List<Student> enrolledStudents = studentDAO.getStudentsBySubject(selectedSubject.getId());
        
        for (Student student : enrolledStudents) {
            Object[] row = {
                student.getId(),
                student.getRollNo(),
                student.getName(),
                student.getCourse()
            };
            studentsTableModel.addRow(row);
        }
        
        // Load available students (not enrolled)
        List<Student> availableStudents = studentDAO.getStudentsNotInSubject(selectedSubject.getId());
        
        for (Student student : availableStudents) {
            Object[] row = {
                student.getId(),
                student.getRollNo(),
                student.getName(),
                student.getCourse()
            };
            availableStudentsTableModel.addRow(row);
        }
        
        // Update panel titles
        ((javax.swing.border.TitledBorder) ((JPanel) mainPanel.getComponent(0)).getBorder()).setTitle(
                "Students Enrolled in Subject (" + enrolledStudents.size() + ")");
        ((javax.swing.border.TitledBorder) ((JPanel) mainPanel.getComponent(1)).getBorder()).setTitle(
                "Available Students (" + availableStudents.size() + ")");
        
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private void addSelectedStudents() {
        if (selectedSubject == null || availableStudentsTable.getSelectedRowCount() == 0) {
            return;
        }
        
        int[] selectedRows = availableStudentsTable.getSelectedRows();
        StudentDAO studentDAO = new StudentDAO();
        int successCount = 0;
        
        for (int row : selectedRows) {
            int studentId = (int) availableStudentsTableModel.getValueAt(row, 0);
            boolean success = studentDAO.enrollStudentInSubject(studentId, selectedSubject.getId());
            if (success) {
                successCount++;
            }
        }
        
        if (successCount > 0) {
            JOptionPane.showMessageDialog(this, 
                    "Added " + successCount + " student(s) to " + selectedSubject.getName(), 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            loadStudentTables();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Failed to add students to subject", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void removeSelectedStudents() {
        if (selectedSubject == null || studentsTable.getSelectedRowCount() == 0) {
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove these students from the course?\n" +
                "(This will also remove their attendance records for this course)",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION);
                
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        
        int[] selectedRows = studentsTable.getSelectedRows();
        StudentDAO studentDAO = new StudentDAO();
        int successCount = 0;
        
        for (int row : selectedRows) {
            int studentId = (int) studentsTableModel.getValueAt(row, 0);
            boolean success = studentDAO.removeStudentFromSubject(studentId, selectedSubject.getId());
            if (success) {
                successCount++;
            }
        }
        
        if (successCount > 0) {
            JOptionPane.showMessageDialog(this, 
                    "Removed " + successCount + " student(s) from " + selectedSubject.getName(), 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            loadStudentTables();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Failed to remove students from subject", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteSelectedStudents() {
        if (studentsTable.getSelectedRowCount() == 0) {
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to DELETE these students?\n" +
                "This will permanently remove them from all courses and the system.\n" +
                "THIS ACTION CANNOT BE UNDONE!",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        
        int[] selectedRows = studentsTable.getSelectedRows();
        StudentDAO studentDAO = new StudentDAO();
        int successCount = 0;
        
        for (int row : selectedRows) {
            int studentId = (int) studentsTableModel.getValueAt(row, 0);
            boolean success = studentDAO.deleteStudent(studentId);
            if (success) {
                successCount++;
            }
        }
        
        if (successCount > 0) {
            JOptionPane.showMessageDialog(this, 
                    "Deleted " + successCount + " student(s) from the system", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            loadStudentTables();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Failed to delete students", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addNewStudent() {
        // Create a dialog to get student information
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JTextField nameField = new JTextField();
        JTextField rollNoField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JCheckBox enrollCheckBox = new JCheckBox("Enroll in current subject");
        enrollCheckBox.setSelected(true);
        
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Roll No:"));
        panel.add(rollNoField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel(""));
        panel.add(enrollCheckBox);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Student", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String rollNo = rollNoField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (name.isEmpty() || rollNo.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Get new IDs
            StudentDAO studentDAO = new StudentDAO();
            int newStudentId = studentDAO.getMaxStudentId() + 1;
            int newUserId = studentDAO.getMaxUserId() + 1;
            
            // Create student object with default course value
            Student student = new Student();
            student.setId(newStudentId);
            student.setName(name);
            student.setRollNo(rollNo);
            student.setCourse("Default Course"); // Set a default course value
            student.setUserId(newUserId);
            
            // Add student to database
            boolean success = studentDAO.addStudent(student, password);
            
            if (success) {
                // Enroll student in current subject if checkbox is selected
                if (enrollCheckBox.isSelected() && selectedSubject != null) {
                    studentDAO.enrollStudentInSubject(newStudentId, selectedSubject.getId());
                }
                
                // Show login credentials to faculty
                JPanel infoPanel = new JPanel(new GridLayout(3, 1));
                infoPanel.add(new JLabel("Student added successfully!"));
                infoPanel.add(new JLabel("Login username: " + rollNo.toLowerCase()));
                infoPanel.add(new JLabel("Password: " + password));
                
                JOptionPane.showMessageDialog(this, 
                        infoPanel, 
                        "Student Login Information", 
                        JOptionPane.INFORMATION_MESSAGE);
                
                loadStudentTables();
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Failed to add student. Check if roll number is unique.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 