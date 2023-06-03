package com.example.vchatmessenger.gui.activities;

import static com.example.vchatmessenger.core.api.server.UserRestClient.checkPassword;
import static com.example.vchatmessenger.core.api.server.UserRestClient.get;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.setAuthData;

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
import com.example.vchatmessenger.dto.User;

public class EnterPasswordActivity extends AppCompatActivity {

    EditText password;
    TextView password_recovery;
    TextView wrong_password;
    ImageButton button_back;
    Button button_next;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_password_activity);
        password = findViewById(R.id.password);
        password_recovery = findViewById(R.id.password_recovery);
        wrong_password = findViewById(R.id.wrong_password);
        button_back = findViewById(R.id.buttonBack);
        button_next = findViewById(R.id.buttonNext);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        password_recovery.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE));
            Intent intent = new Intent(EnterPasswordActivity.this, CheckSecretKeyActivity.class);
            intent.putExtra("nickname", getIntent().getStringExtra("nickname"));
            startActivity(intent);
        });
        button_back.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            finish();
        });
        button_next.setOnClickListener(view -> {
            Thread t = new Thread(() -> {
                int passwordResult = checkPassword(getIntent().getStringExtra("nickname"),
                        password.getText().toString());
                if (passwordResult == 1) {
                    runOnUiThread(() -> wrong_password.setText(""));
                    User user = get(getIntent().getStringExtra("nickname"),
                                    password.getText().toString());
                    setAuthData(
                            getApplicationContext(),
                            getIntent().getStringExtra("nickname").toLowerCase().strip(),
                            password.getText().toString(),
                            user.getId()
                    );
                    vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
                    Intent intent = new Intent(EnterPasswordActivity.this, ChatViewActivity.class);
                    startActivity(intent);
                } else if (passwordResult == 0) {
                    runOnUiThread(() -> wrong_password.setText(R.string.wrong_password));
                } else {
                    runOnUiThread(() -> wrong_password.setText(R.string.connection_problems));
                }
            });
            t.start();
        });
    }
}
