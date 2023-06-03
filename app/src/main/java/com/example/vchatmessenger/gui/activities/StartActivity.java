package com.example.vchatmessenger.gui.activities;

import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.isLoggedIn;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity;

public class StartActivity extends LocaleAwareCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent;
        if (isLoggedIn(getApplicationContext())) {
            intent = new Intent(StartActivity.this, ChatViewActivity.class);
        } else {
            intent = new Intent(StartActivity.this, WelcomeActivity.class);
        }
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }
}
