package com.attendance.ui.panels;

/**
 * DEPRECATED - This panel is no longer used in the application.
 * Course management functionality has been moved to Admin dashboards only.
 * This file is kept for reference purposes and should not be used in new code.
 */

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.attendance.dao.SubjectDAO;
import com.attendance.model.Subject;

public class ManageCoursesPanel extends JPanel {
    private int facultyId;
    private JTable coursesTable;
    private DefaultTableModel coursesTableModel;
    private JButton addCourseButton;
    private JButton removeCourseButton;
    private JButton editCourseButton;
    private JButton refreshButton;
    
    public ManageCoursesPanel(int facultyId) {
        this.facultyId = facultyId;
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Create title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Manage Your Courses");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(titleLabel);
        
        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        addCourseButton = new JButton("Add New Course");
        addCourseButton.addActionListener(e -> addNewCourse());
        
        editCourseButton = new JButton("Edit Selected Course");
        editCourseButton.setEnabled(false);
        editCourseButton.addActionListener(e -> editSelectedCourse());
        
        removeCourseButton = new JButton("Remove Selected Course");
        removeCourseButton.setEnabled(false);
        removeCourseButton.addActionListener(e -> removeSelectedCourse());
        
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadCourses());
        
        buttonPanel.add(addCourseButton);
        buttonPanel.add(editCourseButton);
        buttonPanel.add(removeCourseButton);
        buttonPanel.add(refreshButton);
        
        // Create course table
        String[] columns = {"ID", "Course Code", "Course Name", "Semester", "Department"};
        coursesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        coursesTable = new JTable(coursesTableModel);
        coursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Hide ID column
        coursesTable.getColumnModel().getColumn(0).setMinWidth(0);
        coursesTable.getColumnModel().getColumn(0).setMaxWidth(0);
        coursesTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Set column widths
        coursesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        coursesTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        coursesTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        coursesTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(coursesTable);
        
        // Add selection listener
        coursesTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = coursesTable.getSelectedRow() != -1;
            editCourseButton.setEnabled(hasSelection);
            removeCourseButton.setEnabled(hasSelection);
        });
        
        // Add components to main panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Load courses
        loadCourses();
    }
    
    private void loadCourses() {
        coursesTableModel.setRowCount(0);
        
        SubjectDAO subjectDAO = new SubjectDAO();
        List<Subject> subjects = subjectDAO.getSubjectsByFacultyId(facultyId);
        
        for (Subject subject : subjects) {
            Object[] row = {
                subject.getId(),
                subject.getCode(),
                subject.getName(),
                subject.getSemester(),
                subject.getDepartment()
            };
            coursesTableModel.addRow(row);
        }
        
        if (subjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "You don't have any courses assigned yet.\nClick 'Add New Course' to create one.", 
                    "No Courses", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void addNewCourse() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        
        JTextField codeField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField semesterField = new JTextField();
        JTextField departmentField = new JTextField();
        
        panel.add(new JLabel("Course Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Course Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Semester:"));
        panel.add(semesterField);
        panel.add(new JLabel("Department:"));
        panel.add(departmentField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Course", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            String semester = semesterField.getText().trim();
            String department = departmentField.getText().trim();
            
            if (code.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                        "Course code and name are required!", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            SubjectDAO subjectDAO = new SubjectDAO();
            
            // Get next available ID
            int nextId = subjectDAO.getMaxSubjectId() + 1;
            
            // Create subject object
            Subject subject = new Subject();
            subject.setId(nextId);
            subject.setCode(code);
            subject.setName(name);
            subject.setSemester(semester);
            subject.setDepartment(department);
            subject.setFacultyId(facultyId);
            
            boolean success = subjectDAO.addSubject(subject);
            
            if (success) {
                JOptionPane.showMessageDialog(this, 
                        "Course added successfully", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                loadCourses();
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Failed to add course. Please try again.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editSelectedCourse() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        int subjectId = (int) coursesTableModel.getValueAt(selectedRow, 0);
        String code = (String) coursesTableModel.getValueAt(selectedRow, 1);
        String name = (String) coursesTableModel.getValueAt(selectedRow, 2);
        String semester = (String) coursesTableModel.getValueAt(selectedRow, 3);
        String department = (String) coursesTableModel.getValueAt(selectedRow, 4);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        
        JTextField codeField = new JTextField(code);
        JTextField nameField = new JTextField(name);
        JTextField semesterField = new JTextField(semester);
        JTextField departmentField = new JTextField(department);
        
        panel.add(new JLabel("Course Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Course Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Semester:"));
        panel.add(semesterField);
        panel.add(new JLabel("Department:"));
        panel.add(departmentField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Course", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String newCode = codeField.getText().trim();
            String newName = nameField.getText().trim();
            String newSemester = semesterField.getText().trim();
            String newDepartment = departmentField.getText().trim();
            
            if (newCode.isEmpty() || newName.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                        "Course code and name are required!", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            SubjectDAO subjectDAO = new SubjectDAO();
            
            // Create updated subject object
            Subject subject = new Subject();
            subject.setId(subjectId);
            subject.setCode(newCode);
            subject.setName(newName);
            subject.setSemester(newSemester);
            subject.setDepartment(newDepartment);
            subject.setFacultyId(facultyId);
            
            boolean success = subjectDAO.updateSubject(subject);
            
            if (success) {
                JOptionPane.showMessageDialog(this, 
                        "Course updated successfully", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                loadCourses();
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Failed to update course. Please try again.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void removeSelectedCourse() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        int subjectId = (int) coursesTableModel.getValueAt(selectedRow, 0);
        String courseName = (String) coursesTableModel.getValueAt(selectedRow, 2);
        
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove the course: " + courseName + "?\n" +
                "This will also remove all student enrollments and attendance records for this course.",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        
        SubjectDAO subjectDAO = new SubjectDAO();
        boolean success = subjectDAO.deleteSubject(subjectId);
        
        if (success) {
            JOptionPane.showMessageDialog(this, 
                    "Course removed successfully", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            loadCourses();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Failed to remove course. Please try again.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
} 