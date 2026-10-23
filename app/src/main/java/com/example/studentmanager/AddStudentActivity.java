package com.example.studentmanager;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentmanager.db.DatabaseHelper;

public class AddStudentActivity extends AppCompatActivity {
    // 原来的
    private EditText etName, etStudentId, etClass, etEmail, etPhone, etPassword;
    // 新增：年龄 + 性别
    private EditText etAge, etGender;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("请添加学生");

        dbHelper = new DatabaseHelper(this);
        initViews();
    }


    private String parseClassFromStudentId(String studentId) {
        if (studentId == null || studentId.length() != 8) {
            return "";
        }
        // 第3-4位：入学年份（如23），第5-6位：班级号（如15）
        String yearPart = studentId.substring(2, 4);
        String classPart = studentId.substring(4, 6);
        return yearPart + "级" + classPart + "班";
    }
    private void initViews() {
        etName = findViewById(R.id.et_name);
        etStudentId = findViewById(R.id.et_student_id);
        etClass = findViewById(R.id.et_class);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);

        // 绑定新增的年龄、性别输入框
        etAge = findViewById(R.id.et_age);
        etGender = findViewById(R.id.et_gender);

        findViewById(R.id.btn_save).setOnClickListener(v -> saveStudent());

        // 监听学号输入，自动填班级
        etStudentId.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String studentId = s.toString().trim();
                String className = parseClassFromStudentId(studentId);
                etClass.setText(className);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        findViewById(R.id.btn_save).setOnClickListener(v -> saveStudent());
    }

    private void saveStudent() {
        String name = etName.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim();
        String className = etClass.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 获取年龄、性别
        String age = etAge.getText().toString().trim();
        String gender = etGender.getText().toString().trim();

        if (name.isEmpty() || studentId.isEmpty() || className.isEmpty() ||
                email.isEmpty() || phone.isEmpty() || password.isEmpty() ||
                age.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_NAME, name);
        values.put(DatabaseHelper.COLUMN_USER_STUDENT_ID, studentId);
        values.put(DatabaseHelper.COLUMN_USER_CLASS, className);
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_USER_PHONE, phone);
        values.put(DatabaseHelper.COLUMN_USER_PASSWORD, password);
        values.put(DatabaseHelper.COLUMN_USER_TYPE, 2); // 2 表示学生

        // 保存年龄、性别
        values.put(DatabaseHelper.COLUMN_USER_AGE, age);
        values.put(DatabaseHelper.COLUMN_USER_GENDER, gender);

        long result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        if (result != -1) {
            Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}