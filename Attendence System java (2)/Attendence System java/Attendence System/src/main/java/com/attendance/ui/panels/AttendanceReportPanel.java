package com.attendance.ui.panels;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.dao.SubjectDAO;
import com.attendance.model.Student;
import com.attendance.model.Subject;
import com.attendance.model.Attendance;
import com.attendance.dao.FacultyDAO;
import com.attendance.model.Faculty;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AttendanceReportPanel extends JPanel {
    private int facultyId;
    private JComboBox<Subject> subjectComboBox;
    private JComboBox<String> reportTypeComboBox;
    private JPanel reportTablePanel;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JPanel dateDetailsPanel;
    private JTable dateDetailsTable;
    
    private static final String[] REPORT_TYPES = {"Daily", "Weekly", "Monthly", "All Time"};
    
    public AttendanceReportPanel(int facultyId) {
        this.facultyId = facultyId;
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Create title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Attendance Reports");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(titleLabel);
        
        // Create filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Subject selection
        JLabel subjectLabel = new JLabel("Select Subject:");
        subjectComboBox = new JComboBox<>();
        loadSubjects();
        
        // Report type selection
        JLabel reportTypeLabel = new JLabel("Report Type:");
        reportTypeComboBox = new JComboBox<>(REPORT_TYPES);
        
        // Generate report button
        JButton generateReportButton = new JButton("Generate Report");
        generateReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateReport();
            }
        });
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadSubjects();
                generateReport();
            }
        });
        
        filterPanel.add(subjectLabel);
        filterPanel.add(subjectComboBox);
        filterPanel.add(reportTypeLabel);
        filterPanel.add(reportTypeComboBox);
        filterPanel.add(generateReportButton);
        filterPanel.add(refreshButton);
        
        // Create report table panel
        reportTablePanel = new JPanel(new BorderLayout());
        reportTablePanel.setBorder(BorderFactory.createTitledBorder("Attendance Report"));
        
        // Create date details panel
        dateDetailsPanel = new JPanel(new BorderLayout());
        dateDetailsPanel.setBorder(BorderFactory.createTitledBorder("Date Details"));
        
        // Create main content panel with split between summary and details
        JSplitPane contentSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        contentSplitPane.setTopComponent(reportTablePanel);
        contentSplitPane.setBottomComponent(dateDetailsPanel);
        contentSplitPane.setResizeWeight(0.5); // Equal resize weight
        
        // Export button
        JButton exportButton = new JButton("Export to CSV");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportReportToCSV();
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(exportButton);
        
        // Add panels to main layout
        add(titlePanel, BorderLayout.NORTH);
        add(filterPanel, BorderLayout.NORTH, 1);
        add(contentSplitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
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
    
    private void generateReport() {
        if (subjectComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a subject", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Subject selectedSubject = (Subject) subjectComboBox.getSelectedItem();
        String reportType = (String) reportTypeComboBox.getSelectedItem();
        
        // Get date range based on report type
        Date startDate = null;
        Date endDate = new Date(); // Current date
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        
        switch (reportType) {
            case "Daily":
                startDate = endDate; // Same day
                break;
            case "Weekly":
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                startDate = calendar.getTime();
                break;
            case "Monthly":
                calendar.add(Calendar.MONTH, -1);
                startDate = calendar.getTime();
                break;
            case "All Time":
                // Set to oldest possible date for "All Time" option
                calendar.set(2000, 0, 1); // January 1, 2000 (reasonably old date)
                startDate = calendar.getTime();
                break;
        }
        
        // Fetch students for this subject
        StudentDAO studentDAO = new StudentDAO();
        java.util.List<Student> students = studentDAO.getStudentsBySubject(selectedSubject.getId());
        
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students enrolled in this subject", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Create report
        createAttendanceReport(students, selectedSubject, startDate, endDate);
    }
    
    private void createAttendanceReport(java.util.List<Student> students, Subject subject, Date startDate, Date endDate) {
        // Headers for the summary table
        String[] columns = {"Roll No", "Name", "Total Classes", "Present", "Absent", "Percentage", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        
        // Add data for each student
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        // Store all date information for detailed view
        final Map<Integer, Map<String, Boolean>> studentAttendanceMap = new HashMap<>();
        final Set<String> allDates = new TreeSet<>(); // Sorted set of all dates
        
        // First, find all dates on which attendance was recorded for this subject
        // This ensures we're considering all class sessions
        String query = "SELECT DISTINCT date FROM attendance WHERE subject_id = ? AND date BETWEEN ? AND ?";
        Set<String> classDates = new TreeSet<>();
        
        try {
            java.sql.Connection conn = com.attendance.util.DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, subject.getId());
            stmt.setDate(2, new java.sql.Date(startDate.getTime()));
            stmt.setDate(3, new java.sql.Date(endDate.getTime()));
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Date classDate = rs.getDate("date");
                String dateStr = dateFormat.format(classDate);
                classDates.add(dateStr);
            }
            
            rs.close();
            stmt.close();
            System.out.println("Found " + classDates.size() + " unique class dates for subject ID: " + subject.getId());
        } catch (SQLException e) {
            System.err.println("Error fetching class dates: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Store all dates in our set
        allDates.addAll(classDates);
        
        if (allDates.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No attendance records found for this subject in the selected date range.",
                "No Data", JOptionPane.INFORMATION_MESSAGE);
            
            // Add empty rows for each student to show they have 0 attendance
            for (Student student : students) {
                Object[] row = {
                    student.getRollNo(),
                    student.getName(),
                    "0",  // Total classes
                    "0",  // Present
                    "0",  // Absent 
                    "0.00%", // Percentage
                    "No Data"  // Status
                };
                tableModel.addRow(row);
                
                // Add empty attendance map for this student
                studentAttendanceMap.put(student.getId(), new HashMap<>());
            }
        } else {
            // Process each student's attendance
            for (Student student : students) {
                // Initialize counters
                int totalClasses = allDates.size(); // All unique class dates
                int presentCount = 0;
                
                // Fetch attendance records for this student and subject
                List<Attendance> attendanceList = 
                    attendanceDAO.getAttendanceByStudentAndSubject(student.getId(), subject.getId());
                
                // Create date map for this student - initialize all dates as absent by default
                Map<String, Boolean> dateAttendanceMap = new HashMap<>();
                for (String dateStr : allDates) {
                    dateAttendanceMap.put(dateStr, false); // Default to absent
                }
                
                // Update attendance status for dates where the student has records
                for (Attendance attendance : attendanceList) {
                    Date attendanceDate = attendance.getDate();
                    // Only consider dates within our range
                    if (!attendanceDate.before(startDate) && !attendanceDate.after(endDate)) {
                        String dateStr = dateFormat.format(attendanceDate);
                        if (attendance.isPresent()) {
                            presentCount++;
                            dateAttendanceMap.put(dateStr, true);
                        }
                    }
                }
                
                // Calculate absent count and percentage
                int absentCount = totalClasses - presentCount;
                double percentage = (totalClasses > 0) ? ((double) presentCount / totalClasses) * 100 : 0.0;
                
                String status = percentage >= 75 ? "Good Standing" : "Low Attendance";
                if (totalClasses == 0) {
                    status = "No Data";
                }
                
                Object[] row = {
                    student.getRollNo(),
                    student.getName(),
                    String.valueOf(totalClasses),
                    String.valueOf(presentCount),
                    String.valueOf(absentCount),
                    String.format("%.2f%%", percentage),
                    status
                };
                tableModel.addRow(row);
                
                // Store the attendance map for this student
                studentAttendanceMap.put(student.getId(), dateAttendanceMap);
            }
        }
        
        // Create or update summary table
        if (reportTable == null) {
            reportTable = new JTable(tableModel);
            
            // Set custom renderer for attendance percentage
            reportTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
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
            
            // Set custom renderer for status column
            reportTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    
                    String status = (String) value;
                    
                    if (status.equals("Low Attendance")) {
                        c.setForeground(Color.RED);
                    } else if (status.equals("Good Standing")) {
                        c.setForeground(new Color(0, 128, 0)); // Dark green
                    } else {
                        c.setForeground(Color.GRAY); // For "No Data"
                    }
                    
                    return c;
                }
            });
            
            // Make the table look better
            reportTable.setRowHeight(25);
            reportTable.setShowGrid(true);
            reportTable.setGridColor(Color.LIGHT_GRAY);
            reportTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
            
            // Add selection listener to show date details for selected student
            reportTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && reportTable.getSelectedRow() != -1) {
                    // Get selected student
                    int row = reportTable.getSelectedRow();
                    String rollNo = reportTable.getValueAt(row, 0).toString();
                    String name = reportTable.getValueAt(row, 1).toString();
                    
                    // Find student ID
                    int studentId = -1;
                    for (Student student : students) {
                        if (student.getRollNo().equals(rollNo)) {
                            studentId = student.getId();
                            break;
                        }
                    }
                    
                    if (studentId != -1) {
                        showDateDetails(studentId, name, studentAttendanceMap, allDates);
                    }
                }
            });
            
            JScrollPane scrollPane = new JScrollPane(reportTable);
            reportTablePanel.add(scrollPane, BorderLayout.CENTER);
        } else {
            reportTable.setModel(tableModel);
        }
        
        // Create date details table with all dates for all students
        createDateDetailsTable(students, studentAttendanceMap, allDates);
        
        // Set report title based on date range
        String dateRangeStr = dateFormat.format(startDate);
        if (!startDate.equals(endDate)) {
            dateRangeStr += " to " + dateFormat.format(endDate);
        }
        
        ((javax.swing.border.TitledBorder) reportTablePanel.getBorder()).setTitle(
            "Attendance Report: " + subject.getName() + " (" + dateRangeStr + ")");
        
        reportTablePanel.revalidate();
        reportTablePanel.repaint();
    }
    
    private void createDateDetailsTable(List<Student> students, Map<Integer, Map<String, Boolean>> studentAttendanceMap, Set<String> allDates) {
        // Create headers: Roll No, Name, and one column for each date
        List<String> datesList = new ArrayList<>(allDates);
        String[] columns = new String[datesList.size() + 2];
        columns[0] = "Roll No";
        columns[1] = "Name";
        for (int i = 0; i < datesList.size(); i++) {
            columns[i + 2] = datesList.get(i);
        }
        
        DefaultTableModel dateTableModel = new DefaultTableModel(columns, 0);
        
        // Add data for each student
        for (Student student : students) {
            Object[] row = new Object[columns.length];
            row[0] = student.getRollNo();
            row[1] = student.getName();
            
            Map<String, Boolean> studentDates = studentAttendanceMap.get(student.getId());
            if (studentDates != null) {
                for (int i = 0; i < datesList.size(); i++) {
                    String date = datesList.get(i);
                    Boolean present = studentDates.get(date);
                    row[i + 2] = present != null ? (present ? "Present" : "Absent") : "-";
                }
            }
            
            dateTableModel.addRow(row);
        }
        
        // Create or update date details table
        if (dateDetailsTable == null) {
            dateDetailsTable = new JTable(dateTableModel);
            
            // Setup custom renderer for attendance values
            DefaultTableCellRenderer attendanceRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    
                    if (value instanceof String) {
                        String attendanceValue = (String) value;
                        if ("Present".equals(attendanceValue)) {
                            c.setForeground(new Color(0, 128, 0)); // Dark green
                        } else if ("Absent".equals(attendanceValue)) {
                            c.setForeground(Color.RED);
                        } else {
                            c.setForeground(Color.GRAY);
                        }
                    }
                    
                    return c;
                }
            };
            
            // Apply renderer to date columns
            for (int i = 2; i < dateDetailsTable.getColumnCount(); i++) {
                dateDetailsTable.getColumnModel().getColumn(i).setCellRenderer(attendanceRenderer);
            }
            
            // Make the table look better
            dateDetailsTable.setRowHeight(25);
            dateDetailsTable.setShowGrid(true);
            dateDetailsTable.setGridColor(Color.LIGHT_GRAY);
            dateDetailsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
            
            JScrollPane scrollPane = new JScrollPane(dateDetailsTable);
            dateDetailsPanel.add(scrollPane, BorderLayout.CENTER);
        } else {
            dateDetailsTable.setModel(dateTableModel);
        }
        
        dateDetailsPanel.revalidate();
        dateDetailsPanel.repaint();
    }
    
    private void showDateDetails(int studentId, String studentName, Map<Integer, Map<String, Boolean>> studentAttendanceMap, Set<String> allDates) {
        Map<String, Boolean> dateMap = studentAttendanceMap.get(studentId);
        
        if (dateMap != null) {
            // Highlight the corresponding row in the date details table
            for (int i = 0; i < dateDetailsTable.getRowCount(); i++) {
                String name = (String) dateDetailsTable.getValueAt(i, 1);
                if (name.equals(studentName)) {
                    dateDetailsTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }
    
    private void exportReportToCSV() {
        if (reportTable == null) {
            JOptionPane.showMessageDialog(this, "No report data to export", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Attendance Report");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new java.io.File("attendance_report.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                java.io.FileWriter csvWriter = new java.io.FileWriter(file);
                
                // Get faculty name
                FacultyDAO facultyDAO = new FacultyDAO();
                Faculty faculty = facultyDAO.getFacultyById(facultyId);
                String facultyName = (faculty != null) ? faculty.getName() : "Unknown";
                
                // Get subject name and information
                Subject selectedSubject = (Subject) subjectComboBox.getSelectedItem();
                String subjectName = selectedSubject.getName();
                String subjectCode = selectedSubject.getCode();
                String department = selectedSubject.getDepartment();
                
                // Get date range from the title
                String reportTitle = ((javax.swing.border.TitledBorder) reportTablePanel.getBorder()).getTitle();
                String dateRange = reportTitle.contains("(") ? 
                    reportTitle.substring(reportTitle.indexOf("(") + 1, reportTitle.indexOf(")")) : 
                    "All Time";
                
                // Write report header information
                csvWriter.append("Attendance Report\n");
                csvWriter.append("Faculty Name:," + facultyName + "\n");
                csvWriter.append("Department:," + department + "\n");
                csvWriter.append("Subject:," + subjectName + " (" + subjectCode + ")\n");
                csvWriter.append("Date Range:,'" + dateRange + "\n");  // Added ' prefix to ensure Excel treats it as text
                csvWriter.append("Report Type:," + reportTypeComboBox.getSelectedItem() + "\n");
                csvWriter.append("Generated On:,'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n\n");
                
                // Write data headers
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    csvWriter.append(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) {
                        csvWriter.append(",");
                    }
                }
                csvWriter.append("\n");
                
                // Write data
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        String value = tableModel.getValueAt(row, col).toString();
                        csvWriter.append(value.replace(",", ";"));  // Replace commas to avoid CSV issues
                        if (col < tableModel.getColumnCount() - 1) {
                            csvWriter.append(",");
                        }
                    }
                    csvWriter.append("\n");
                }
                
                // Add date details section if we have them
                if (dateDetailsTable != null && dateDetailsTable.getModel().getColumnCount() > 2) {
                    csvWriter.append("\n\nDetailed Attendance by Date\n");
                    
                    DefaultTableModel dateModel = (DefaultTableModel) dateDetailsTable.getModel();
                    
                    // Write date headers
                    for (int i = 0; i < dateModel.getColumnCount(); i++) {
                        String header = dateModel.getColumnName(i);
                        // Prefix dates with ' to ensure they're treated as text in spreadsheet applications
                        if (i >= 2) {
                            // Try to parse as date and reformat to prevent ##### display in Excel
                            try {
                                // Format: If it's a valid date, add text prefix
                                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                                Date date = parser.parse(header);
                                header = "'" + formatter.format(date);
                            } catch (Exception e) {
                                // Not a date or wrong format, just add text prefix
                                header = "'" + header;
                            }
                        }
                        csvWriter.append(header);
                        if (i < dateModel.getColumnCount() - 1) {
                            csvWriter.append(",");
                        }
                    }
                    csvWriter.append("\n");
                    
                    // Write date details for each student
                    for (int row = 0; row < dateModel.getRowCount(); row++) {
                        for (int col = 0; col < dateModel.getColumnCount(); col++) {
                            String value = dateModel.getValueAt(row, col) != null ? 
                                           dateModel.getValueAt(row, col).toString() : "-";
                            csvWriter.append(value.replace(",", ";"));
                            if (col < dateModel.getColumnCount() - 1) {
                                csvWriter.append(",");
                            }
                        }
                        csvWriter.append("\n");
                    }
                }
                
                csvWriter.flush();
                csvWriter.close();
                
                JOptionPane.showMessageDialog(this, "Report exported successfully to:\n" + file.getAbsolutePath(), 
                                             "Export Success", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exporting report: " + e.getMessage(), 
                                             "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 