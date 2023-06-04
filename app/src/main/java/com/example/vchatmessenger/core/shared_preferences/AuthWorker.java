package com.example.vchatmessenger.core.shared_preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.vchatmessenger.core.api.socket_gateway.SocketService;

import java.util.ArrayList;

public class AuthWorker {

    public static ArrayList<String> getAuthData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        ArrayList<String> data = new ArrayList<>();
        data.add(sharedPreferences.getString("nickname", ""));
        data.add(sharedPreferences.getString("password", ""));
        return data;
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isloggedin", false);
    }

    public static Long getId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        return sharedPreferences.getLong("id", 0);
    }

    public static void setAuthData(Context context, String newNickname, String newPassword, Long id) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nickname", newNickname);
        editor.putString("password", newPassword);
        editor.putLong("id", id);
        editor.putBoolean("isloggedin", true);
        editor.apply();
        SocketService.updateOptions(newNickname, newPassword);
    }


    public static void logout(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nickname", "");
        editor.putString("password", "");
        editor.putLong("id", 0L);
        editor.putBoolean("isloggedin", false);
        editor.apply();
    }
}
