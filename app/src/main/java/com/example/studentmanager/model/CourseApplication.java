package com.example.studentmanager.model;

public class CourseApplication {
    private int id;
    private int studentId;
    private String studentName;
    private int courseId;
    private String courseName;
    private String status;
    private long applicationTime;

    public CourseApplication(int id, int studentId, String studentName, int courseId, String courseName, String status, long applicationTime) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseId = courseId;
        this.courseName = courseName;
        this.status = status;
        this.applicationTime = applicationTime;
    }

    public int getId() {
        return id;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getStatus() {
        return status;
    }

    public long getApplicationTime() {
        return applicationTime;
    }

    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isApproved() {
        return "approved".equals(status);
    }

    public boolean isRejected() {
        return "rejected".equals(status);
    }
} 