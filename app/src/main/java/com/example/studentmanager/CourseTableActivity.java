package com.example.studentmanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.studentmanager.db.DatabaseHelper;
import com.example.studentmanager.utils.SPUtils;

public class CourseTableActivity extends AppCompatActivity {

    TextView mon1,mon2,mon3,tue1,tue2,tue3,wed1,wed2,wed3,thu1,thu2,thu3,fri1,fri2,fri3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_table);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        mon1 = findViewById(R.id.mon1);
        mon2 = findViewById(R.id.mon2);
        mon3 = findViewById(R.id.mon3);
        tue1 = findViewById(R.id.tue1);
        tue2 = findViewById(R.id.tue2);
        tue3 = findViewById(R.id.tue3);
        wed1 = findViewById(R.id.wed1);
        wed2 = findViewById(R.id.wed2);
        wed3 = findViewById(R.id.wed3);
        thu1 = findViewById(R.id.thu1);
        thu2 = findViewById(R.id.thu2);
        thu3 = findViewById(R.id.thu3);
        fri1 = findViewById(R.id.fri1);
        fri2 = findViewById(R.id.fri2);
        fri3 = findViewById(R.id.fri3);

        int currentUserId = SPUtils.getUserId(this);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT c.name, c.teacher, c.location, c.time "+
                        "FROM course_applications a "+
                        "JOIN courses c ON a.course_id = c.id "+
                        "WHERE a.student_id = ?",
                new String[]{String.valueOf(currentUserId)}
        );

        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String teacher = cursor.getString(1);
            String location = cursor.getString(2);
            String time = cursor.getString(3);

            // 🔥 强制限制：只显示 课程名\n教室，不显示老师，避免文字太多
            String info = name + "\n" + location;

            setText(time, info);
        }

        cursor.close();
        db.close();
    }

    private void setText(String time, String info) {
        TextView tv = null;

        if (time.contains("周一")) {
            if (time.contains("1-2")) tv = mon1;
            else if (time.contains("3-4")) tv = mon2;
            else tv = mon3;
        } else if (time.contains("周二")) {
            if (time.contains("1-2")) tv = tue1;
            else if (time.contains("3-4")) tv = tue2;
            else tv = tue3;
        } else if (time.contains("周三")) {
            if (time.contains("1-2")) tv = wed1;
            else if (time.contains("3-4")) tv = wed2;
            else tv = wed3;
        } else if (time.contains("周四")) {
            if (time.contains("1-2")) tv = thu1;
            else if (time.contains("3-4")) tv = thu2;
            else tv = thu3;
        } else if (time.contains("周五")) {
            if (time.contains("1-2")) tv = fri1;
            else if (time.contains("3-4")) tv = fri2;
            else tv = fri3;
        }

        if (tv != null) {
            tv.setText(info);
        }
    }
}