package com.example.studentmanager.model;

public class Course {
    private int id;
    private String name;
    private String teacher;
    private String time;
    private String location;
    private int capacity;
    private int selected;
    private String courseCode;  // 课程号
    private int credit;        // 学分
    // 新的构造方法
    public Course(int id, String courseCode, String name, String teacher,
                  String time, String location, int capacity, int selected, int credit) {
        this.id = id;
        this.courseCode = courseCode;
        this.name = name;
        this.teacher = teacher;
        this.time = time;
        this.location = location;
        this.capacity = capacity;
        this.selected = selected;
        this.credit = credit;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSelected() {
        return selected;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
    // getter
    public String getCourseCode() { return courseCode; }
    public int getCredit() { return credit; }

    // setter
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setCredit(int credit) { this.credit = credit; }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }
} 