package com.example.vchatmessenger.gui.activities;

import static com.example.vchatmessenger.core.secret_keys_worker.SecretKeyWorker.getSecretWords;
import static com.example.vchatmessenger.core.api.server.UserRestClient.changeSecretKeys;
import static com.example.vchatmessenger.core.api.server.UserRestClient.get;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.dto.User;

import java.util.ArrayList;
import java.util.List;

public class ChangeSecretKeyActivity extends AppCompatActivity {

    Button button_regenerate;
    ImageButton button_back;
    TextView secret_key1;
    TextView secret_key2;
    TextView secret_key3;
    TextView secret_key4;
    TextView secret_key5;
    TextView error_message;

    public static boolean onSecretKeyChange = false;

    private void setData(List<String> secretWords) {
        secret_key1.setText(secretWords.get(0));
        secret_key2.setText(secretWords.get(1));
        secret_key3.setText(secretWords.get(2));
        secret_key4.setText(secretWords.get(3));
        secret_key5.setText(secretWords.get(4));
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secret_key_activity);
        button_regenerate = findViewById(R.id.buttonNext);
        button_back = findViewById(R.id.buttonBack);
        secret_key1 = findViewById(R.id.secret_number1);
        secret_key2 = findViewById(R.id.secret_number2);
        secret_key3 = findViewById(R.id.secret_number3);
        secret_key4 = findViewById(R.id.secret_number4);
        secret_key5 = findViewById(R.id.secret_number5);
        error_message = findViewById(R.id.error_message);
        button_regenerate.setText(R.string.regenerate);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        Thread t = new Thread(() -> {
            ArrayList<String> authData = getAuthData(getApplicationContext());
            User userResult = get(authData.get(0), authData.get(1));
            if (userResult != null) {
                error_message.setText("");
                setData(userResult.getSecretKeys());
            } else {
                error_message.setText(R.string.connection_problems);
            }
        });
        t.start();
        button_back.setOnClickListener(v -> {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            finish();
        });
        button_regenerate.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.regenerate_secret_key_question);
            builder.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                Thread thread = new Thread(() -> {
                    if (!onSecretKeyChange) {
                        onSecretKeyChange = true;
                        ArrayList<String> secretWords = getSecretWords(getApplicationContext());
                        ArrayList<String> authData = getAuthData(getApplicationContext());
                        int secretKeysChangeResult = changeSecretKeys(authData.get(0), authData.get(1), secretWords);
                        if (secretKeysChangeResult == 1) {
                            setData(secretWords);
                            runOnUiThread(() -> Toast.makeText(this, getString(R.string.secret_key_regenerated), Toast.LENGTH_SHORT).show());
                            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            runOnUiThread(() -> error_message.setText(R.string.connection_problems));
                        }
                        onSecretKeyChange = false;
                    }
                });
                thread.start();
            });
            builder.setNegativeButton(R.string.no, (dialogInterface, i) -> {
                Toast.makeText(this, getString(R.string.regeneration_of_secret_key_canceled), Toast.LENGTH_SHORT).show();
            });
            builder.create().show();
        });
    }
}
