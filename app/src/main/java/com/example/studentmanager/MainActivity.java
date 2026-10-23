package com.example.studentmanager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.studentmanager.db.DatabaseHelper;
import com.example.studentmanager.utils.SPUtils;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private LinearLayout studentFunctions, adminFunctions;
    private DatabaseHelper dbHelper;
    private int currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 检查登录状态
        if (!SPUtils.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentUserId = getIntent().getIntExtra("user_id", 0);
        initViews();
        setupDrawer();
        setupFunctions();
        updateUserInfo();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        studentFunctions = findViewById(R.id.student_functions);
        adminFunctions = findViewById(R.id.admin_functions);

        dbHelper = new DatabaseHelper(this);
    }

    private void setupDrawer() {
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_menu) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 根据用户类型显示不同功能区
    private void setupFunctions() {
        int userType = SPUtils.getUserType(this);
        if (userType == 0) { // 学生
            studentFunctions.setVisibility(View.VISIBLE);
            adminFunctions.setVisibility(View.GONE);
            setupStudentFunctions();
        } else { // 管理员
            studentFunctions.setVisibility(View.GONE);
            adminFunctions.setVisibility(View.VISIBLE);
            setupAdminFunctions();
        }
    }

    private void setupStudentFunctions() {
        Button btnViewProfile = findViewById(R.id.btn_view_profile);
        Button btnEditProfile = findViewById(R.id.btn_edit_profile);
        Button btnMyCourses = findViewById(R.id.btn_my_courses);
        Button btnApplyCourse = findViewById(R.id.btn_apply_course);
        Button btnMyGrades = findViewById(R.id.btn_my_grades);
        Button btnCourseTable = findViewById(R.id.btn_course_table);

        btnViewProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        btnMyCourses.setOnClickListener(v -> {
            startActivity(new Intent(this, MyCoursesActivity.class));
        });

        btnApplyCourse.setOnClickListener(v -> {
            startActivity(new Intent(this, CourseListActivity.class));
        });

        btnMyGrades.setOnClickListener(v -> {
            startActivity(new Intent(this, MyGradeActivity.class));
        });
        // 课表点击事件
        btnCourseTable.setOnClickListener(v -> {
            startActivity(new Intent(this, CourseTableActivity.class));
        });
    }

    private void setupAdminFunctions() {
        Button btnManageStudents = findViewById(R.id.btn_manage_students);
        Button btnManageCourses = findViewById(R.id.btn_manage_courses);
        Button btnAddCourse = findViewById(R.id.btn_add_course);

        Button btnViewReports = findViewById(R.id.btn_view_reports);
        Button btnLogout = findViewById(R.id.btn_logout);
        Button btnGradeManagement = findViewById(R.id.btn_grade_management);

        btnManageStudents.setOnClickListener(v -> {
            startActivity(new Intent(this, StudentManagementActivity.class));
        });

        btnManageCourses.setOnClickListener(v -> {
            startActivity(new Intent(this, CourseListActivity.class));
        });

        btnAddCourse.setOnClickListener(v -> {
            startActivity(new Intent(this, AddCourseActivity.class));
        });



        btnViewReports.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportActivity.class));
        });

        btnGradeManagement.setOnClickListener(v -> {
            startActivity(new Intent(this, GradeListActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            SPUtils.clearLoginState(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    // 更新侧边栏头部的用户信息
    private void updateUserInfo() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvUsername = headerView.findViewById(R.id.tv_username);
        TextView tvUserType = headerView.findViewById(R.id.tv_user_type);

        String username = SPUtils.getUsername(this);
        int userType = SPUtils.getUserType(this);

        tvUsername.setText(username);
        tvUserType.setText(userType == 0 ? "学生" : "管理员");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_edit_profile) {
            startActivity(new Intent(this, EditProfileActivity.class));
        } else if (id == R.id.nav_change_password) {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        } else if (id == R.id.nav_logout) {
            SPUtils.clearLoginState(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}