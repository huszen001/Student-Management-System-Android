package com.example.studentmanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.studentmanager.utils.DataGenerator;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "student_manager.db";
    private static final int DATABASE_VERSION = 1;

    // 用户表
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_STUDENT_ID = "student_id";
    public static final String COLUMN_USER_PASSWORD = "password";
    // 用户类型（1:管理员，2:学生）
    public static final String COLUMN_USER_TYPE = "type";
    public static final String COLUMN_USER_CLASS = "class";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PHONE = "phone";

    // 新增：年龄 + 性别
    public static final String COLUMN_USER_AGE = "age";
    public static final String COLUMN_USER_GENDER = "gender";

    // 课程表
    public static final String TABLE_COURSES = "courses";
    public static final String COLUMN_COURSE_ID = "id";
    public static final String COLUMN_COURSE_NAME = "name";
    public static final String COLUMN_COURSE_TEACHER = "teacher";
    public static final String COLUMN_COURSE_TIME = "time";
    public static final String COLUMN_COURSE_CODE = "course_code";
    public static final String COLUMN_COURSE_CREDIT = "credit";
    // 上课地点
    public static final String COLUMN_COURSE_LOCATION = "location";
    // 课程容量
    public static final String COLUMN_COURSE_CAPACITY = "capacity";
    // 已选人数
    public static final String COLUMN_COURSE_SELECTED = "selected";

    // 选课申请表
    public static final String TABLE_COURSE_APPLICATIONS = "course_applications";
    public static final String COLUMN_APPLICATION_ID = "id";
    public static final String COLUMN_APPLICATION_STUDENT_ID = "student_id";
    public static final String COLUMN_APPLICATION_COURSE_ID = "course_id";
    // 申请状态
    public static final String COLUMN_APPLICATION_STATUS = "status";
    public static final String COLUMN_APPLICATION_TIME = "apply_time";

    // 成绩表
    public static final String TABLE_GRADES = "grades";
    public static final String COLUMN_GRADE_ID = "grade_id";
    public static final String COLUMN_GRADE_STUDENT_ID = "student_id";
    public static final String COLUMN_GRADE_COURSE_ID = "course_id";
    public static final String COLUMN_GRADE_SCORE = "score";
    public static final String COLUMN_GRADE_TIME = "grade_time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建用户表 管理员和学生
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_NAME + " TEXT NOT NULL, " +
                COLUMN_USER_STUDENT_ID + " TEXT UNIQUE NOT NULL, " +
                COLUMN_USER_PASSWORD + " TEXT NOT NULL, " +
                COLUMN_USER_TYPE + " INTEGER NOT NULL, " +
                COLUMN_USER_CLASS + " TEXT, " +
                COLUMN_USER_EMAIL + " TEXT, " +
                 COLUMN_USER_AGE + " INTEGER,"  // 年龄
                + COLUMN_USER_GENDER + " TEXT,"+
                COLUMN_USER_PHONE + " TEXT" + ")";
        db.execSQL(createUsersTable);

        // 创建课程表
        String createCoursesTable = "CREATE TABLE " + TABLE_COURSES + " (" +
                COLUMN_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_COURSE_NAME + " TEXT NOT NULL, " +
                COLUMN_COURSE_CODE + " TEXT NOT NULL, " +
                COLUMN_COURSE_CREDIT + " INTEGER DEFAULT 0,"+
                COLUMN_COURSE_TEACHER + " TEXT NOT NULL, " +
                COLUMN_COURSE_TIME + " TEXT NOT NULL, " +
                COLUMN_COURSE_LOCATION + " TEXT NOT NULL, " +
                COLUMN_COURSE_CAPACITY + " INTEGER NOT NULL, " +
                COLUMN_COURSE_SELECTED + " INTEGER DEFAULT 0" +
                ")";
        db.execSQL(createCoursesTable);

        // 创建选课申请表（关联用户和课程）
        String createApplicationsTable = "CREATE TABLE " + TABLE_COURSE_APPLICATIONS + " (" +
                COLUMN_APPLICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_APPLICATION_STUDENT_ID + " INTEGER NOT NULL, " +
                COLUMN_APPLICATION_COURSE_ID + " INTEGER NOT NULL, " +
                COLUMN_APPLICATION_STATUS + " TEXT NOT NULL, " +
                COLUMN_APPLICATION_TIME + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_APPLICATION_STUDENT_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), " +
                "FOREIGN KEY (" + COLUMN_APPLICATION_COURSE_ID + ") REFERENCES " + TABLE_COURSES + "(" + COLUMN_COURSE_ID + ")" +
                ")";
        db.execSQL(createApplicationsTable);

        // 创建成绩表（关联学生和课程）
        db.execSQL("CREATE TABLE " + TABLE_GRADES + " (" +
                COLUMN_GRADE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_GRADE_STUDENT_ID + " INTEGER, " +
                COLUMN_GRADE_COURSE_ID + " INTEGER, " +
                COLUMN_GRADE_SCORE + " REAL, " +
                COLUMN_GRADE_TIME + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_GRADE_STUDENT_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), " +
                "FOREIGN KEY(" + COLUMN_GRADE_COURSE_ID + ") REFERENCES " + TABLE_COURSES + "(" + COLUMN_COURSE_ID + "))");

        // 添加管理员账号
        ContentValues adminValues = new ContentValues();
        adminValues.put(COLUMN_USER_NAME, "demo-admin");
        adminValues.put(COLUMN_USER_STUDENT_ID, "demo-admin");
        adminValues.put(COLUMN_USER_PASSWORD, "demo123");
        adminValues.put(COLUMN_USER_EMAIL, "admin@example.com");
        adminValues.put(COLUMN_USER_PHONE, "00000000000");
        adminValues.put(COLUMN_USER_TYPE, 1); // 管理员类型
        db.insert(TABLE_USERS, null, adminValues);

        // 检查并生成测试数据
        DataGenerator.checkAndGenerateData(db);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 数据库升级时的处理
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSE_APPLICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADES);
        onCreate(db);
    }
    // 批量插入测试课程
    private void insertDemoCourses(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            addCourse(db, "CS1001", "高等数学", "示例教师A", "周一 1节", "教学楼A101", 50, 0, 4);
            addCourse(db, "CS1002", "大学英语", "示例教师B", "周二 3节", "教学楼B202", 45, 0, 3);
            addCourse(db, "CS1003", "计算机基础", "示例教师C", "周三 2节", "实验楼C302", 40, 0, 3);
            addCourse(db, "CS1004", "马克思主义基本原理", "示例教师D", "周四 1节", "教学楼D403", 60, 0, 2);
            addCourse(db, "CS1005", "程序设计基础", "示例教师E", "周五 4节", "实验楼E501", 35, 0, 4);
            addCourse(db, "CS2001", "数据结构", "示例教师F", "周一 3节", "教学楼A203", 40, 0, 4);
            addCourse(db, "CS2002", "操作系统", "示例教师G", "周二 1节", "实验楼C303", 35, 0, 4);
            addCourse(db, "CS2003", "计算机网络", "示例教师H", "周三 4节", "实验楼E502", 45, 0, 3);
            addCourse(db, "CS2004", "数据库原理", "示例教师I", "周四 3节", "实验楼F601", 40, 0, 4);
            addCourse(db, "CS3001", "人工智能导论", "示例教师J", "周五 3节", "教学楼B203", 30, 0, 3);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // 单条插入工具方法
    private void addCourse(SQLiteDatabase db, String code, String name, String teacher,
                           String time, String location, int capacity, int selected, int credit) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_COURSE_CODE, code);
        values.put(COLUMN_COURSE_NAME, name);
        values.put(COLUMN_COURSE_TEACHER, teacher);
        values.put(COLUMN_COURSE_TIME, time);
        values.put(COLUMN_COURSE_LOCATION, location);
        values.put(COLUMN_COURSE_CAPACITY, capacity);
        values.put(COLUMN_COURSE_SELECTED, selected);
        values.put(COLUMN_COURSE_CREDIT, credit);
        db.insert(TABLE_COURSES, null, values);
    }
}
