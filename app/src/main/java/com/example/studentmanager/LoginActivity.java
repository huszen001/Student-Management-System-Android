package com.example.studentmanager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanager.db.DatabaseHelper;
import com.example.studentmanager.utils.SPUtils;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private RadioGroup rgUserType;
    private Button btnLogin, btnRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 检查是否已登录
        if (SPUtils.isLoggedIn(this)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        initViews();
        initListeners();
        dbHelper = new DatabaseHelper(this);
    }

    // 通过ID绑定界面控件
    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        rgUserType = findViewById(R.id.rg_user_type);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
    }

    private void initListeners() {
        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        // 切换角色时，控制注册按钮显示/隐藏
        rgUserType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_student) {
                btnRegister.setVisibility(android.view.View.VISIBLE);
            } else {
                btnRegister.setVisibility(android.view.View.GONE);
            }
        });
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        int userType = rgUserType.getCheckedRadioButtonId() == R.id.rb_student ? 0 : 1;

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {DatabaseHelper.COLUMN_USER_ID};
        String selection = DatabaseHelper.COLUMN_USER_STUDENT_ID + " = ? AND " +
                DatabaseHelper.COLUMN_USER_PASSWORD + " = ? AND " +
                DatabaseHelper.COLUMN_USER_TYPE + " = ?";
        String[] selectionArgs = {username, password, String.valueOf(userType)};

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
            SPUtils.saveLoginState(this, true, userId, userType, username);
            Intent intent = new Intent(this, MainActivity.class);
// 把 userId 传过去！！！
            intent.putExtra("user_id", userId);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        db.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
} 