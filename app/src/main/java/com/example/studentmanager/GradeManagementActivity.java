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
import java.util.ArrayList;
import java.util.List;

public class GradeManagementActivity extends AppCompatActivity {
    private RecyclerView rvGrades;
    private TextInputLayout tilSearch;
    private TextInputEditText etSearch;
    private MaterialButton btnSearch, btnAddGrade, btnExport;
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
        btnExport = findViewById(R.id.btn_export);
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
                    .setMessage("确定删除？")
                    .setPositiveButton("确定", (d, w) -> deleteGrade(grade))
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    private void setupListeners() {
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

        btnSearch.setOnClickListener(v -> searchGrades(etSearch.getText().toString()));
        btnAddGrade.setOnClickListener(v -> startActivity(new Intent(this, AddGradeActivity.class)));

        // 导出按钮（最终版，必弹Toast）
        btnExport.setOnClickListener(v -> {
            Toast.makeText(this, "正在导出成绩...", Toast.LENGTH_SHORT).show();
            exportGrades();
        });
    }

    // 极简导出，无异常，必弹结果
    private void exportGrades() {
        FileWriter writer = null;
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            // 1. 创建文件（应用私有目录，无需权限）
            File dir = getFilesDir();
            File file = new File(dir, "成绩导出.csv");
            writer = new FileWriter(file);

            // 2. 写入表头
            writer.write("课程名称,学生姓名,成绩\n");

            // 3. 查询所有成绩
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT c.name, u.name, g.score FROM grades g " +
                            "JOIN users u ON g.student_id = u.id " +
                            "JOIN courses c ON g.course_id = c.id",
                    null
            );

            // 4. 写入数据
            while (cursor.moveToNext()) {
                String courseName = cursor.getString(0);
                String studentName = cursor.getString(1);
                double score = cursor.getDouble(2);
                writer.write(String.format("%s,%s,%.1f\n", courseName, studentName, score));
            }

            // 5. 导出成功，提示
            runOnUiThread(() -> Toast.makeText(this, "✅ 导出成功！\n文件路径：" + file.getAbsolutePath(), Toast.LENGTH_LONG).show());

        } catch (Exception e) {
            // 任何异常都提示，不会静默失败
            final String errorMsg = "❌ 导出失败：" + e.getMessage();
            runOnUiThread(() -> Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show());
            e.printStackTrace();
        } finally {
            // 关闭资源，防止内存泄漏
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
            Toast.makeText(this, "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void searchGrades(String searchText) {
        if (searchText.isEmpty()) {
            loadGrades();
            return;
        }
        List<Grade> filtered = new ArrayList<>();
        for (Grade g : grades) {
            if (g.getStudentName().toLowerCase().contains(searchText.toLowerCase())) {
                filtered.add(g);
            }
        }
        adapter.updateGrades(filtered);
    }

    private void deleteGrade(Grade grade) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete(DatabaseHelper.TABLE_GRADES,
                DatabaseHelper.COLUMN_GRADE_ID + "=?",
                new String[]{String.valueOf(grade.getId())});
        if (result > 0) {
            Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
            loadGrades();
        } else {
            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGrades();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}