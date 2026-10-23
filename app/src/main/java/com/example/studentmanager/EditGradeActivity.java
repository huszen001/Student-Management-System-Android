package com.example.studentmanager;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentmanager.db.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditGradeActivity extends AppCompatActivity {
    private TextInputLayout tilStudent, tilCourse, tilScore;
    private TextInputEditText etStudent, etCourse, etScore;
    private MaterialButton btnSave;
    private DatabaseHelper dbHelper;
    private int gradeId;
    private int studentId;
    private int courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_grade);

        gradeId = getIntent().getIntExtra("grade_id", -1);
        if (gradeId == -1) {
            Toast.makeText(this, "成绩信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupToolbar();
        loadGrade();
        setupListeners();
    }

    private void initViews() {
        tilStudent = findViewById(R.id.til_student);
        tilCourse = findViewById(R.id.til_course);
        tilScore = findViewById(R.id.til_score);
        etStudent = findViewById(R.id.et_student);
        etCourse = findViewById(R.id.et_course);
        etScore = findViewById(R.id.et_score);
        btnSave = findViewById(R.id.btn_save);

        // 禁用学生和课程选择
        etStudent.setEnabled(false);
        etCourse.setEnabled(false);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("编辑成绩");
        }
    }

    private void loadGrade() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT g.*, u." + DatabaseHelper.COLUMN_USER_NAME + " as student_name, " +
                "c." + DatabaseHelper.COLUMN_COURSE_NAME + " as course_name " +
                "FROM " + DatabaseHelper.TABLE_GRADES + " g " +
                "JOIN " + DatabaseHelper.TABLE_USERS + " u ON g." + DatabaseHelper.COLUMN_GRADE_STUDENT_ID + " = u." + DatabaseHelper.COLUMN_USER_ID + " " +
                "JOIN " + DatabaseHelper.TABLE_COURSES + " c ON g." + DatabaseHelper.COLUMN_GRADE_COURSE_ID + " = c." + DatabaseHelper.COLUMN_COURSE_ID + " " +
                "WHERE g." + DatabaseHelper.COLUMN_GRADE_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(gradeId)});
        if (cursor.moveToFirst()) {
            studentId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GRADE_STUDENT_ID));
            courseId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GRADE_COURSE_ID));
            String studentName = cursor.getString(cursor.getColumnIndexOrThrow("student_name"));
            String courseName = cursor.getString(cursor.getColumnIndexOrThrow("course_name"));
            double score = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GRADE_SCORE));

            etStudent.setText(studentName);
            etCourse.setText(courseName);
            etScore.setText(String.valueOf(score));
        }
        cursor.close();
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveGrade());
    }

    private void saveGrade() {
        String scoreStr = etScore.getText().toString().trim();
        if (scoreStr.isEmpty()) {
            Toast.makeText(this, "请输入成绩", Toast.LENGTH_SHORT).show();
            return;
        }

        double score;
        try {
            score = Double.parseDouble(scoreStr);
            if (score < 0 || score > 100) {
                Toast.makeText(this, "成绩必须在0-100之间", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的成绩", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_GRADE_SCORE, score);

        try {
            int result = db.update(DatabaseHelper.TABLE_GRADES, values,
                    DatabaseHelper.COLUMN_GRADE_ID + " = ?",
                    new String[]{String.valueOf(gradeId)});

            if (result > 0) {
                Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "修改失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "修改失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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