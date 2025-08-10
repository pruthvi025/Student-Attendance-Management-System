package com.attendance.model;

public class Subject {
    private int id;
    private String name;
    private String code;
    private int facultyId;
    private String semester;
    private String department;
    
    public Subject() {
    }
    
    public Subject(int id, String name, String code, int facultyId) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.facultyId = facultyId;
    }
    
    public Subject(int id, String name, String code, int facultyId, String semester, String department) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.facultyId = facultyId;
        this.semester = semester;
        this.department = department;
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
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public int getFacultyId() {
        return facultyId;
    }
    
    public void setFacultyId(int facultyId) {
        this.facultyId = facultyId;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
} 