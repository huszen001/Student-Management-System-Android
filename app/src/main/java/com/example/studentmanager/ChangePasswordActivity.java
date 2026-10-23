package com.example.studentmanager;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentmanager.db.DatabaseHelper;
import com.example.studentmanager.utils.SPUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ChangePasswordActivity extends AppCompatActivity {
    private TextInputLayout tilOldPassword, tilNewPassword, tilConfirmPassword;
    private TextInputEditText etOldPassword, etNewPassword, etConfirmPassword;
    private MaterialButton btnSave;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        dbHelper = new DatabaseHelper(this);
        userId = SPUtils.getUserId(this);
        initViews();
        setupToolbar();
        setupListeners();
    }

    private void initViews() {
        tilOldPassword = findViewById(R.id.til_old_password);
        tilNewPassword = findViewById(R.id.til_new_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("修改密码");
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String oldPwd = etOldPassword.getText().toString().trim();
        String newPwd = etNewPassword.getText().toString().trim();
        String confirmPwd = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(oldPwd) || TextUtils.isEmpty(newPwd) || TextUtils.isEmpty(confirmPwd)) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            Toast.makeText(this, "两次新密码输入不一致", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPwd.length() < 6) {
            Toast.makeText(this, "新密码长度不能少于6位", Toast.LENGTH_SHORT).show();
            return;
        }

        // 校验旧密码
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT password FROM users WHERE id=?";
        String[] args = {String.valueOf(userId)};
        String realOldPwd = null;
        try (android.database.Cursor cursor = db.rawQuery(sql, args)) {
            if (cursor.moveToFirst()) {
                realOldPwd = cursor.getString(0);
            }
        }
        if (realOldPwd == null || !realOldPwd.equals(oldPwd)) {
            Toast.makeText(this, "旧密码错误", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新密码
        SQLiteDatabase wdb = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPwd);
        int result = wdb.update("users", values, "id=?", new String[]{String.valueOf(userId)});
        if (result > 0) {
            Toast.makeText(this, "密码修改成功，请重新登录", Toast.LENGTH_SHORT).show();
            SPUtils.clearLoginState(this);
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        } else {
            Toast.makeText(this, "密码修改失败", Toast.LENGTH_SHORT).show();
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