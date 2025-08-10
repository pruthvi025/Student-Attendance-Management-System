package com.attendance.model;

import java.util.Date;

public class Attendance {
    private int id;
    private int studentId;
    private int subjectId;
    private Date date;
    private boolean present;
    
    public Attendance() {
    }
    
    public Attendance(int id, int studentId, int subjectId, Date date, boolean present) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.date = date;
        this.present = present;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getStudentId() {
        return studentId;
    }
    
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
    
    public int getSubjectId() {
        return subjectId;
    }
    
    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public boolean isPresent() {
        return present;
    }
    
    public void setPresent(boolean present) {
        this.present = present;
    }
} 