package com.example.studentmanager.model;

public class Grade {
    private int id;
    private int studentId;
    private String studentName;
    private int courseId;
    private String courseName;
    private double score;
    private long gradeTime;

    public Grade(int id, int studentId, String studentName, int courseId, String courseName, double score, long gradeTime) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseId = courseId;
        this.courseName = courseName;
        this.score = score;
        this.gradeTime = gradeTime;
    }

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

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public long getGradeTime() {
        return gradeTime;
    }

    public void setGradeTime(long gradeTime) {
        this.gradeTime = gradeTime;
    }
} 