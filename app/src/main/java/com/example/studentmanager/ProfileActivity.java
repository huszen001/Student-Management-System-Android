package com.example.studentmanager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentmanager.db.DatabaseHelper;
import com.example.studentmanager.utils.SPUtils;

public class ProfileActivity extends AppCompatActivity {
    // 原来 + 新增年龄、性别
    private TextView tvName, tvStudentId, tvClass, tvEmail, tvPhone, tvAge, tvGender;
    private Button btnEditProfile;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupToolbar();
        loadUserInfo();
        setupListeners();
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_name);
        tvStudentId = findViewById(R.id.tv_student_id);
        tvClass = findViewById(R.id.tv_class);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);

        // 绑定年龄、性别
        tvAge = findViewById(R.id.tv_age);
        tvGender = findViewById(R.id.tv_gender);

        btnEditProfile = findViewById(R.id.btn_edit_profile);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("个人信息");
        }
    }

    private void loadUserInfo() {
        int userId = SPUtils.getUserId(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            // 查询字段加入年龄、性别
            String[] columns = {
                    DatabaseHelper.COLUMN_USER_NAME,
                    DatabaseHelper.COLUMN_USER_STUDENT_ID,
                    DatabaseHelper.COLUMN_USER_CLASS,
                    DatabaseHelper.COLUMN_USER_EMAIL,
                    DatabaseHelper.COLUMN_USER_PHONE,
                    DatabaseHelper.COLUMN_USER_AGE,
                    DatabaseHelper.COLUMN_USER_GENDER
            };
            String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};

            cursor = db.query(DatabaseHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                tvName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME)));
                tvStudentId.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_STUDENT_ID)));
                tvClass.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_CLASS)));
                tvEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)));
                tvPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PHONE)));

                // 显示年龄、性别
                tvAge.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_AGE)));
                tvGender.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_GENDER)));
            }
        } catch (Exception e) {
            Toast.makeText(this, "加载用户信息失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
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