package com.example.vchatmessenger.gui.activities;

import static com.example.vchatmessenger.core.api.server.NameRestClient.checkName;
import static com.example.vchatmessenger.core.api.server.NicknameRestClient.checkNicknameForUser;
import static com.example.vchatmessenger.core.constants.Constants.connectionError;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vchatmessenger.R;

import java.util.concurrent.atomic.AtomicReference;


public class RegistrationActivity extends AppCompatActivity {
    private EditText name;
    private EditText nickname;
    private TextView error_message_for_nickname;

    protected boolean check_ability_to_click() {
        AtomicReference<Boolean> res = new AtomicReference<>(false);
        Thread t = new Thread(() -> {
            Integer nameResult = checkName(name.getText().toString());
            if (nameResult == 200  && checkNickname()) {
                res.set(true);
            } else {
                runOnUiThread(() -> {
                    if (nameResult == connectionError) {
                        runOnUiThread(() -> error_message_for_nickname.setText(R.string.connection_problems));
                    } else if (nameResult != 200) {
                        error_message_for_nickname.setText(R.string.error_message_for_name);
                    }
                });
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return res.get();
    }


    protected boolean checkNickname() {
        AtomicReference<Boolean> res = new AtomicReference<>(false);
        Thread t = new Thread(() -> {
            int nicknameResult = checkNicknameForUser(nickname.getText().toString());
            if (nicknameResult == 200) {
                runOnUiThread(() -> error_message_for_nickname.setText(""));
                res.set(true);
            } else if (nicknameResult == 400) {
                runOnUiThread(() -> error_message_for_nickname.setText(R.string.error_message_for_nickname));
            } else if (nicknameResult == 500) {
                runOnUiThread(() -> error_message_for_nickname.setText(R.string.nickname_is_taken));
            } else {
                runOnUiThread(() -> error_message_for_nickname.setText(R.string.connection_problems));
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return res.get();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        name = findViewById(R.id.name);
        nickname = findViewById(R.id.nickname);
        error_message_for_nickname = findViewById(R.id.error_message_nickname);
        Button button_next = findViewById(R.id.buttonNext);
        ImageButton button_back = findViewById(R.id.buttonBack);
        button_next.setOnClickListener(v -> {
            if (check_ability_to_click()) {
                vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
                Intent intent = new Intent(RegistrationActivity.this, PasswordCreationActivity.class);
                Bundle b = new Bundle();
                b.putString("name", name.getText().toString());
                b.putString("nickname", nickname.getText().toString());
                intent.putExtras(b);
                startActivity(intent);
            } else {
                vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        });

        button_back.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            finish();
        });
    }
}