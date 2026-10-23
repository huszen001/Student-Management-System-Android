package com.example.studentmanager;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanager.adapter.CourseApplicationAdapter;
import com.example.studentmanager.db.DatabaseHelper;
import com.example.studentmanager.model.CourseApplication;

import java.util.ArrayList;
import java.util.List;

public class CourseApprovalsActivity extends AppCompatActivity implements CourseApplicationAdapter.OnApplicationActionListener {
    private RecyclerView recyclerView;
    private CourseApplicationAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<CourseApplication> applications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_approvals);



        dbHelper = new DatabaseHelper(this);
        applications = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourseApplicationAdapter(applications, this);
        recyclerView.setAdapter(adapter);

        loadApplications();
    }

    private void loadApplications() {
        applications.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT a.*, u." + DatabaseHelper.COLUMN_USER_NAME + " as student_name, " +
                "c." + DatabaseHelper.COLUMN_COURSE_NAME + " as course_name " +
                "FROM " + DatabaseHelper.TABLE_COURSE_APPLICATIONS + " a " +
                "JOIN " + DatabaseHelper.TABLE_USERS + " u ON a." + DatabaseHelper.COLUMN_APPLICATION_STUDENT_ID + " = u." + DatabaseHelper.COLUMN_USER_ID + " " +
                "JOIN " + DatabaseHelper.TABLE_COURSES + " c ON a." + DatabaseHelper.COLUMN_APPLICATION_COURSE_ID + " = c." + DatabaseHelper.COLUMN_COURSE_ID + " " +
                "WHERE a." + DatabaseHelper.COLUMN_APPLICATION_STATUS + " = 'pending' " +
                "ORDER BY a." + DatabaseHelper.COLUMN_APPLICATION_TIME + " DESC";

        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_APPLICATION_ID));
            int studentId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_APPLICATION_STUDENT_ID));
            String studentName = cursor.getString(cursor.getColumnIndexOrThrow("student_name"));
            int courseId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_APPLICATION_COURSE_ID));
            String courseName = cursor.getString(cursor.getColumnIndexOrThrow("course_name"));
            String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_APPLICATION_STATUS));
            long applicationTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_APPLICATION_TIME));

            CourseApplication application = new CourseApplication(id, studentId, studentName, courseId, courseName, status, applicationTime);
            applications.add(application);
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onApprove(CourseApplication application) {
        approveApplication(application);
    }

    @Override
    public void onReject(CourseApplication application) {
        rejectApplication(application);
    }

    private void approveApplication(CourseApplication application) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // 更新申请状态
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_APPLICATION_STATUS, "approved");
            db.update(DatabaseHelper.TABLE_COURSE_APPLICATIONS, values,
                    DatabaseHelper.COLUMN_APPLICATION_ID + " = ?",
                    new String[]{String.valueOf(application.getId())});

            // 更新课程已选人数
            String updateCourse = "UPDATE " + DatabaseHelper.TABLE_COURSES +
                    " SET " + DatabaseHelper.COLUMN_COURSE_SELECTED + " = " +
                    DatabaseHelper.COLUMN_COURSE_SELECTED + " + 1 " +
                    "WHERE " + DatabaseHelper.COLUMN_COURSE_ID + " = ?";
            db.execSQL(updateCourse, new String[]{String.valueOf(application.getCourseId())});

            db.setTransactionSuccessful();
            Toast.makeText(this, "已批准申请", Toast.LENGTH_SHORT).show();
            loadApplications();
        } finally {
            db.endTransaction();
        }
    }

    private void rejectApplication(CourseApplication application) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_APPLICATION_STATUS, "rejected");
        db.update(DatabaseHelper.TABLE_COURSE_APPLICATIONS, values,
                DatabaseHelper.COLUMN_APPLICATION_ID + " = ?",
                new String[]{String.valueOf(application.getId())});
        Toast.makeText(this, "已拒绝申请", Toast.LENGTH_SHORT).show();
        loadApplications();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 