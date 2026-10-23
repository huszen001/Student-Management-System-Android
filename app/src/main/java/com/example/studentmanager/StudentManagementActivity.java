package com.example.studentmanager;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanager.adapter.StudentAdapter;
import com.example.studentmanager.db.DatabaseHelper;
import com.example.studentmanager.model.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentManagementActivity extends AppCompatActivity implements StudentAdapter.OnStudentActionListener {
    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Student> students;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_management);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("学生管理");

        dbHelper = new DatabaseHelper(this);
        students = new ArrayList<>();

        recyclerView = findViewById(R.id.rv_students);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(students, this);
        recyclerView.setAdapter(adapter);

        etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchStudents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btn_add_student).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddStudentActivity.class);
            startActivityForResult(intent, 100); // 👈 改成这个
        });

        loadStudents();
    }

    private void loadStudents() {
        students.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS +
                " WHERE " + DatabaseHelper.COLUMN_USER_TYPE + " IN(0,2)" +
                " ORDER BY " + DatabaseHelper.COLUMN_USER_NAME;

        try (Cursor cursor = db.rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME));
                String studentId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_STUDENT_ID));
                String className = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_CLASS));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PHONE));

                students.add(new Student(id, name, studentId, className, email, phone));
            }
        }
        adapter.updateStudents(students);
    }

    private void searchStudents(String query) {
        if (query.isEmpty()) {
            loadStudents();
            return;
        }

        List<Student> filteredList = new ArrayList<>();
        for (Student student : students) {
            if (student.getName().contains(query) ||
                student.getStudentId().contains(query) ||
                student.getClassName().contains(query)) {
                filteredList.add(student);
            }
        }
        adapter.updateStudents(filteredList);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadStudents(); // 你原来的刷新列表方法
        }
    }
    @Override
    public void onEdit(Student student) {
        Intent intent = new Intent(this, EditStudentActivity.class);
        intent.putExtra("student_id", student.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Student student) {
        new AlertDialog.Builder(this)
                .setTitle("删除学生")
                .setMessage("确定要删除该学生吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete(DatabaseHelper.TABLE_USERS,
                            DatabaseHelper.COLUMN_USER_ID + " = ?",
                            new String[]{String.valueOf(student.getId())});
                    Toast.makeText(this, "已删除学生", Toast.LENGTH_SHORT).show();
                    loadStudents();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudents();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 