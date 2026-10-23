package com.example.studentmanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtils {
    private static final String PREF_NAME = "student_manager_pref";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_USERNAME = "username";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveLoginState(Context context, boolean isLoggedIn, int userId, int userType, String username) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putInt(KEY_USER_ID, userId);
        editor.putInt(KEY_USER_TYPE, userType);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public static boolean isLoggedIn(Context context) {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static int getUserId(Context context) {
        return getPrefs(context).getInt(KEY_USER_ID, -1);
    }

    public static int getUserType(Context context) {
        return getPrefs(context).getInt(KEY_USER_TYPE, -1);
    }

    public static String getUsername(Context context) {
        return getPrefs(context).getString(KEY_USERNAME, "");
    }

    public static void clearLoginState(Context context) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.clear();
        editor.apply();
    }
} 