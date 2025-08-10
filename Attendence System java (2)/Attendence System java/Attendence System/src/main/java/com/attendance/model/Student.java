package com.attendance.model;

public class Student {
    private int id;
    private String name;
    private String rollNo;
    private String course;
    private int userId;
    
    public Student() {
    }
    
    public Student(int id, String name, String rollNo, String course, int userId) {
        this.id = id;
        this.name = name;
        this.rollNo = rollNo;
        this.course = course;
        this.userId = userId;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getRollNo() {
        return rollNo;
    }
    
    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }
    
    public String getCourse() {
        return course;
    }
    
    public void setCourse(String course) {
        this.course = course;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
} 