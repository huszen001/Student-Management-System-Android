package com.example.studentmanager;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentmanager.db.DatabaseHelper;
import com.google.android.material.textfield.TextInputLayout;

public class AddCourseActivity extends AppCompatActivity {
    private TextInputLayout tilCode, tilName, tilTeacher, tilTime, tilLocation, tilCapacity, tilCredit;
    private Button btnSave;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupToolbar();
    }

    private void initViews() {
        // 新增：课程号、学分
        tilCode = findViewById(R.id.til_code);
        tilName = findViewById(R.id.til_name);
        tilTeacher = findViewById(R.id.til_teacher);
        tilTime = findViewById(R.id.til_time);
        tilLocation = findViewById(R.id.til_location);
        tilCapacity = findViewById(R.id.til_capacity);
        tilCredit = findViewById(R.id.til_credit);

        btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> saveCourse());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("添加课程");
        }
    }

    private void saveCourse() {
        // 读取新增字段
        String code = tilCode.getEditText().getText().toString().trim();
        String name = tilName.getEditText().getText().toString().trim();
        String teacher = tilTeacher.getEditText().getText().toString().trim();
        String time = tilTime.getEditText().getText().toString().trim();
        String location = tilLocation.getEditText().getText().toString().trim();
        String capacityStr = tilCapacity.getEditText().getText().toString().trim();
        String creditStr = tilCredit.getEditText().getText().toString().trim();

        if (code.isEmpty() || name.isEmpty() || teacher.isEmpty() || time.isEmpty() || location.isEmpty() || capacityStr.isEmpty() || creditStr.isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacity;
        int credit;
        try {
            capacity = Integer.parseInt(capacityStr);
            credit = Integer.parseInt(creditStr);
            if (capacity <= 0) {
                Toast.makeText(this, "课程容量必须大于0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "容量和学分必须是数字", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // 存入 课程号 + 学分
        values.put(DatabaseHelper.COLUMN_COURSE_CODE, code);
        values.put(DatabaseHelper.COLUMN_COURSE_NAME, name);
        values.put(DatabaseHelper.COLUMN_COURSE_TEACHER, teacher);
        values.put(DatabaseHelper.COLUMN_COURSE_TIME, time);
        values.put(DatabaseHelper.COLUMN_COURSE_LOCATION, location);
        values.put(DatabaseHelper.COLUMN_COURSE_CAPACITY, capacity);
        values.put(DatabaseHelper.COLUMN_COURSE_SELECTED, 0);
        values.put(DatabaseHelper.COLUMN_COURSE_CREDIT, credit);

        try {
            long result = db.insert(DatabaseHelper.TABLE_COURSES, null, values);
            if (result != -1) {
                Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "添加失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        db.close();
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