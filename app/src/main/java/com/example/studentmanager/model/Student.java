package com.example.studentmanager.model;

public class Student {
    private int id;
    private String name;
    private String studentId;
    private String className;
    private String email;
    private String phone;

    public Student(int id, String name, String studentId, String className, String email, String phone) {
        this.id = id;
        this.name = name;
        this.studentId = studentId;
        this.className = className;
        this.email = email;
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getClassName() {
        return className;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
} 