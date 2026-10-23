package com.example.studentmanager;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanager.db.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername, etPassword, etConfirmPassword, etClass, etName, etEmail, etPhone, etAge;
    private RadioGroup rgUserType, rgGender; // 这里加了 rgGender
    private android.widget.Button btnRegister, btnBackToLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initListeners();
        dbHelper = new DatabaseHelper(this);
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etAge = findViewById(R.id.et_age);
        etClass = findViewById(R.id.et_class);

        rgUserType = findViewById(R.id.rg_user_type);
        rgGender = findViewById(R.id.rg_gender); // 绑定性别单选

        btnRegister = findViewById(R.id.btn_register);
        btnBackToLogin = findViewById(R.id.btn_back_to_login);

        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String studentId = s.toString().trim();
                int checkedId = rgUserType.getCheckedRadioButtonId();

                if (checkedId == R.id.rb_student) {
                    String className = parseClassFromStudentId(studentId);
                    etClass.setText(className);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initListeners() {
        btnRegister.setOnClickListener(v -> register());
        btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String className = etClass.getText().toString().trim();

        // 获取性别（男/女）
        String gender = rgGender.getCheckedRadioButtonId() == R.id.rb_male ? "男" : "女";

        int userType = rgUserType.getCheckedRadioButtonId() == R.id.rb_student ? 0 : 1;

        // 性别已经默认选中，无需判断空
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                name.isEmpty() || email.isEmpty() || phone.isEmpty() || age.isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_USER_STUDENT_ID},
                DatabaseHelper.COLUMN_USER_STUDENT_ID + " = ?",
                new String[]{username},
                null, null, null);

        if (cursor.moveToFirst()) {
            Toast.makeText(this, "该学号已被注册", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_STUDENT_ID, username);
        values.put(DatabaseHelper.COLUMN_USER_PASSWORD, password);
        values.put(DatabaseHelper.COLUMN_USER_TYPE, userType);
        values.put(DatabaseHelper.COLUMN_USER_NAME, name);
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_USER_PHONE, phone);
        values.put(DatabaseHelper.COLUMN_USER_CLASS, className);
        values.put(DatabaseHelper.COLUMN_USER_AGE, age);
        values.put(DatabaseHelper.COLUMN_USER_GENDER, gender); // 数据库还是存字符串，不变

        long result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        if (result != -1) {
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "注册失败", Toast.LENGTH_SHORT).show();
        }
    }

    private String parseClassFromStudentId(String studentId) {
        if (studentId == null || studentId.length() != 8) {
            return "";
        }
        String yearPart = studentId.substring(2, 4);
        String classPart = studentId.substring(4, 6);
        return yearPart + "级" + classPart + "班";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}