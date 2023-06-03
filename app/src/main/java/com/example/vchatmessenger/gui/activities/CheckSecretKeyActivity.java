package com.example.vchatmessenger.gui.activities;

import static com.example.vchatmessenger.core.api.server.UserRestClient.checkSecretKeys;

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

import java.util.ArrayList;
import java.util.Collections;

public class CheckSecretKeyActivity extends AppCompatActivity {

    TextView number1_text;
    EditText number1_input;
    TextView number2_text;
    EditText number2_input;
    TextView number3_text;
    EditText number3_input;

    ImageButton button_back;
    Button button_next;
    TextView warning_text;

    public static ArrayList<Integer> getRandomNumbers() {
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            result.add(numbers.get(i));
        }
        Collections.sort(result);
        return result;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_secret_key_activity);
        number1_text = findViewById(R.id.number1_text);
        number1_input = findViewById(R.id.number1_input);
        number2_text = findViewById(R.id.number2_text);
        number2_input = findViewById(R.id.number2_input);
        number3_text = findViewById(R.id.number3_text);
        number3_input = findViewById(R.id.number3_input);
        button_back = findViewById(R.id.buttonBack);
        button_next = findViewById(R.id.buttonNext);
        warning_text = findViewById(R.id.warning_text);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        ArrayList<Integer> massive = getRandomNumbers();
        // установить значения massive в number1_text, number2_text, number3_text
        number1_text.setText(massive.get(0) + ".");
        number2_text.setText(massive.get(1) + ".");
        number3_text.setText(massive.get(2) + ".");
        button_back.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            finish();
        });
        button_next.setOnClickListener(view -> {
            Thread t = new Thread(() -> {
                int secretKeysResult = checkSecretKeys(
                        getIntent().getStringExtra("nickname"),
                        massive.get(0) -1, number1_input.getText().toString(),
                        massive.get(1) - 1, number2_input.getText().toString(),
                        massive.get(2) - 1, number3_input.getText().toString()
                );
                if (secretKeysResult == 1) {
                    runOnUiThread(() -> warning_text.setText(""));
                    vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
                    Intent intent = new Intent(CheckSecretKeyActivity.this, PasswordCreationActivity.class);
                    intent.putExtra("launchMode", 2);
                    intent.putExtra("nickname", getIntent().getStringExtra("nickname"));
                    intent.putExtra("a", massive.get(0) - 1);
                    intent.putExtra("a_value", number1_input.getText().toString());
                    intent.putExtra("b", massive.get(1) - 1);
                    intent.putExtra("b_value", number2_input.getText().toString());
                    intent.putExtra("c", massive.get(2) - 1);
                    intent.putExtra("c_value", number3_input.getText().toString());
                    startActivity(intent);
                } else if (secretKeysResult == 0) {
                    runOnUiThread(() -> warning_text.setText(R.string.warning_text));
                } else {
                    runOnUiThread(() -> warning_text.setText(R.string.connection_problems));
                }
            });
            t.start();
        });
    }
}
