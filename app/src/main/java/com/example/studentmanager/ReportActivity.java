package com.example.studentmanager;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentmanager.db.DatabaseHelper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {
    private TextView tvTotalCourses, tvTotalStudents, tvAvgStudentsPerCourse;
    private TextView tvMostPopularCourse, tvLeastPopularCourse;
    private TextView tvFullCourses, tvAvailableCourses;
    private TextView tvAvgScore, tvMaxScore, tvMinScore, tvFailRate;

    private Spinner spCourse;
    private List<String> courseNameList = new ArrayList<>();
    private List<Integer> courseIdList = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupToolbar();
        loadCourseSpinner();
        loadReportData();
    }

    private void initViews() {
        tvTotalCourses = findViewById(R.id.tv_total_courses);
        tvTotalStudents = findViewById(R.id.tv_total_students);
        tvAvgStudentsPerCourse = findViewById(R.id.tv_avg_students);
        tvMostPopularCourse = findViewById(R.id.tv_most_popular);
        tvLeastPopularCourse = findViewById(R.id.tv_least_popular);
        tvFullCourses = findViewById(R.id.tv_full_courses);
        tvAvailableCourses = findViewById(R.id.tv_available_courses);

        tvAvgScore = findViewById(R.id.tv_avg_score);
        tvMaxScore = findViewById(R.id.tv_max_score);
        tvMinScore = findViewById(R.id.tv_min_score);
        tvFailRate = findViewById(R.id.tv_fail_rate);

        spCourse = findViewById(R.id.sp_course);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("数据统计与备份");
        }
    }

    // ========== 右上角菜单：备份 / 恢复 ==========
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_backup) {
            backupDatabase();
            return true;
        } else if (id == R.id.action_restore) {
            restoreDatabase();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ========== 加载课程下拉框 ==========
    private void loadCourseSpinner() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DatabaseHelper.COLUMN_COURSE_ID + "," + DatabaseHelper.COLUMN_COURSE_NAME +
                " FROM " + DatabaseHelper.TABLE_COURSES, null);

        courseNameList.clear();
        courseIdList.clear();
        courseNameList.add("请选择课程");
        courseIdList.add(-1);

        while (cursor.moveToNext()) {
            int cid = cursor.getInt(0);
            String name = cursor.getString(1);
            courseIdList.add(cid);
            courseNameList.add(name);
        }
        cursor.close();
        db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseNameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCourse.setAdapter(adapter);

        spCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int cid = courseIdList.get(position);
                if (cid == -1) {
                    tvAvgScore.setText("平均分：--");
                    tvMaxScore.setText("最高分：--");
                    tvMinScore.setText("最低分：--");
                    tvFailRate.setText("挂科率：--");
                    return;
                }
                loadCourseScoreStats(cid);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ========== 整体课程统计 ==========
    private void loadReportData() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();

            cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_COURSES, null);
            int totalCourses = cursor.moveToFirst() ? cursor.getInt(0) : 0;
            tvTotalCourses.setText("总课程数：" + totalCourses);
            cursor.close();

            cursor = db.rawQuery("SELECT IFNULL(SUM(" + DatabaseHelper.COLUMN_COURSE_SELECTED + "), 0) FROM " + DatabaseHelper.TABLE_COURSES, null);
            int totalSelected = cursor.moveToFirst() ? cursor.getInt(0) : 0;
            tvTotalStudents.setText("总选课人次：" + totalSelected);
            cursor.close();

            cursor = db.rawQuery("SELECT IFNULL(AVG(" + DatabaseHelper.COLUMN_COURSE_SELECTED + "), 0) FROM " + DatabaseHelper.TABLE_COURSES, null);
            double avg = cursor.moveToFirst() ? cursor.getDouble(0) : 0;
            tvAvgStudentsPerCourse.setText(String.format("平均选课人数：%.1f", avg));
            cursor.close();

            cursor = db.rawQuery("SELECT " + DatabaseHelper.COLUMN_COURSE_NAME + "," + DatabaseHelper.COLUMN_COURSE_SELECTED +
                    " FROM " + DatabaseHelper.TABLE_COURSES + " ORDER BY " + DatabaseHelper.COLUMN_COURSE_SELECTED + " DESC LIMIT 1", null);
            String most = "暂无";
            int mNum = 0;
            if (cursor.moveToFirst()) {
                most = cursor.getString(0);
                mNum = cursor.getInt(1);
            }
            tvMostPopularCourse.setText("最热门课程：" + most + "（" + mNum + "人）");
            cursor.close();

            cursor = db.rawQuery("SELECT " + DatabaseHelper.COLUMN_COURSE_NAME + "," + DatabaseHelper.COLUMN_COURSE_SELECTED +
                    " FROM " + DatabaseHelper.TABLE_COURSES + " ORDER BY " + DatabaseHelper.COLUMN_COURSE_SELECTED + " ASC LIMIT 1", null);
            String least = "暂无";
            int lNum = 0;
            if (cursor.moveToFirst()) {
                least = cursor.getString(0);
                lNum = cursor.getInt(1);
            }
            tvLeastPopularCourse.setText("最冷门课程：" + least + "（" + lNum + "人）");
            cursor.close();

            cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_COURSES +
                    " WHERE " + DatabaseHelper.COLUMN_COURSE_SELECTED + " >= " + DatabaseHelper.COLUMN_COURSE_CAPACITY, null);
            int full = cursor.moveToFirst() ? cursor.getInt(0) : 0;
            tvFullCourses.setText("已满课程：" + full);
            cursor.close();

            cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_COURSES +
                    " WHERE " + DatabaseHelper.COLUMN_COURSE_SELECTED + " < " + DatabaseHelper.COLUMN_COURSE_CAPACITY, null);
            int available = cursor.moveToFirst() ? cursor.getInt(0) : 0;
            tvAvailableCourses.setText("可报课程：" + available);
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    // ========== 单课程成绩统计 ==========
    private void loadCourseScoreStats(int courseId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String sql = "SELECT " +
                    "IFNULL(AVG(" + DatabaseHelper.COLUMN_GRADE_SCORE + "), 0), " +
                    "IFNULL(MAX(" + DatabaseHelper.COLUMN_GRADE_SCORE + "), 0), " +
                    "IFNULL(MIN(" + DatabaseHelper.COLUMN_GRADE_SCORE + "), 0), " +
                    "COUNT(CASE WHEN " + DatabaseHelper.COLUMN_GRADE_SCORE + " < 60 THEN 1 END), " +
                    "COUNT(*) " +
                    "FROM " + DatabaseHelper.TABLE_GRADES +
                    " WHERE " + DatabaseHelper.COLUMN_GRADE_COURSE_ID + " = ? " +
                    "AND " + DatabaseHelper.COLUMN_GRADE_SCORE + " >= 0";

            cursor = db.rawQuery(sql, new String[]{String.valueOf(courseId)});

            if (cursor.moveToFirst()) {
                double avg = cursor.getDouble(0);
                double max = cursor.getDouble(1);
                double min = cursor.getDouble(2);
                int fail = cursor.getInt(3);
                int total = cursor.getInt(4);

                tvAvgScore.setText(String.format("平均分：%.1f", avg));
                tvMaxScore.setText(String.format("最高分：%.0f", max));
                tvMinScore.setText(String.format("最低分：%.0f", min));

                double rate = total > 0 ? (fail * 100.0 / total) : 0;
                tvFailRate.setText(String.format("挂科率：%.1f%%", rate));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "统计失败", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    // ======================
    // 数据库备份
    // ======================
    private void backupDatabase() {
        FileWriter writer = null;
        SQLiteDatabase db = null;
        try {
            // 完全用你能用的 DOWNLOAD 文件夹
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
            String fileName = "student_manager_backup_" + sdf.format(new Date()) + ".db";
            // 备份文件放在下载目录，和你导出CSV一样
            File backupFile = new File(downloadDir, fileName);

            // 打开当前数据库
            db = dbHelper.getReadableDatabase();
            db.close();

            // 复制文件
            File src = getDatabasePath(DatabaseHelper.DATABASE_NAME);
            FileInputStream in = new FileInputStream(src);
            FileOutputStream out = new FileOutputStream(backupFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();

            runOnUiThread(() -> Toast.makeText(this,
                    "✅ 备份成功！\n路径：下载/" + backupFile.getName(),
                    Toast.LENGTH_LONG).show());

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this,
                    "❌ 备份失败", Toast.LENGTH_SHORT).show());
        }
    }

    // ======================
    // 数据库恢复
    // ======================
    private void restoreDatabase() {

            new AlertDialog.Builder(this)
                    .setTitle("恢复数据")
                    .setMessage("确定要恢复最新的备份吗？")
                    .setPositiveButton("确定", (dialog, which) -> new Thread(() -> {
                        try {
                            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                            File[] files = downloadDir.listFiles((dir, name) -> name.startsWith("student_manager_backup_") && name.endsWith(".db"));

                            if (files == null || files.length == 0) {
                                runOnUiThread(() -> Toast.makeText(this, "❌ 暂无备份", Toast.LENGTH_SHORT).show());
                                return;
                            }

                            // 按时间排序，取最新的
                            File latest = files[0];
                            for (File f : files) {
                                if (f.lastModified() > latest.lastModified()) {
                                    latest = f;
                                }
                            }

                            dbHelper.close();

                            File dst = getDatabasePath(DatabaseHelper.DATABASE_NAME);
                            FileInputStream in = new FileInputStream(latest);
                            FileOutputStream out = new FileOutputStream(dst);

                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }
                            in.close();
                            out.close();

                            runOnUiThread(() -> Toast.makeText(this, "✅ 已恢复最新备份", Toast.LENGTH_LONG).show());

                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() -> Toast.makeText(this, "❌ 恢复失败", Toast.LENGTH_SHORT).show());
                        }
                    }).start())
                    .setNegativeButton("取消", null)
                    .show();
        }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}