package com.example.vchatmessenger.gui.activities;

import static com.example.vchatmessenger.core.api.server.UserRestClient.exists;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vchatmessenger.R;

public class LoginActivity extends AppCompatActivity {

    ImageButton button_back;
    Button button_next;
    EditText nickname;
    TextView error_message_for_nickname;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        button_back = findViewById(R.id.buttonBack);
        button_next = findViewById(R.id.buttonNext);
        nickname = findViewById(R.id.nickname);
        error_message_for_nickname = findViewById(R.id.error_message_for_nickname);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        button_back.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            finish();
        });
        button_next.setOnClickListener(view -> {
            Thread t = new Thread(() -> {
                int nicknameResult = exists(nickname.getText().toString());
                if (nicknameResult == 1) {
                    runOnUiThread(() -> error_message_for_nickname.setText(""));
                    vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
                    Intent intent = new Intent(LoginActivity.this, EnterPasswordActivity.class);
                    intent.putExtra("nickname", nickname.getText().toString());
                    startActivity(intent);
                } else if (nicknameResult == 0) {
                    runOnUiThread(() -> error_message_for_nickname.
                            setText(R.string.error_message_for_nickname_not_found));
                } else {
                    runOnUiThread(() -> error_message_for_nickname.
                            setText(R.string.connection_problems));
                }
            });
            t.start();
        });
    }
}
