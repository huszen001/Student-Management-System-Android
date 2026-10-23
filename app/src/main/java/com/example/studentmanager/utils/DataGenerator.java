package com.example.studentmanager.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.studentmanager.db.DatabaseHelper;

import java.util.Random;

public class DataGenerator {
    private static final String TAG = "DataGenerator";
    private static final int MIN_DATA_COUNT = 20;
    private static final String[] COURSE_NAMES = {
            "单片机与接口技术",
            "大学物理",
            "MicroPython程序设计",
            "大学英语",
            "Linux系统管理与运维",
            "Web应用开发技术",
            "前端脚本应用开发",
            "大学物理实验",
            "马克思主义基本原理",
            "敦煌文化艺术",
            "计算机组成原理",
            "操作系统原理",
            "数据结构与算法",
            "数据库系统概论",
            "计算机网络",
            "人工智能基础",
            "机器学习导论",
            "深度学习基础",
            "软件工程",
            "软件测试技术",
            "Java程序设计",
            "Python高级开发",
            "Android移动开发",
            "iOS应用开发",
            "云计算技术",
            "大数据处理",
            "网络安全",
            "密码学基础",
            "嵌入式系统",
            "树莓派开发",
            "物联网技术",
            "无线传感器网络",
            "数字图像处理",
            "计算机视觉",
            "自然语言处理",
            "数据挖掘",
            "模式识别",
            "算法设计与分析",
            "编译原理",
            "形式语言与自动机",
            "计算机图形学",
            "虚拟现实技术",
            "增强现实技术",
            "人机交互技术",
            "软件体系结构",
            "需求工程",
            "软件项目管理",
            "软件建模与设计",
            "移动互联网开发",
            "分布式系统",
            "并行计算",
            "高性能计算",
            "信息系统分析与设计",
            "管理信息系统",
            "电子商务技术",
            "电子政务系统",
            "多媒体技术",
            "信号与系统",
            "数字电路",
            "模拟电路",
            "微机原理与接口",
            "汇编语言",
            "C语言程序设计",
            "C++面向对象编程"
    };
    private static final String[] TEACHERS = {"示例教师A", "示例教师B", "示例教师C", "示例教师D", "示例教师E"};
    private static final String[] LOCATIONS = {"教学楼A101", "教学楼B203", "教学楼C305", "教学楼D407", "教学楼E509"};
    private static final String[] TIMES = {
            "周一 1-2节", "周一 3-4节", "周二 1-2节", "周二 3-4节",
            "周三 1-2节", "周三 3-4节", "周四 1-2节", "周四 3-4节", "周五 1-2节", "周五 3-4节"
    };

    private static final Random random = new Random();

    public static void checkAndGenerateData(SQLiteDatabase db) {
        try {
            db.beginTransaction();

            int studentCount = getCount(db, DatabaseHelper.TABLE_USERS,
                    DatabaseHelper.COLUMN_USER_TYPE + " = 2");
            if (studentCount < MIN_DATA_COUNT) {
                generateStudents(db, MIN_DATA_COUNT - studentCount);
            }

            int courseCount = getCount(db, DatabaseHelper.TABLE_COURSES, null);
            if (courseCount < MIN_DATA_COUNT) {
                generateCourses(db, MIN_DATA_COUNT - courseCount);
            }

            int applicationCount = getCount(db, DatabaseHelper.TABLE_COURSE_APPLICATIONS, null);
            if (applicationCount < MIN_DATA_COUNT) {
                generateCourseApplications(db, MIN_DATA_COUNT - applicationCount);
            }

            // ========== 新增：自动生成成绩 ==========
            int gradeCount = getCount(db, DatabaseHelper.TABLE_GRADES, null);
            if (gradeCount < MIN_DATA_COUNT) {
                generateGrades(db, MIN_DATA_COUNT - gradeCount);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "检查并生成数据失败", e);
        } finally {
            db.endTransaction();
        }
    }

