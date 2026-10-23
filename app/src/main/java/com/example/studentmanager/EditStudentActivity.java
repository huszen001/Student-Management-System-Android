package com.example.studentmanager;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentmanager.db.DatabaseHelper;

public class EditStudentActivity extends AppCompatActivity {
    // 原来 + 新增年龄、性别
    private EditText etName, etStudentId, etClass, etEmail, etPhone, etPassword, etAge, etGender;
    private DatabaseHelper dbHelper;
    private int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("编辑学生");

        studentId = getIntent().getIntExtra("student_id", -1);
        if (studentId == -1) {
            Toast.makeText(this, "学生信息不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        initViews();
        loadStudentInfo();
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        etStudentId = findViewById(R.id.et_student_id);
        etClass = findViewById(R.id.et_class);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);

        // 绑定年龄、性别
        etAge = findViewById(R.id.et_age);
        etGender = findViewById(R.id.et_gender);

        findViewById(R.id.btn_save).setOnClickListener(v -> updateStudent());
    }

    private void loadStudentInfo() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(studentId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME)));
            etStudentId.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_STUDENT_ID)));
            etClass.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_CLASS)));
            etEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)));
            etPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PHONE)));
            etPassword.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PASSWORD)));

            // 加载年龄、性别
            etAge.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_AGE)));
            etGender.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_GENDER)));
        }
        cursor.close();
    }

    private void updateStudent() {
        String name = etName.getText().toString().trim();
        String studentIdStr = etStudentId.getText().toString().trim();
        String className = etClass.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String gender = etGender.getText().toString().trim();

        if (name.isEmpty() || studentIdStr.isEmpty() || className.isEmpty() ||
                email.isEmpty() || phone.isEmpty() || password.isEmpty() ||
                age.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_NAME, name);
        values.put(DatabaseHelper.COLUMN_USER_STUDENT_ID, studentIdStr);
        values.put(DatabaseHelper.COLUMN_USER_CLASS, className);
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_USER_PHONE, phone);
        values.put(DatabaseHelper.COLUMN_USER_PASSWORD, password);
        values.put(DatabaseHelper.COLUMN_USER_AGE, age);       // 年龄
        values.put(DatabaseHelper.COLUMN_USER_GENDER, gender); // 性别

        int result = db.update(DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(studentId)});

        if (result > 0) {
            Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}