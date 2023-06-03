package com.example.vchatmessenger.gui.activities;

import static com.example.vchatmessenger.core.secret_keys_worker.SecretKeyWorker.getSecretWords;
import static com.example.vchatmessenger.core.api.server.UserRestClient.createUser;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.setAuthData;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.dto.CreateUserDto;
import com.example.vchatmessenger.dto.User;

import java.util.ArrayList;
import java.util.Base64;

public class SecretKeyActivity extends AppCompatActivity {
    Button button_next;
    ImageButton button_back;
    TextView secret_key1;
    TextView secret_key2;
    TextView secret_key3;
    TextView secret_key4;
    TextView secret_key5;
    TextView errorMessage;
    public static byte[] imageData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secret_key_activity);
        button_back = findViewById(R.id.buttonBack);
        button_next = findViewById(R.id.buttonNext);
        secret_key1 = findViewById(R.id.secret_number1);
        secret_key2 = findViewById(R.id.secret_number2);
        secret_key3 = findViewById(R.id.secret_number3);
        secret_key4 = findViewById(R.id.secret_number4);
        secret_key5 = findViewById(R.id.secret_number5);
        errorMessage = findViewById(R.id.error_message);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        TextView[] secret_numbers = {secret_key1, secret_key2, secret_key3, secret_key4, secret_key5};
        ArrayList<String> secretWords = getSecretWords(getApplicationContext());
        for (int i = 0; i < secret_numbers.length; i++) {
            secret_numbers[i].setText(secretWords.get(i));
        }
        button_back.setOnClickListener(v -> {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            finish();
        });
        button_next.setOnClickListener(v -> {
            Intent intent = new Intent(SecretKeyActivity.this, ChatViewActivity.class);
            Bundle b = getIntent().getExtras();
            b.putStringArrayList("secret words", secretWords);
            intent.putExtras(b);
            CreateUserDto createUserDto = new CreateUserDto();
            createUserDto.setName(b.getString("name"));
            createUserDto.setNickname(b.getString("nickname"));
            createUserDto.setPassword(b.getString("password"));
            createUserDto.setSecretWords(b.getStringArrayList("secret words"));
            createUserDto.setImageData(Base64.getEncoder().encodeToString(imageData));
            createUserDto.setTypeOfImage(b.getInt("type_of_image"));
            Thread t = new Thread(() -> {
                User userResult = createUser(createUserDto);
                if (userResult != null) {
                    startActivity(intent);
                    runOnUiThread(() -> errorMessage.setText(""));
                    setAuthData(
                            getApplicationContext(),
                            b.getString("nickname").toLowerCase().strip(),
                            b.getString("password"),
                            userResult.getId()
                    );
                    vibrator.vibrate(VibrationEffect.createOneShot(160, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    runOnUiThread(() -> errorMessage.setText(R.string.connection_problems));
                    vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            });
            t.start();
        });
    }
}
