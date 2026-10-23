package com.example.studentmanager;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentmanager.db.DatabaseHelper;
import com.example.studentmanager.utils.SPUtils;

public class EditProfileActivity extends AppCompatActivity {
    // 声明所有控件，包括年龄和性别
    private EditText etName, etEmail, etPhone, etAge, etGender;
    private Button btnSave;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupToolbar();
        loadUserInfo();
        initListeners();
    }

    private void initViews() {
        // 绑定所有输入框
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etAge = findViewById(R.id.et_age);   // 绑定年龄
        etGender = findViewById(R.id.et_gender); // 绑定性别
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("编辑个人信息");
        }
    }

    private void loadUserInfo() {
        int userId = SPUtils.getUserId(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            // 查询字段包含年龄和性别
            String[] columns = {
                    DatabaseHelper.COLUMN_USER_NAME,
                    DatabaseHelper.COLUMN_USER_EMAIL,
                    DatabaseHelper.COLUMN_USER_PHONE,
                    DatabaseHelper.COLUMN_USER_AGE,
                    DatabaseHelper.COLUMN_USER_GENDER
            };
            String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};

            cursor = db.query(DatabaseHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                etName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME)));
                etEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)));
                etPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PHONE)));
                // 加载年龄和性别
                etAge.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_AGE)));
                etGender.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_GENDER)));
            }
        } catch (Exception e) {
            Toast.makeText(this, "加载用户信息失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void initListeners() {
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String gender = etGender.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || age.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "请填写所有信息", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = SPUtils.getUserId(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USER_NAME, name);
            values.put(DatabaseHelper.COLUMN_USER_EMAIL, email);
            values.put(DatabaseHelper.COLUMN_USER_PHONE, phone);
            values.put(DatabaseHelper.COLUMN_USER_AGE, age);
            values.put(DatabaseHelper.COLUMN_USER_GENDER, gender);

            String whereClause = DatabaseHelper.COLUMN_USER_ID + " = ?";
            String[] whereArgs = {String.valueOf(userId)};

            int result = db.update(DatabaseHelper.TABLE_USERS, values, whereClause, whereArgs);

            if (result > 0) {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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