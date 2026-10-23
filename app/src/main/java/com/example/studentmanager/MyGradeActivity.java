package com.example.studentmanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanager.adapter.StudentGradeAdapter;
import com.example.studentmanager.db.DatabaseHelper;
import com.example.studentmanager.model.Grade;
import com.example.studentmanager.utils.SPUtils;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyGradeActivity extends AppCompatActivity {
    private RecyclerView rvGrades;
    private TextView tvEmpty;
    private MaterialButton btnExportMyGrade; // 导出按钮
    private StudentGradeAdapter adapter;
    private List<Grade> grades = new ArrayList<>();
    private List<Grade> enrolledCourses = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private double calculateGPA(double score) {
        if (score >= 90) return 4.0;
        if (score >= 87) return 3.7;
        if (score >= 83) return 3.3;
        if (score >= 80) return 3.0;
        if (score >= 77) return 2.7;
        if (score >= 73) return 2.3;
        if (score >= 70) return 2.0;
        if (score >= 67) return 1.7;
        if (score >= 63) return 1.3;
        if (score >= 60) return 1.0;
        return 0.0;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_grade);

        dbHelper = new DatabaseHelper(this);

        setupToolbar();
        setupViews();
        loadMyGrades();

        // ========== 导出我的成绩 ==========
        btnExportMyGrade.setOnClickListener(v -> {
            exportMyGrades();
        });
        findViewById(R.id.btn_gpa).setOnClickListener(v -> showMyGPA());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("我的成绩");
        }
    }

    private void setupViews() {
        rvGrades = findViewById(R.id.rv_grades);
        tvEmpty = findViewById(R.id.tv_empty);
        btnExportMyGrade = findViewById(R.id.btn_export_my_grade); // 绑定按钮

        adapter = new StudentGradeAdapter(grades);
        rvGrades.setLayoutManager(new LinearLayoutManager(this));
        rvGrades.setAdapter(adapter);
    }
    private void showMyGPA() {
        double totalGpa = 0;
        int validCount = 0;

        // 只统计已有成绩（score != -1）
        for (Grade grade : grades) {
            double score = grade.getScore();
            // 未录入成绩的跳过
            if (score < 0) continue;

            totalGpa += calculateGPA(score);
            validCount++;
        }

        if (validCount == 0) {
            Toast.makeText(this, "暂无有效成绩，无法计算GPA", Toast.LENGTH_SHORT).show();
            return;
        }

        double finalGpa = totalGpa / validCount;

        new AlertDialog.Builder(this)
                .setTitle("我的学业绩点")
                .setMessage(
                        "已修课程：" + validCount + " 门\n" +
                                "平均 GPA：" + String.format("%.2f", finalGpa)
                )
                .setPositiveButton("确定", null)
                .show();
    }
    // ========== 【核心】导出当前登录学生的成绩 ==========
    private void exportMyGrades() {
        FileWriter writer = null;
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            // 真正的公共下载目录
            File downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
            String fileName = "我的成绩_" + sdf.format(new Date()) + ".csv";
            File file = new File(downloadDir, fileName);
            writer = new FileWriter(file);

            // 写入表头（加BOM防止Excel中文乱码）
            writer.write("\uFEFF");
            writer.write("课程名称,成绩\n");

            // 获取当前学生ID
            int studentId = com.example.studentmanager.utils.SPUtils.getUserId(this);

            // 查询数据（只查自己的成绩）
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT c.name, g.score FROM grades g " +
                            "JOIN courses c ON g.course_id = c.id " +
                            "WHERE g.student_id = ?",
                    new String[]{String.valueOf(studentId)}
            );

            while (cursor.moveToNext()) {
                String courseName = cursor.getString(0);
                double score = cursor.getDouble(1);
                writer.write(String.format("%s,%.1f\n", courseName, score));
            }

            runOnUiThread(() -> android.widget.Toast.makeText(this,
                    "✅ 导出成功！\n文件路径：" + file.getAbsolutePath(),
                    android.widget.Toast.LENGTH_LONG).show());

        } catch (Exception e) {
            final String errorMsg = "❌ 导出失败：" + e.getMessage();
            runOnUiThread(() -> android.widget.Toast.makeText(this, errorMsg, android.widget.Toast.LENGTH_LONG).show());
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) writer.close();
                if (cursor != null) cursor.close();
                if (db != null) db.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMyGrades() {
        grades.clear();
        enrolledCourses.clear();
        int studentId = SPUtils.getUserId(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 1. 获取学生已选课程（已批准的申请）
        String courseQuery = "SELECT c.* FROM " + DatabaseHelper.TABLE_COURSES + " c " +
                "INNER JOIN " + DatabaseHelper.TABLE_COURSE_APPLICATIONS + " a " +
                "ON c." + DatabaseHelper.COLUMN_COURSE_ID + " = a." + DatabaseHelper.COLUMN_APPLICATION_COURSE_ID + " " +
                "WHERE a." + DatabaseHelper.COLUMN_APPLICATION_STUDENT_ID + " = ? " +
                "AND a." + DatabaseHelper.COLUMN_APPLICATION_STATUS + " = 'approved'";

        Cursor courseCursor = db.rawQuery(courseQuery, new String[]{String.valueOf(studentId)});
        while (courseCursor.moveToNext()) {
            int courseId = courseCursor.getInt(courseCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_ID));
            String courseName = courseCursor.getString(courseCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_NAME));

            Grade grade = new Grade(-1, studentId, "", courseId, courseName, -1, 0);
            enrolledCourses.add(grade);
        }
        courseCursor.close();

        // 2. 获取学生已有的成绩记录
        String gradeQuery = "SELECT g.*, c." + DatabaseHelper.COLUMN_COURSE_NAME + " as course_name " +
                "FROM " + DatabaseHelper.TABLE_GRADES + " g " +
                "JOIN " + DatabaseHelper.TABLE_COURSES + " c ON g." + DatabaseHelper.COLUMN_GRADE_COURSE_ID + " = c." + DatabaseHelper.COLUMN_COURSE_ID + " " +
                "WHERE g." + DatabaseHelper.COLUMN_GRADE_STUDENT_ID + " = ? " +
                "ORDER BY g." + DatabaseHelper.COLUMN_GRADE_TIME + " DESC";

        Cursor gradeCursor = db.rawQuery(gradeQuery, new String[]{String.valueOf(studentId)});

        while (gradeCursor.moveToNext()) {
            int id = gradeCursor.getInt(gradeCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GRADE_ID));
            int courseId = gradeCursor.getInt(gradeCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GRADE_COURSE_ID));
            String courseName = gradeCursor.getString(gradeCursor.getColumnIndexOrThrow("course_name"));
            double score = gradeCursor.getDouble(gradeCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GRADE_SCORE));
            long gradeTime = gradeCursor.getLong(gradeCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GRADE_TIME));

            grades.add(new Grade(id, studentId, "", courseId, courseName, score, gradeTime));

            for (int i = 0; i < enrolledCourses.size(); i++) {
                if (enrolledCourses.get(i).getCourseId() == courseId) {
                    enrolledCourses.remove(i);
                    break;
                }
            }
        }
        gradeCursor.close();

        grades.addAll(enrolledCourses);
        adapter.updateGrades(grades);

        if (grades.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvGrades.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvGrades.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}