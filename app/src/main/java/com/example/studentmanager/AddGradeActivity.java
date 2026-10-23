package com.example.studentmanager;
import android.view.View;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentmanager.db.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class AddGradeActivity extends AppCompatActivity {

    private TextInputLayout tilStudent, tilCourse, tilScore;
    private TextInputEditText etStudent, etCourse, etScore;
    private MaterialButton btnSave;
    private DatabaseHelper dbHelper;

    private List<StudentScore> studentList = new ArrayList<>();
    private int selectedCourseId = -1;
    private LinearLayout dynamicLayout;

    static class StudentScore {
        int id;
        String name;
        EditText etScore;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_grade);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupToolbar();
        createDynamicLayout();
        setupLogic();
    }

    private void initViews() {
        tilStudent = findViewById(R.id.til_student);
        tilCourse = findViewById(R.id.til_course);
        tilScore = findViewById(R.id.til_score);
        etStudent = findViewById(R.id.et_student);
        etCourse = findViewById(R.id.et_course);
        etScore = findViewById(R.id.et_score);
        btnSave = findViewById(R.id.btn_save);
    }

    private void createDynamicLayout() {
        dynamicLayout = new LinearLayout(this);
        dynamicLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout parent = (LinearLayout) tilCourse.getParent();
        int index = parent.indexOfChild(tilCourse) + 1;
        parent.addView(dynamicLayout, index);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("批量录入成绩");
        }
    }

    private void setupLogic() {
        etCourse.setOnClickListener(v -> showCourseDialog());
        btnSave.setOnClickListener(v -> saveAllGrades());

        tilStudent.setVisibility(View.GONE);
        tilScore.setVisibility(View.GONE);
    }

    private void showCourseDialog() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_COURSES,
                new String[]{DatabaseHelper.COLUMN_COURSE_ID, DatabaseHelper.COLUMN_COURSE_NAME},
                null, null, null, null, DatabaseHelper.COLUMN_COURSE_NAME);

        List<String> courseNames = new ArrayList<>();
        List<Integer> courseIds = new ArrayList<>();

        while (cursor.moveToNext()) {
            courseIds.add(cursor.getInt(0));
            courseNames.add(cursor.getString(1));
        }
        cursor.close();

        new AlertDialog.Builder(this)
                .setTitle("选择课程")
                .setItems(courseNames.toArray(new String[0]), (dialog, which) -> {
                    selectedCourseId = courseIds.get(which);
                    etCourse.setText(courseNames.get(which));
                    loadAllStudents(selectedCourseId);
                })
                .show();
    }

    private void loadAllStudents(int courseId) {
        studentList.clear();
        dynamicLayout.removeAllViews();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 【终极正确】加载：选了课的 + 已有成绩的 → 全部显示
        String sql = "SELECT DISTINCT u.id, u.name " +
                "FROM users u " +
                "LEFT JOIN course_applications a ON u.id = a.student_id AND a.course_id = ? " +
                "LEFT JOIN grades g ON u.id = g.student_id AND g.course_id = ? " +
                "WHERE (a.status = 'approved' OR g.score IS NOT NULL) " +
                "ORDER BY u.name";

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(courseId), String.valueOf(courseId)});

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(4, 12, 4, 12);

            TextView tvName = new TextView(this);
            tvName.setText(name);
            tvName.setTextSize(16);
            LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, -2, 1f);
            tvName.setLayoutParams(p1);

            EditText et = new EditText(this);
            et.setHint("分数");
            et.setTextSize(16);
            et.setWidth(180);
            et.setGravity(Gravity.CENTER);

            // 自动加载已有成绩
            Cursor scoreCursor = db.query(DatabaseHelper.TABLE_GRADES,
                    new String[]{DatabaseHelper.COLUMN_GRADE_SCORE},
                    DatabaseHelper.COLUMN_GRADE_STUDENT_ID + "=? AND " + DatabaseHelper.COLUMN_GRADE_COURSE_ID + "=?",
                    new String[]{String.valueOf(id), String.valueOf(courseId)},
                    null, null, null);

            if (scoreCursor.moveToFirst()) {
                double oldScore = scoreCursor.getDouble(0);
                et.setText(String.valueOf(oldScore));
            }
            scoreCursor.close();

            row.addView(tvName);
            row.addView(et);
            dynamicLayout.addView(row);

            StudentScore item = new StudentScore();
            item.id = id;
            item.name = name;
            item.etScore = et;
            studentList.add(item);
        }
        cursor.close();
        Toast.makeText(this, "加载完成：" + studentList.size() + " 人", Toast.LENGTH_SHORT).show();
    }

    private void saveAllGrades() {
        if (selectedCourseId == -1) {
            Toast.makeText(this, "请先选课程", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;

        for (StudentScore s : studentList) {
            String str = s.etScore.getText().toString().trim();
            if (str.isEmpty()) continue;

            try {
                double score = Double.parseDouble(str);
                if (score < 0 || score > 100) continue;

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_GRADE_STUDENT_ID, s.id);
                values.put(DatabaseHelper.COLUMN_GRADE_COURSE_ID, selectedCourseId);
                values.put(DatabaseHelper.COLUMN_GRADE_SCORE, score);
                values.put(DatabaseHelper.COLUMN_GRADE_TIME, System.currentTimeMillis());

                String where = DatabaseHelper.COLUMN_GRADE_STUDENT_ID + "=? AND " +
                        DatabaseHelper.COLUMN_GRADE_COURSE_ID + "=?";
                String[] args = {String.valueOf(s.id), String.valueOf(selectedCourseId)};

                Cursor cursor = db.query(DatabaseHelper.TABLE_GRADES, null, where, args, null, null, null);
                boolean exists = cursor.moveToFirst();
                cursor.close();

                if (exists) {
                    db.update(DatabaseHelper.TABLE_GRADES, values, where, args);
                } else {
                    db.insert(DatabaseHelper.TABLE_GRADES, null, values);
                }
                count++;
            } catch (Exception e) {}
        }

        Toast.makeText(this, "保存成功：" + count + " 人", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}