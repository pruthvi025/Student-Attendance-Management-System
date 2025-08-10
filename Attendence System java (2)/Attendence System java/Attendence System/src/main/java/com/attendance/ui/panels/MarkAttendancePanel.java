package com.attendance.ui.panels;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.dao.SubjectDAO;
import com.attendance.model.Student;
import com.attendance.model.Subject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.AbstractCellEditor;
import javax.swing.ButtonGroup;

public class MarkAttendancePanel extends JPanel {
    private int facultyId;
    private JComboBox<Subject> subjectComboBox;
    private JPanel attendanceTablePanel;
    private JPanel mainPanel;
    private JTable studentTable;
    private DefaultTableModel tableModel;
    
    // Replace JDateChooser with standard components
    private JComboBox<String> yearComboBox;
    private JComboBox<String> monthComboBox;
    private JComboBox<String> dayComboBox;
    
    private JButton markAttendanceButton;
    private JButton showStudentsButton;
    private JButton debugButton;
    
    private Map<Integer, Boolean> attendanceMap = new HashMap<>();
    
    public MarkAttendancePanel(int facultyId) {
        this.facultyId = facultyId;
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Create title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Mark Attendance");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(titleLabel);
        
        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create top controls panel (subject and date selection)
        JPanel topControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Subject selection
        JLabel subjectLabel = new JLabel("Select Subject:");
        subjectComboBox = new JComboBox<>();
        loadSubjects();
        
        // Date selection using standard components
        JLabel dateLabel = new JLabel("Select Date:");
        JPanel datePanel = createDatePanel();
        
        // Add controls to top panel
        topControlsPanel.add(subjectLabel);
        topControlsPanel.add(subjectComboBox);
        topControlsPanel.add(dateLabel);
        topControlsPanel.add(datePanel);
        
        // Create button panel for action buttons
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Button to show students - make it more prominent
        showStudentsButton = new JButton("SHOW STUDENTS");
        showStudentsButton.setFont(new Font("Arial", Font.BOLD, 14));
        showStudentsButton.setBackground(new Color(0, 120, 215));
        showStudentsButton.setForeground(Color.WHITE);
        showStudentsButton.setFocusPainted(false);
        showStudentsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Show Students button clicked");
                loadStudentsForSubject();
            }
        });
        
        // Debug button
        debugButton = new JButton("Debug DB");
        debugButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                debugDatabase();
            }
        });
        
        // Add buttons to action panel
        actionButtonsPanel.add(showStudentsButton);
        actionButtonsPanel.add(debugButton);
        
        // Add both panels to form panel
        formPanel.add(topControlsPanel);
        formPanel.add(actionButtonsPanel);
        
        // Create attendance table panel with a visible border and fixed size
        attendanceTablePanel = new JPanel(new BorderLayout());
        attendanceTablePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.RED, 2), "Student Attendance (Click Show Students to load students)"));
        
        // Add an initial message
        JLabel initialLabel = new JLabel("No students loaded. Select a subject and date, then click 'Show Students'.");
        initialLabel.setHorizontalAlignment(JLabel.CENTER);
        attendanceTablePanel.add(initialLabel, BorderLayout.CENTER);
        
        // Wrap the attendance panel in a scroll pane to handle overflow
        JScrollPane attendanceScrollPane = new JScrollPane(attendanceTablePanel);
        attendanceScrollPane.setPreferredSize(new Dimension(600, 350));
        attendanceScrollPane.setMinimumSize(new Dimension(300, 200));
        
        // Create main panel to hold everything
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(attendanceScrollPane, BorderLayout.CENTER);
        
        // Add mark attendance button
        markAttendanceButton = new JButton("Save Attendance");
        markAttendanceButton.setEnabled(false);
        markAttendanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAttendance();
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(markAttendanceButton);
        
        // Add panels to main layout
        add(titlePanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createDatePanel() {
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        // Create year dropdown (current year and next year)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearComboBox = new JComboBox<>();
        yearComboBox.addItem(String.valueOf(currentYear - 1));
        yearComboBox.addItem(String.valueOf(currentYear));
        yearComboBox.addItem(String.valueOf(currentYear + 1));
        yearComboBox.setSelectedItem(String.valueOf(currentYear));
        
        // Create month dropdown
        String[] months = {"January", "February", "March", "April", "May", "June", 
                           "July", "August", "September", "October", "November", "December"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(Calendar.getInstance().get(Calendar.MONTH));
        
        // Create day dropdown
        dayComboBox = new JComboBox<>();
        updateDayComboBox();
        
        // Add listener to update days when month/year changes
        monthComboBox.addActionListener(e -> updateDayComboBox());
        yearComboBox.addActionListener(e -> updateDayComboBox());
        
        datePanel.add(dayComboBox);
        datePanel.add(monthComboBox);
        datePanel.add(yearComboBox);
        
        return datePanel;
    }
    
    private void updateDayComboBox() {
        int year = Integer.parseInt((String) yearComboBox.getSelectedItem());
        int month = monthComboBox.getSelectedIndex();
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Save selected day if possible
        int selectedDay = 1;
        if (dayComboBox.getSelectedItem() != null) {
            try {
                selectedDay = Integer.parseInt((String) dayComboBox.getSelectedItem());
            } catch (NumberFormatException e) {
                // Ignore, use default
            }
        }
        
        dayComboBox.removeAllItems();
        for (int i = 1; i <= daysInMonth; i++) {
            dayComboBox.addItem(String.valueOf(i));
        }
        
        // Try to restore selected day, if it's valid
        if (selectedDay <= daysInMonth) {
            dayComboBox.setSelectedItem(String.valueOf(selectedDay));
        }
    }
    
    private Date getSelectedDate() {
        try {
            int day = Integer.parseInt((String) dayComboBox.getSelectedItem());
            int month = monthComboBox.getSelectedIndex();
            int year = Integer.parseInt((String) yearComboBox.getSelectedItem());
            
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            return calendar.getTime();
        } catch (Exception e) {
            return null;
        }
    }
    
    private void loadSubjects() {
        SubjectDAO subjectDAO = new SubjectDAO();
        java.util.List<Subject> subjects = subjectDAO.getSubjectsByFacultyId(facultyId);
        
        if (subjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No subjects assigned to you", "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            subjectComboBox.removeAllItems();
            for (Subject subject : subjects) {
                subjectComboBox.addItem(subject);
            }
        }
    }
    
    private void loadStudentsForSubject() {
        System.out.println("Loading students for subject...");
        
        if (subjectComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a subject", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Subject selectedSubject = (Subject) subjectComboBox.getSelectedItem();
        Date selectedDate = getSelectedDate();
        
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid date", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Ensure fresh database connections
        resetDatabaseConnection();
        
        // Get students for this subject
        StudentDAO studentDAO = new StudentDAO();
        System.out.println("About to call getStudentsBySubject with subject ID: " + selectedSubject.getId());
        java.util.List<Student> students = studentDAO.getStudentsBySubject(selectedSubject.getId());
        
        // For debugging
        System.out.println("Loading students for subject: " + selectedSubject.getName() + " (ID: " + selectedSubject.getId() + ")");
        System.out.println("Number of students found: " + students.size());
        
        // Clear existing content from attendance panel
        attendanceTablePanel.removeAll();
        
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No students enrolled in this subject: " + selectedSubject.getName() + " (ID: " + selectedSubject.getId() + ")",
                "No Students Found", 
                JOptionPane.INFORMATION_MESSAGE);
                
            JLabel noStudentsLabel = new JLabel("No students found for this subject. Check student enrollments in the database.");
            noStudentsLabel.setHorizontalAlignment(JLabel.CENTER);
            noStudentsLabel.setForeground(Color.RED);
            attendanceTablePanel.add(noStudentsLabel, BorderLayout.CENTER);
            
            // Add a button to run diagnostic inside the panel
            JButton diagnoseButton = new JButton("Run Diagnostics");
            diagnoseButton.addActionListener(e -> debugDatabase());
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(diagnoseButton);
            attendanceTablePanel.add(buttonPanel, BorderLayout.SOUTH);
            
            attendanceTablePanel.revalidate();
            attendanceTablePanel.repaint();
            return;
        }
        
        // Create or update table
        createAttendanceTable(students);
        markAttendanceButton.setEnabled(true);
        
        // Update the border title to show how many students are loaded
        ((javax.swing.border.TitledBorder) attendanceTablePanel.getBorder()).setTitle(
            "Student Attendance (" + students.size() + " students loaded)");
        
        // Make sure UI is updated
        attendanceTablePanel.revalidate();
        attendanceTablePanel.repaint();
        this.revalidate();
        this.repaint();
    }
    
    /**
     * Reset the database connection to ensure fresh data
     */
    private void resetDatabaseConnection() {
        try {
            System.out.println("Resetting database connection...");
            
            // Close existing connection
            com.attendance.util.DBConnection.closeConnection();
            
            // Re-establish connection
            com.attendance.util.DBConnection.getConnection();
            
            // Verify the database connection
            java.sql.Connection conn = com.attendance.util.DBConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                // Run a simple query to test connection
                java.sql.Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM student_subjects");
                if (rs.next()) {
                    System.out.println("Connection reset successful. Found " + rs.getInt(1) + " student-subject enrollments.");
                }
                rs.close();
                stmt.close();
            } else {
                System.err.println("Failed to reset database connection!");
            }
        } catch (Exception e) {
            System.err.println("Error resetting database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createAttendanceTable(java.util.List<Student> students) {
        // Create table model with radio buttons for Present/Absent
        String[] columns = {"Roll No", "Name", "Attendance Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;  // Only attendance status column is editable
            }
        };
        
        // Add students to table
        attendanceMap.clear();
        for (Student student : students) {
            attendanceMap.put(student.getId(), false);  // Default to absent
            Object[] row = {student.getRollNo(), student.getName(), "Absent"};
            tableModel.addRow(row);
        }
        
        // Create table
        studentTable = new JTable(tableModel);
        
        // Set custom cell renderer and editor for attendance status column
        studentTable.getColumnModel().getColumn(2).setCellRenderer(new AttendanceStatusRenderer());
        studentTable.getColumnModel().getColumn(2).setCellEditor(new AttendanceStatusEditor(students));
        
        // Set table properties for better UI
        studentTable.setRowHeight(30);  // Increased height for radio buttons
        studentTable.setShowGrid(true);
        studentTable.setGridColor(Color.LIGHT_GRAY);
        studentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Set preferred column widths
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(100);  // Roll No
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(250);  // Name
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(150);  // Attendance Status
        
        // Add table to scroll pane and to panel
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        
        // Clear previous content and add the new scrollpane
        attendanceTablePanel.removeAll();
        attendanceTablePanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    // Custom renderer for attendance status column
    private class AttendanceStatusRenderer extends DefaultTableCellRenderer {
        private JPanel panel;
        private JRadioButton presentButton;
        private JRadioButton absentButton;
        
        public AttendanceStatusRenderer() {
            panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            presentButton = new JRadioButton("Present");
            absentButton = new JRadioButton("Absent");
            
            ButtonGroup group = new ButtonGroup();
            group.add(presentButton);
            group.add(absentButton);
            
            panel.add(presentButton);
            panel.add(absentButton);
            
            // Set colors
            presentButton.setBackground(new Color(220, 255, 220)); // Light green
            absentButton.setBackground(new Color(255, 220, 220));  // Light red
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Reset selection
            presentButton.setSelected(false);
            absentButton.setSelected(false);
            
            // Set selection based on value
            if ("Present".equals(value)) {
                presentButton.setSelected(true);
            } else {
                absentButton.setSelected(true);
            }
            
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }
            
            return panel;
        }
    }
    
    // Custom editor for attendance status column
    private class AttendanceStatusEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JRadioButton presentButton;
        private JRadioButton absentButton;
        private String currentValue;
        private java.util.List<Student> studentList;
        private int currentRow;
        
        public AttendanceStatusEditor(java.util.List<Student> students) {
            this.studentList = students;
            panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            presentButton = new JRadioButton("Present");
            absentButton = new JRadioButton("Absent");
            
            ButtonGroup group = new ButtonGroup();
            group.add(presentButton);
            group.add(absentButton);
            
            panel.add(presentButton);
            panel.add(absentButton);
            
            // Set colors
            presentButton.setBackground(new Color(220, 255, 220)); // Light green
            absentButton.setBackground(new Color(255, 220, 220));  // Light red
            
            // Add action listeners
            presentButton.addActionListener(e -> {
                currentValue = "Present";
                // Update attendance map when the radio button is clicked
                if (currentRow >= 0 && currentRow < studentList.size()) {
                    Student student = studentList.get(currentRow);
                    attendanceMap.put(student.getId(), true);
                    System.out.println("Set attendance for student ID " + student.getId() + " to PRESENT");
                    
                    // Update the table model to reflect the change
                    tableModel.setValueAt("Present", currentRow, 2);
                }
                stopCellEditing();
            });
            
            absentButton.addActionListener(e -> {
                currentValue = "Absent";
                // Update attendance map when the radio button is clicked
                if (currentRow >= 0 && currentRow < studentList.size()) {
                    Student student = studentList.get(currentRow);
                    attendanceMap.put(student.getId(), false);
                    System.out.println("Set attendance for student ID " + student.getId() + " to ABSENT");
                    
                    // Update the table model to reflect the change
                    tableModel.setValueAt("Absent", currentRow, 2);
                }
                stopCellEditing();
            });
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            // Save the current row for use in action listeners
            currentRow = row;
            
            // Set initial selection based on current value
            currentValue = (String) value;
            presentButton.setSelected("Present".equals(currentValue));
            absentButton.setSelected("Absent".equals(currentValue));
            
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return currentValue;
        }
    }
    
    private void saveAttendance() {
        if (subjectComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a subject", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Subject selectedSubject = (Subject) subjectComboBox.getSelectedItem();
        Date selectedDate = getSelectedDate();
        
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid date", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (attendanceMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No attendance data to save", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Debug - print attendance map values
        System.out.println("===== ATTENDANCE MAP BEFORE SAVING =====");
        for (Map.Entry<Integer, Boolean> entry : attendanceMap.entrySet()) {
            System.out.println("Student ID: " + entry.getKey() + ", Present: " + entry.getValue());
        }
        System.out.println("========================================");
        
        // Reset database connection before saving
        try {
            com.attendance.util.DBConnection.closeConnection();
            com.attendance.util.DBConnection.getConnection();
        } catch (Exception e) {
            System.err.println("Error resetting connection before save: " + e.getMessage());
        }
        
        // Save attendance for each student
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        boolean success = true;
        StringBuilder errorMessages = new StringBuilder();
        int savedCount = 0;
        int errorCount = 0;
        
        for (Map.Entry<Integer, Boolean> entry : attendanceMap.entrySet()) {
            int studentId = entry.getKey();
            boolean isPresent = entry.getValue();
            
            try {
                boolean result = attendanceDAO.markAttendance(studentId, selectedSubject.getId(), selectedDate, isPresent);
                if (result) {
                    savedCount++;
                } else {
                    success = false;
                    errorCount++;
                    errorMessages.append("Failed to save attendance for student ID ").append(studentId).append("\n");
                }
            } catch (Exception e) {
                success = false;
                errorCount++;
                System.err.println("Error saving attendance: " + e.getMessage());
                e.printStackTrace();
                errorMessages.append("Error for student ID ").append(studentId).append(": ").append(e.getMessage()).append("\n");
            }
        }
        
        if (success) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            JOptionPane.showMessageDialog(this, 
                "Attendance saved successfully for " + dateFormat.format(selectedDate) + 
                "\nSaved " + savedCount + " records.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Clear the table after saving
            attendanceTablePanel.removeAll();
            studentTable = null;
            attendanceMap.clear();
            markAttendanceButton.setEnabled(false);
            attendanceTablePanel.revalidate();
            attendanceTablePanel.repaint();
        } else {
            StringBuilder message = new StringBuilder();
            message.append("Error saving attendance\n");
            message.append("Saved: ").append(savedCount).append(" records\n");
            message.append("Failed: ").append(errorCount).append(" records\n\n");
            
            if (errorMessages.length() > 0) {
                message.append("Details:\n").append(errorMessages.toString());
            }
            
            // Display error in scrollable text area for better readability
            JTextArea textArea = new JTextArea(message.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 200));
            
            JOptionPane.showMessageDialog(this, scrollPane, "Error Saving Attendance", JOptionPane.ERROR_MESSAGE);
            
            // Try to reload database connection
            resetDatabaseConnection();
            
            // If some records were saved, refresh the table
            if (savedCount > 0) {
                loadStudentsForSubject();
            }
        }
    }
    
    private void debugDatabase() {
        // This method will check the database connections and print debug information
        try {
            StringBuilder debug = new StringBuilder();
            
            // Check faculty ID
            debug.append("Current faculty ID: ").append(facultyId).append("\n");
            
            // Check subjects
            SubjectDAO subjectDAO = new SubjectDAO();
            java.util.List<Subject> subjects = subjectDAO.getSubjectsByFacultyId(facultyId);
            debug.append("Found ").append(subjects.size()).append(" subjects for faculty\n");
            
            for (Subject subject : subjects) {
                debug.append(" - Subject: ").append(subject.getName())
                     .append(" (ID: ").append(subject.getId()).append(")\n");
                
                // Check students for this subject
                StudentDAO studentDAO = new StudentDAO();
                java.util.List<Student> students = studentDAO.getStudentsBySubject(subject.getId());
                debug.append("   - Found ").append(students.size()).append(" students\n");
                
                for (Student student : students) {
                    debug.append("     - ").append(student.getName())
                         .append(" (ID: ").append(student.getId())
                         .append(", Roll No: ").append(student.getRollNo()).append(")\n");
                }
                
                // Check student_subjects table directly via SQL
                try {
                    java.sql.Connection conn = com.attendance.util.DBConnection.getConnection();
                    java.sql.PreparedStatement stmt = conn.prepareStatement(
                        "SELECT COUNT(*) FROM student_subjects WHERE subject_id = ?");
                    stmt.setInt(1, subject.getId());
                    java.sql.ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        debug.append("   - Direct SQL query found ").append(rs.getInt(1))
                             .append(" enrollments in student_subjects table\n");
                    }
                    rs.close();
                    stmt.close();
                } catch (Exception e) {
                    debug.append("   - Error checking student_subjects: ").append(e.getMessage()).append("\n");
                }
            }
            
            // Display results
            JTextArea textArea = new JTextArea(debug.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));
            
            JOptionPane.showMessageDialog(this, scrollPane, "Database Debug Information", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Debug Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
} 