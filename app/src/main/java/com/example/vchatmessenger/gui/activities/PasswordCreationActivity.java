package com.example.vchatmessenger.gui.activities;

import static com.example.vchatmessenger.core.api.server.PasswordRestClient.checkPassword;
import static com.example.vchatmessenger.core.api.server.UserRestClient.changePassword;
import static com.example.vchatmessenger.core.api.server.UserRestClient.changePasswordBySecretKeys;
import static com.example.vchatmessenger.core.api.server.UserRestClient.get;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.setAuthData;
import static com.example.vchatmessenger.core.constants.Constants.contentError;
import static com.example.vchatmessenger.core.constants.Constants.lengthError;
import static com.example.vchatmessenger.core.constants.Constants.matchError;
import static com.example.vchatmessenger.core.constants.Constants.noLowercaseLetter;
import static com.example.vchatmessenger.core.constants.Constants.noNumberError;
import static com.example.vchatmessenger.core.constants.Constants.noSpecialSymbolError;
import static com.example.vchatmessenger.core.constants.Constants.noUppercaseLetter;
import static com.example.vchatmessenger.core.constants.Constants.ok;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.dto.User;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class PasswordCreationActivity extends AppCompatActivity {

    TextView password_condition_text;
    TextView password_confirmation_text;
    EditText password;
    EditText password_confirmation;
    Button button_next;
    ImageButton button_back;

    int launchMode;

    protected boolean check_password() {
        AtomicReference<Boolean> res = new AtomicReference<>(false);
        Thread t = new Thread(() -> {
            int passwordResult = checkPassword(password.getText().toString(),
                    password_confirmation.getText().toString());
            if (passwordResult == ok) {
                runOnUiThread(() -> {
                    password_condition_text.setText("");
                    password_confirmation_text.setText("");
                });
                res.set(true);
            } else if (passwordResult == lengthError) {
                runOnUiThread(() -> {
                    password_condition_text.setText(R.string.length_password_error);
                    password_confirmation_text.setText("");
                });
            } else if (passwordResult == noNumberError) {
                runOnUiThread(() -> {
                    password_condition_text.setText(R.string.password_number_error);
                    password_confirmation_text.setText("");
                });
            } else if (passwordResult == noLowercaseLetter) {
                runOnUiThread(() -> {
                    password_condition_text.setText(R.string.password_lowercase_error);
                    password_confirmation_text.setText("");
                });
            } else if (passwordResult == noUppercaseLetter) {
                runOnUiThread(() -> {
                    password_condition_text.setText(R.string.password_uppercase_error);
                    password_confirmation_text.setText("");
                });
            } else if (passwordResult == noSpecialSymbolError) {
                runOnUiThread(() -> {
                    password_condition_text.setText(R.string.special_symbols_error);
                    password_confirmation_text.setText("");
                });
            } else if (passwordResult == contentError) {
                runOnUiThread(() -> {
                    password_condition_text.setText(R.string.passwords_trash_error);
                    password_confirmation_text.setText("");
                });
            } else if (passwordResult == matchError) {
                runOnUiThread(() -> {
                    password_condition_text.setText("");
                    password_confirmation_text.setText(R.string.password_confirmation);
                });
            } else {
                runOnUiThread(() -> {
                    password_confirmation_text.setText(R.string.connection_problems);
                    password_condition_text.setText("");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_creation);

        launchMode = getIntent().getIntExtra("launchMode", 1);

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        password_condition_text = findViewById(R.id.password_condition_hint);
        password_confirmation_text = findViewById(R.id.password_confirmation);
        password = findViewById(R.id.password_input);
        password_confirmation = findViewById(R.id.password_confirmation_input);
        button_next = findViewById(R.id.buttonNext);
        button_back = findViewById(R.id.buttonBack);
        button_back.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            finish();
        });
        button_next.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            if (check_password()) {
                if (launchMode == 1) {
                    Intent intent = new Intent(PasswordCreationActivity.this, SelectImageActivity.class);
                    Bundle b = getIntent().getExtras();
                    b.putString("password", password.getText().toString());
                    intent.putExtras(b);
                    startActivity(intent);
                } else if (launchMode == 2) {
                    // todo: set PASSWORD
                    Thread t = new Thread(() -> {
                        ArrayList<String> authData = getAuthData(getApplicationContext());
                        int changePasswordResult = changePasswordBySecretKeys(
                                getIntent().getStringExtra("nickname"),
                                getIntent().getIntExtra("a", 0),
                                getIntent().getStringExtra("a_value"),
                                getIntent().getIntExtra("b", 0),
                                getIntent().getStringExtra("b_value"),
                                getIntent().getIntExtra("c", 0),
                                getIntent().getStringExtra("c_value"),
                                password.getText().toString()
                        );
                        if (changePasswordResult == 1) {
                            runOnUiThread(() -> {
                                password_confirmation_text.setText("");
                                password_condition_text.setText("");
                            });
                            User user = get(authData.get(0), password.getText().toString());
                            setAuthData(
                                    getApplicationContext(),
                                    authData.get(0),
                                    password.getText().toString(),
                                    user.getId()
                            );
                            Intent intent = new Intent(PasswordCreationActivity.this, ChatViewActivity.class);
                            startActivity(intent);
                        } else {
                            runOnUiThread(() -> {
                                password_confirmation_text.setText(R.string.connection_problems);
                                password_condition_text.setText("");
                            });
                        }
                    });
                    t.start();
                } else {
                    Thread t = new Thread(() -> {
                        ArrayList<String> authData = getAuthData(getApplicationContext());
                        int changePasswordResult = changePassword(
                                authData.get(0),
                                authData.get(1),
                                password.getText().toString());
                        if (changePasswordResult == 1) {
                            runOnUiThread(() -> {
                                password_confirmation_text.setText("");
                                password_condition_text.setText("");
                                Toast.makeText(this, getString(R.string.password_was_changed), Toast.LENGTH_SHORT).show();
                            });
                            User user = get(authData.get(0), password.getText().toString());
                            setAuthData(
                                    getApplicationContext(),
                                    authData.get(0),
                                    password.getText().toString(),
                                    user.getId()
                            );
                            finish();
                        }  else if (changePasswordResult == 0) {
                            runOnUiThread(() -> {
                                password_confirmation_text.setText(R.string.wrong_password);
                                password_condition_text.setText("");
                            });
                        } else {
                            runOnUiThread(() -> {
                                password_confirmation_text.setText(R.string.connection_problems);
                                password_condition_text.setText("");
                            });
                        }
                    });
                    t.start();
                }
            }
        });
    }
}

