package com.example.chatrealtime.model;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_MA_TAI_KHOAN = "maTaiKhoan";
    private static final String KEY_MA_NGUOI_DUNG = "maNguoiDung";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_REMEMBER = "remember";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void login(int maTaiKhoan, int maNguoiDung, String email, String token, boolean remember) {
        editor.putInt(KEY_MA_TAI_KHOAN, maTaiKhoan);
        editor.putInt(KEY_MA_NGUOI_DUNG, maNguoiDung);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TOKEN, token);
        editor.putBoolean(KEY_REMEMBER, remember);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_TOKEN) && prefs.getBoolean(KEY_REMEMBER, false);
    }

    public int getMaTaiKhoan() {
        return prefs.getInt(KEY_MA_TAI_KHOAN, -1);
    }

    public int getMaNguoiDung() {
        return prefs.getInt(KEY_MA_NGUOI_DUNG, -1);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
