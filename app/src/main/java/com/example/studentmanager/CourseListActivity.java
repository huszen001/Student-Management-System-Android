package com.example.studentmanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanager.adapter.CourseAdapter;
import com.example.studentmanager.db.DatabaseHelper;
import com.example.studentmanager.model.Course;
import com.example.studentmanager.utils.SPUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CourseListActivity extends AppCompatActivity {
    private RecyclerView rvCourses;
    private MaterialButton btnSort, btnFilter;
    private TextInputLayout tilSearch;
    private TextInputEditText etSearch;
    private CourseAdapter adapter;
    private List<Course> courses;
    private DatabaseHelper dbHelper;
    private boolean isAdmin;
    private String currentSort = "name";
    private String currentFilter = "all";
    private List<Integer> appliedCourseIds = new ArrayList<>();
    private List<Course> selectedCourses = new ArrayList<>(); // 新增：学生已选课程
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        dbHelper = new DatabaseHelper(this);
        courses = new ArrayList<>();
        isAdmin = SPUtils.getUserType(this) == 1;

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadCourses();
        loadSelectedCourses();
    }

    private void initViews() {
        rvCourses = findViewById(R.id.rv_courses);
        btnSort = findViewById(R.id.btn_sort);
        btnFilter = findViewById(R.id.btn_filter);
        tilSearch = findViewById(R.id.til_search);
        etSearch = findViewById(R.id.et_search);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isAdmin ? "课程管理" : "申请课程");
        }
    }

    private void setupRecyclerView() {
        adapter = new CourseAdapter(courses, isAdmin);
        rvCourses.setLayoutManager(new LinearLayoutManager(this));
        rvCourses.setAdapter(adapter);

        adapter.setOnItemClickListener((course, position) -> {
            if (isAdmin) {
                Intent intent = new Intent(this, EditCourseActivity.class);
                intent.putExtra("course_id", course.getId());
                startActivity(intent);
            }
        });

        adapter.setOnDeleteClickListener((course, position) -> {
            new AlertDialog.Builder(this)
                    .setTitle("删除课程")
                    .setMessage("确定要删除课程 \"" + course.getName() + "\" 吗？")
                    .setPositiveButton("确定", (dialog, which) -> deleteCourse(course))
                    .setNegativeButton("取消", null)
                    .show();
        });

        adapter.setOnApplyClickListener((course, position) -> applyForCourse(course));
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCourses(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSort.setOnClickListener(v -> showSortDialog());
        btnFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void showSortDialog() {
        String[] sortOptions = {"按名称排序", "按教师排序", "按时间排序", "按容量排序"};
        new AlertDialog.Builder(this)
                .setTitle("选择排序方式")
                .setItems(sortOptions, (dialog, which) -> {
                    switch (which) {
                        case 0: currentSort = "name"; break;
                        case 1: currentSort = "teacher"; break;
                        case 2: currentSort = "time"; break;
                        case 3: currentSort = "capacity"; break;
                    }
                    sortCourses();
                })
                .show();
    }

    private void showFilterDialog() {
        String[] filterOptions = {"全部课程", "已满课程", "未满课程"};
        new AlertDialog.Builder(this)
                .setTitle("选择分类")
                .setItems(filterOptions, (dialog, which) -> {
                    switch (which) {
                        case 0: currentFilter = "all"; break;
                        case 1: currentFilter = "full"; break;
                        case 2: currentFilter = "available"; break;
                    }
                    filterCourses(etSearch.getText().toString());
                })
                .show();
    }

    private void sortCourses() {
        List<Course> sortedList = new ArrayList<>(courses);
        Collections.sort(sortedList, (c1, c2) -> {
            switch (currentSort) {
                case "name": return c1.getName().compareTo(c2.getName());
                case "teacher": return c1.getTeacher().compareTo(c2.getTeacher());
                case "time": return c1.getTime().compareTo(c2.getTime());
                case "capacity": return Integer.compare(c1.getCapacity(), c2.getCapacity());
                default: return 0;
            }
        });
        adapter.updateCourses(sortedList);
    }

    private void filterCourses(String searchText) {
        List<Course> filteredList = new ArrayList<>();
        for (Course course : courses) {
            boolean matchesSearch = searchText.isEmpty() ||
                    course.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                    course.getTeacher().toLowerCase().contains(searchText.toLowerCase()) ||
                    course.getTime().toLowerCase().contains(searchText.toLowerCase()) ||
                    course.getLocation().toLowerCase().contains(searchText.toLowerCase()) ||
                    course.getCourseCode().toLowerCase().contains(searchText.toLowerCase());

            boolean matchesFilter = currentFilter.equals("all") ||
                    (currentFilter.equals("full") && course.getSelected() >= course.getCapacity()) ||
                    (currentFilter.equals("available") && course.getSelected() < course.getCapacity());

            if (matchesSearch && matchesFilter) {
                filteredList.add(course);
            }
        }
        adapter.updateCourses(filteredList);
    }

    // ==============================================
    // 已升级：加载课程 + 读取 课程号、学分
    // ==============================================
    private void loadCourses() {
        courses.clear();
        appliedCourseIds.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int studentId = SPUtils.getUserId(this);

        if (isAdmin) {
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_COURSES;
            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_ID));
                String code = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_CODE));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_NAME));
                String teacher = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_TEACHER));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_TIME));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_LOCATION));
                int capacity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_CAPACITY));
                int selected = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_SELECTED));
                int credit = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_CREDIT));

                courses.add(new Course(id, code, name, teacher, time, location, capacity, selected, credit));
            }
            cursor.close();
        } else {
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_COURSES;
            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_ID));
                String code = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_CODE));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_NAME));
                String teacher = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_TEACHER));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_TIME));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_LOCATION));
                int capacity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_CAPACITY));
                int selected = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_SELECTED));
                int credit = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_CREDIT));

                courses.add(new Course(id, code, name, teacher, time, location, capacity, selected, credit));
            }
            cursor.close();

            String appQuery = "SELECT " + DatabaseHelper.COLUMN_APPLICATION_COURSE_ID + " FROM " + DatabaseHelper.TABLE_COURSE_APPLICATIONS +
                    " WHERE " + DatabaseHelper.COLUMN_APPLICATION_STUDENT_ID + " = ?";
            Cursor appCursor = db.rawQuery(appQuery, new String[]{String.valueOf(studentId)});
            while (appCursor.moveToNext()) {
                int cid = appCursor.getInt(0);
                appliedCourseIds.add(cid);
            }
            appCursor.close();
        }

        sortCourses();
        filterCourses(etSearch.getText().toString());
        if (!isAdmin && adapter != null) {
            adapter.setAppliedCourseIds(new ArrayList<>(appliedCourseIds));
        }

    }
    // 加载学生已选通过的课程
    private void loadSelectedCourses() {
        selectedCourses.clear();
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
            String code = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_CODE));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_NAME));
            String teacher = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_TEACHER));
            String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_TIME));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_LOCATION));
            int capacity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_CAPACITY));
            int selected = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_SELECTED));
            int credit = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_CREDIT));

            selectedCourses.add(new Course(id, code, name, teacher, time, location, capacity, selected, credit));
        }
        cursor.close();
        db.close();
    }
    private void deleteCourse(Course course) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            Cursor cursor = db.query(DatabaseHelper.TABLE_COURSE_APPLICATIONS,
                    new String[]{"COUNT(*)"},
                    DatabaseHelper.COLUMN_APPLICATION_COURSE_ID + " = ? AND " +
                            DatabaseHelper.COLUMN_APPLICATION_STATUS + " = 'approved'",
                    new String[]{String.valueOf(course.getId())},
                    null, null, null);

            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                cursor.close();
                db.endTransaction();
                Toast.makeText(this, "该课程已有学生选修，无法删除", Toast.LENGTH_SHORT).show();
                return;
            }
            cursor.close();

            db.delete(DatabaseHelper.TABLE_COURSE_APPLICATIONS,
                    DatabaseHelper.COLUMN_APPLICATION_COURSE_ID + " = ?",
                    new String[]{String.valueOf(course.getId())});

            int result = db.delete(DatabaseHelper.TABLE_COURSES,
                    DatabaseHelper.COLUMN_COURSE_ID + " = ?",
                    new String[]{String.valueOf(course.getId())});

            if (result > 0) {
                db.setTransactionSuccessful();
                Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                loadCourses();
            } else {
                Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("CourseListActivity", "删除课程失败", e);
            Toast.makeText(this, "删除失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }
    }

    private void applyForCourse(Course course) {
        int studentId = SPUtils.getUserId(this);

        if (appliedCourseIds.contains(course.getId())) {
            Toast.makeText(this, "你已申请过该课程", Toast.LENGTH_SHORT).show();
            return;
        }

        if (course.getSelected() >= course.getCapacity()) {
            Toast.makeText(this, "该课程已满员，无法申请", Toast.LENGTH_SHORT).show();
            return;
        }


        // 3. ✅ 时间冲突检测（核心逻辑）
        for (Course c : selectedCourses) {
            if (isTimeConflict(c.getTime(), course.getTime())) {
                new AlertDialog.Builder(this)
                        .setTitle("时间冲突")
                        .setMessage("❌ 课程：" + c.getName() + "\n与要选的课程时间冲突！")
                        .setPositiveButton("确定", null)
                        .show();
                return; // 直接退出，不允许选课
            }
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            android.content.ContentValues values = new android.content.ContentValues();
            values.put(DatabaseHelper.COLUMN_APPLICATION_STUDENT_ID, studentId);
            values.put(DatabaseHelper.COLUMN_APPLICATION_COURSE_ID, course.getId());
            values.put(DatabaseHelper.COLUMN_APPLICATION_STATUS, "approved");
            values.put(DatabaseHelper.COLUMN_APPLICATION_TIME, System.currentTimeMillis());
            db.insert(DatabaseHelper.TABLE_COURSE_APPLICATIONS, null, values);

            String updateSql = "UPDATE " + DatabaseHelper.TABLE_COURSES +
                    " SET " + DatabaseHelper.COLUMN_COURSE_SELECTED + " = " + DatabaseHelper.COLUMN_COURSE_SELECTED + " + 1 " +
                    "WHERE " + DatabaseHelper.COLUMN_COURSE_ID + " = ?";
            db.execSQL(updateSql, new String[]{String.valueOf(course.getId())});

            db.setTransactionSuccessful();
            Toast.makeText(this, "选课成功！", Toast.LENGTH_SHORT).show();
            loadCourses();

        } catch (Exception e) {
            Toast.makeText(this, "选课失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }
        loadSelectedCourses(); // 刷新已选课程列表
    }
    // 判断两个课程时间是否冲突
// 时间格式示例："周一 第1-2节"、"周二 第5-8节"
    private boolean isTimeConflict(String existTime, String newTime) {
        // 1. 拆分星期和节次部分
        String[] existParts = existTime.split(" ");
        String[] newParts = newTime.split(" ");

        // 星期不同 → 无冲突
        if (!existParts[0].equals(newParts[0])) {
            return false;
        }

        // 2. 提取节次区间（如 "第1-2节" → "1-2"）
        String existSection = existParts[1].replace("第", "").replace("节", "");
        String newSection = newParts[1].replace("第", "").replace("节", "");

        // 3. 解析开始/结束节次
        int existStart = Integer.parseInt(existSection.split("-")[0]);
        int existEnd = Integer.parseInt(existSection.split("-")[1]);
        int newStart = Integer.parseInt(newSection.split("-")[0]);
        int newEnd = Integer.parseInt(newSection.split("-")[1]);

        // 4. 区间重叠判断：只要两个区间有交集，就判定为冲突
        return !(existEnd < newStart || newEnd < existStart);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourses();
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