    private static int getCount(SQLiteDatabase db, String table, String whereClause) {
        String query = "SELECT COUNT(*) FROM " + table;
        if (whereClause != null) {
            query += " WHERE " + whereClause;
        }
        Cursor cursor = db.rawQuery(query, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private static void generateStudents(SQLiteDatabase db, int count) {
        try {
            for (int i = 0; i < count; i++) {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_USER_NAME, generateName());
                values.put(DatabaseHelper.COLUMN_USER_STUDENT_ID, generateStudentId());
                values.put(DatabaseHelper.COLUMN_USER_PASSWORD, "student123");
                values.put(DatabaseHelper.COLUMN_USER_EMAIL, generateEmail());
                values.put(DatabaseHelper.COLUMN_USER_PHONE, generatePhone());
                values.put(DatabaseHelper.COLUMN_USER_TYPE, 2);
                values.put(DatabaseHelper.COLUMN_USER_CLASS, generateClass());
                values.put(DatabaseHelper.COLUMN_USER_AGE, 18 + random.nextInt(5));
                values.put(DatabaseHelper.COLUMN_USER_GENDER, random.nextBoolean() ? "男" : "女");

                db.insert(DatabaseHelper.TABLE_USERS, null, values);
            }
        } catch (Exception e) {
            Log.e(TAG, "生成学生数据失败", e);
            throw e;
        }
    }

    private static void generateCourses(SQLiteDatabase db, int count) {
        try {
            for (int i = 0; i < count; i++) {
                ContentValues values = new ContentValues();
                String courseCode = "CS" + (1000 + i);
                int credit = 1 + random.nextInt(4);

                values.put(DatabaseHelper.COLUMN_COURSE_CODE, courseCode);
                values.put(DatabaseHelper.COLUMN_COURSE_NAME, COURSE_NAMES[random.nextInt(COURSE_NAMES.length)]);
                values.put(DatabaseHelper.COLUMN_COURSE_TEACHER, TEACHERS[random.nextInt(TEACHERS.length)]);
                values.put(DatabaseHelper.COLUMN_COURSE_TIME, TIMES[random.nextInt(TIMES.length)]);
                values.put(DatabaseHelper.COLUMN_COURSE_LOCATION, LOCATIONS[random.nextInt(LOCATIONS.length)]);
                values.put(DatabaseHelper.COLUMN_COURSE_CAPACITY, random.nextInt(31) + 20);
                values.put(DatabaseHelper.COLUMN_COURSE_SELECTED, 0);
                values.put(DatabaseHelper.COLUMN_COURSE_CREDIT, credit);

                db.insert(DatabaseHelper.TABLE_COURSES, null, values);
            }
        } catch (Exception e) {
            Log.e(TAG, "生成课程数据失败", e);
            throw e;
        }
    }

    // ========== 修复：选课通过后，课程selected +1 ==========
    private static void generateCourseApplications(SQLiteDatabase db, int count) {
        try {
            String[] studentIds = getAllStudentIds(db);
            String[] courseIds = getAllCourseIds(db);

            if (studentIds.length == 0 || courseIds.length == 0) return;

            for (int i = 0; i < count; i++) {
                String studentId = studentIds[random.nextInt(studentIds.length)];
                String courseId = courseIds[random.nextInt(courseIds.length)];

                // 插入申请
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_APPLICATION_STUDENT_ID, studentId);
                values.put(DatabaseHelper.COLUMN_APPLICATION_COURSE_ID, courseId);
                values.put(DatabaseHelper.COLUMN_APPLICATION_STATUS, "approved");
                values.put(DatabaseHelper.COLUMN_APPLICATION_TIME, System.currentTimeMillis());
                db.insert(DatabaseHelper.TABLE_COURSE_APPLICATIONS, null, values);

                // ========== 关键修复：已选人数 +1 ==========
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COLUMN_COURSE_SELECTED,
                        getCourseSelectedCount(db, courseId) + 1);
                db.update(DatabaseHelper.TABLE_COURSES, cv,
                        DatabaseHelper.COLUMN_COURSE_ID + "=?",
                        new String[]{courseId});
            }
        } catch (Exception e) {
            Log.e(TAG, "生成选课申请数据失败", e);
            throw e;
        }
    }

    // ========== 新增：自动生成成绩（报表显示） ==========
    private static void generateGrades(SQLiteDatabase db, int count) {
        try {
            String[] studentIds = getAllStudentIds(db);
            String[] courseIds = getAllCourseIds(db);
            if (studentIds.length == 0 || courseIds.length == 0) return;

            for (int i = 0; i < count; i++) {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_GRADE_STUDENT_ID, studentIds[random.nextInt(studentIds.length)]);
                values.put(DatabaseHelper.COLUMN_GRADE_COURSE_ID, courseIds[random.nextInt(courseIds.length)]);
                values.put(DatabaseHelper.COLUMN_GRADE_SCORE, 60 + random.nextInt(41));
                values.put(DatabaseHelper.COLUMN_GRADE_TIME, System.currentTimeMillis());
                db.insert(DatabaseHelper.TABLE_GRADES, null, values);
            }
        } catch (Exception e) {
            Log.e(TAG, "生成成绩失败", e);
        }
    }

    private static int getCourseSelectedCount(SQLiteDatabase db, String courseId) {
        Cursor c = db.query(DatabaseHelper.TABLE_COURSES,
                new String[]{DatabaseHelper.COLUMN_COURSE_SELECTED},
                DatabaseHelper.COLUMN_COURSE_ID + "=?",
                new String[]{courseId}, null, null, null);
        int selected = 0;
        if (c.moveToFirst()) selected = c.getInt(0);
        c.close();
        return selected;
    }

    private static String[] getAllStudentIds(SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_USER_ID},
                DatabaseHelper.COLUMN_USER_TYPE + " = 2",
                null, null, null, null);
        String[] ids = new String[cursor.getCount()];
        int i = 0;
        while (cursor.moveToNext()) ids[i++] = String.valueOf(cursor.getInt(0));
        cursor.close();
        return ids;
    }

    private static String[] getAllCourseIds(SQLiteDatabase db) {
        Cursor cursor = db.query(DatabaseHelper.TABLE_COURSES,
                new String[]{DatabaseHelper.COLUMN_COURSE_ID},
                null, null, null, null, null);
        String[] ids = new String[cursor.getCount()];
        int i = 0;
        while (cursor.moveToNext()) ids[i++] = String.valueOf(cursor.getInt(0));
        cursor.close();
        return ids;
    }

    private static String generateName() {
        return "示例学生" + String.format("%03d", random.nextInt(1000));
    }

    private static String generateStudentId() {
        return String.format("2023%04d", random.nextInt(10000));
    }

    private static String generateClass() {
        String[] departments = {"计算机", "软件", "网络", "人工智能", "数据科学"};
        String[] grades = {"2020", "2021", "2022", "2023"};
        return departments[random.nextInt(departments.length)] +
                grades[random.nextInt(grades.length)] +
                String.format("%02d", random.nextInt(10) + 1);
    }

    private static String generateEmail() {
        return "student" + random.nextInt(1000) + "@example.com";
    }

    private static String generatePhone() {
        return "00000000000";
    }
}
