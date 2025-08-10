package com.attendance.model;

public class Faculty {
    private int id;
    private String name;
    private String department;
    private int userId;
    
    public Faculty() {
    }
    
    public Faculty(int id, String name, String department, int userId) {
        this.id = id;
        this.name = name;
        this.department = department;
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
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
} 