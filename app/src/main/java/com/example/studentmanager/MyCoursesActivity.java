package com.example.studentmanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanager.adapter.CourseAdapter;
import com.example.studentmanager.db.DatabaseHelper;
import com.example.studentmanager.model.Course;
import com.example.studentmanager.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;

public class MyCoursesActivity extends AppCompatActivity {
    private RecyclerView rvCourses;
    private CourseAdapter adapter;
    private List<Course> courses = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_courses);

        dbHelper = new DatabaseHelper(this);

        setupToolbar();
        setupRecyclerView();
        loadMyCourses();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("我的课程");
        }
    }

    // ==============================================
    // 修复：只创建一个 adapter！！！
    // ==============================================
    private void setupRecyclerView() {
        rvCourses = findViewById(R.id.rv_courses);
        tvEmpty = findViewById(R.id.tv_empty);

        // 只创建一次！！！
        adapter = new CourseAdapter(courses, false);
        adapter.setShowApplyButton(false);  // 关闭申请
        adapter.setShowDropButton(true);    // 打开退选

        rvCourses.setLayoutManager(new LinearLayoutManager(this));
        rvCourses.setAdapter(adapter);
    }

    private void loadMyCourses() {
        courses.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int studentId = SPUtils.getUserId(this);

        String query = "SELECT c.* FROM " + DatabaseHelper.TABLE_COURSES + " c " +
                "INNER JOIN " + DatabaseHelper.TABLE_COURSE_APPLICATIONS + " a " +
                "ON c." + DatabaseHelper.COLUMN_COURSE_ID + " = a." + DatabaseHelper.COLUMN_APPLICATION_COURSE_ID + " " +
                "WHERE a." + DatabaseHelper.COLUMN_APPLICATION_STUDENT_ID + " = ? " +
                "AND a." + DatabaseHelper.COLUMN_APPLICATION_STATUS + " = 'approved'";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(studentId)});
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_NAME));
            String teacher = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_TEACHER));
            String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_TIME));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_LOCATION));
            int capacity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_CAPACITY));
            int selected = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_SELECTED));
            String courseCode = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_CODE)
            );

            int credit = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_CREDIT)
            );

            courses.add(new Course(
                    id,
                    courseCode,
                    name,
                    teacher,
                    time,
                    location,
                    capacity,
                    selected,
                    credit
            ));
        }

        if (courses.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvCourses.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvCourses.setVisibility(View.VISIBLE);
        }

        cursor.close();
        db.close();
        adapter.updateCourses(courses);
    }

    // ==============================================
    // 修复：退课逻辑（事务 + 防止负数 + 强制刷新）
    // ==============================================
    public void dropCourse(int courseId) {
        int studentId = SPUtils.getUserId(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();
        try {
            // 删除选课记录
            int rows = db.delete(
                    DatabaseHelper.TABLE_COURSE_APPLICATIONS,
                    DatabaseHelper.COLUMN_APPLICATION_STUDENT_ID + "=? AND " +
                            DatabaseHelper.COLUMN_APPLICATION_COURSE_ID + "=?",
                    new String[]{String.valueOf(studentId), String.valueOf(courseId)}
            );

            // 只有删除成功，才减人数，且不能 < 0
            if (rows > 0) {
                db.execSQL(
                        "UPDATE " + DatabaseHelper.TABLE_COURSES +
                                " SET " + DatabaseHelper.COLUMN_COURSE_SELECTED + " = " + DatabaseHelper.COLUMN_COURSE_SELECTED + " - 1" +
                                " WHERE " + DatabaseHelper.COLUMN_COURSE_ID + " = ? " +
                                " AND " + DatabaseHelper.COLUMN_COURSE_SELECTED + " > 0 ",
                        new Object[]{courseId}
                );
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }

        // 刷新
        loadMyCourses();
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
    protected void onResume() {
        super.onResume();
        loadMyCourses();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}