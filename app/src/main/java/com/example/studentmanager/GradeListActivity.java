package com.example.studentmanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Toast;
import android.os.Environment;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanager.adapter.GradeAdapter;
import com.example.studentmanager.db.DatabaseHelper;
import com.example.studentmanager.model.Grade;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GradeListActivity extends AppCompatActivity {
    private RecyclerView rvGrades;
    private TextInputLayout tilSearch;
    private TextInputEditText etSearch;
    private MaterialButton btnSearch, btnAddGrade,btnExport;
    private GradeAdapter adapter;
    private List<Grade> grades;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_management);

        dbHelper = new DatabaseHelper(this);
        grades = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadGrades();
    }

    private void initViews() {
        rvGrades = findViewById(R.id.rv_grades);
        tilSearch = findViewById(R.id.til_search);
        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);
        btnAddGrade = findViewById(R.id.btn_add_grade);
        btnExport=findViewById(R.id.btn_export);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("成绩管理");
        }
    }

    private void setupRecyclerView() {
        adapter = new GradeAdapter(grades);
        rvGrades.setLayoutManager(new LinearLayoutManager(this));
        rvGrades.setAdapter(adapter);

        adapter.setOnEditClickListener((grade, position) -> {
            Intent intent = new Intent(this, EditGradeActivity.class);
            intent.putExtra("grade_id", grade.getId());
            startActivity(intent);
        });

        adapter.setOnDeleteClickListener((grade, position) -> {
            new AlertDialog.Builder(this)
                    .setTitle("删除成绩")
                    .setMessage("确定要删除该成绩记录吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        deleteGrade(grade);
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    private void setupListeners() {
        // 搜索监听
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchGrades(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 搜索按钮监听
        btnSearch.setOnClickListener(v -> searchGrades(etSearch.getText().toString()));

        // 添加成绩按钮监听
        btnAddGrade.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddGradeActivity.class);
            startActivity(intent);
        });

        // 导出按钮（最终版，必弹Toast）
        btnExport.setOnClickListener(v -> {
            Toast.makeText(this, "正在导出成绩...", Toast.LENGTH_SHORT).show();
            exportGrades();
        });
    }
    // 最终版：存到真正的Downloads目录，模拟器文件管理器能直接看到
    private void exportGrades() {
        FileWriter writer = null;
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            // 真正的公共下载目录
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
            String fileName = "课程成绩_" + sdf.format(new Date()) + ".csv";
            File file = new File(downloadDir, fileName);
            writer = new FileWriter(file);

            // 写入表头（加BOM防止Excel中文乱码）
            writer.write("\uFEFF");
            writer.write("课程名称,学生姓名,成绩\n");

            // 查询数据
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT c.name, u.name, g.score FROM grades g " +
                            "JOIN users u ON g.student_id = u.id " +
                            "JOIN courses c ON g.course_id = c.id",
                    null
            );

            while (cursor.moveToNext()) {
                String courseName = cursor.getString(0);
                String studentName = cursor.getString(1);
                double score = cursor.getDouble(2);
                writer.write(String.format("%s,%s,%.1f\n", courseName, studentName, score));
            }

            runOnUiThread(() -> Toast.makeText(this,
                    "✅ 导出成功！\n文件路径：" + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show());

        } catch (Exception e) {
            final String errorMsg = "❌ 导出失败：" + e.getMessage();
            runOnUiThread(() -> Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show());
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) writer.close();
                if (cursor != null) cursor.close();
                if (db != null) db.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void loadGrades() {
        grades.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT g.*, u." + DatabaseHelper.COLUMN_USER_NAME + " as student_name, " +
                "c." + DatabaseHelper.COLUMN_COURSE_NAME + " as course_name " +
                "FROM " + DatabaseHelper.TABLE_GRADES + " g " +
                "JOIN " + DatabaseHelper.TABLE_USERS + " u ON g." + DatabaseHelper.COLUMN_GRADE_STUDENT_ID + " = u." + DatabaseHelper.COLUMN_USER_ID + " " +
                "JOIN " + DatabaseHelper.TABLE_COURSES + " c ON g." + DatabaseHelper.COLUMN_GRADE_COURSE_ID + " = c." + DatabaseHelper.COLUMN_COURSE_ID + " " +
                "ORDER BY g." + DatabaseHelper.COLUMN_GRADE_TIME + " DESC";

        try {
            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GRADE_ID));
                int studentId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GRADE_STUDENT_ID));
                String studentName = cursor.getString(cursor.getColumnIndexOrThrow("student_name"));
                int courseId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GRADE_COURSE_ID));
                String courseName = cursor.getString(cursor.getColumnIndexOrThrow("course_name"));
                double score = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GRADE_SCORE));
                long gradeTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GRADE_TIME));

                grades.add(new Grade(id, studentId, studentName, courseId, courseName, score, gradeTime));
            }
            cursor.close();
            adapter.updateGrades(grades);
        } catch (Exception e) {
            Toast.makeText(this, "加载成绩失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void searchGrades(String searchText) {
        if (searchText.isEmpty()) {
            loadGrades();
            return;
        }

        List<Grade> filteredList = new ArrayList<>();
        for (Grade grade : grades) {
            if (grade.getStudentName().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(grade);
            }
        }
        adapter.updateGrades(filteredList);
    }

    private void deleteGrade(Grade grade) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int result = db.delete(DatabaseHelper.TABLE_GRADES,
                    DatabaseHelper.COLUMN_GRADE_ID + " = ?",
                    new String[]{String.valueOf(grade.getId())});

            if (result > 0) {
                Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                loadGrades();
            } else {
                Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "删除失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGrades();
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