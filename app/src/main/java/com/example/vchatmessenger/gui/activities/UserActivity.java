package com.example.vchatmessenger.gui.activities;

import static com.example.vchatmessenger.core.api.server.UserRestClient.changeImageData;
import static com.example.vchatmessenger.core.api.server.UserRestClient.deleteUser;
import static com.example.vchatmessenger.core.api.server.UserRestClient.get;
import static com.example.vchatmessenger.core.api.server.UserRestClient.setTypeOfImage;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.logout;
import static com.example.vchatmessenger.core.constants.Constants.getBytesOfBitmap;
import static com.example.vchatmessenger.core.constants.Constants.getBytesOfUri;
import static com.example.vchatmessenger.core.constants.Constants.getGeneratedBitmap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.dto.User;
import com.example.vchatmessenger.gui.fragments.UserActivityNameViewFragment;
import com.example.vchatmessenger.gui.fragments.UserActivityNicknameChangeFragment;
import com.example.vchatmessenger.gui.fragments.UserActivityNicknameViewFragment;
import com.example.vchatmessenger.gui.fragments.UserActivtyNameChangeFragment;
import com.github.drjacky.imagepicker.ImagePicker;
import com.github.drjacky.imagepicker.constant.ImageProvider;
import com.google.android.material.imageview.ShapeableImageView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

public class UserActivity extends AppCompatActivity {

    private static boolean nameOnChange;
    private static boolean nicknameOnChange;
    ShapeableImageView userImage;
    Button  buttonChangePassword;
    Button buttonChangeSecretKey;
    Button buttonLogout;
    Button deleteButton;
    ImageButton buttonBack;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private static boolean onPasswordChange;
    Vibrator vibrator;


    public static boolean isNameOnChange() {
        return nameOnChange;
    }

    public static void setNameOnChange(boolean nameOnChange) {
        UserActivity.nameOnChange = nameOnChange;
    }

    public static boolean isNicknameOnChange() {
        return nicknameOnChange;
    }

    public static void setNicknameOnChange(boolean nicknameOnChange) {
        UserActivity.nicknameOnChange = nicknameOnChange;
    }

    public void back() {
        nameOnChange = false;
        nicknameOnChange = false;
        UserActivtyNameChangeFragment.setChangedName("");
        UserActivityNicknameChangeFragment.setChangedNickname("");
        finish();
    }

    public void changeImage(String imageData) {
        ArrayList<String> authData = getAuthData(getApplicationContext());
        int imageResult = changeImageData(authData.get(0), authData.get(1), imageData);
        if (imageResult == 1) {
            Thread t = new Thread(this::setImage);
            t.start();
        } else {
            runOnUiThread(() -> Toast.makeText(UserActivity.this, getString(R.string.connection_problems), Toast.LENGTH_SHORT).show());
        }
    }

    public void setImage() {
        ArrayList<String> authData = getAuthData(getApplicationContext());
        User userResult = get(authData.get(0), authData.get(1));
        if (userResult != null) {
            String imageData = userResult.getImageData();
            byte[] data = Base64.getDecoder().decode(imageData);
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            runOnUiThread(() -> userImage.setImageBitmap(bitmap));
        } else {
            runOnUiThread(() -> Toast.makeText(UserActivity.this, getString(R.string.connection_problems), Toast.LENGTH_SHORT).show());
        }
    }

    ActivityResultLauncher<Intent> launcher=
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),(ActivityResult result)->{
                if(result.getResultCode()== Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        try {
                            ArrayList<String> authData = getAuthData(getApplicationContext());
                            String imageData = Base64.getEncoder().encodeToString(getBytesOfUri(result.getData().getData(), getContentResolver()));
                            Thread t = new Thread(() -> {
                                changeImage(imageData);
                                setTypeOfImage(authData.get(0), authData.get(1), 1);
                            });
                            t.start();
                        } catch (IOException ignored) {}
                    } else {
                        Toast.makeText(this, "Error no data", Toast.LENGTH_SHORT).show();
                    }
                    // Use the uri to load the image
                } else {
                    Toast.makeText(this, "Error no data", Toast.LENGTH_SHORT).show();
                    // Use ImagePicker.Companion.getError(result.getData()) to show an error
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);
        userImage = findViewById(R.id.user_image);
        buttonChangePassword = findViewById(R.id.password_btn);
        buttonChangeSecretKey = findViewById(R.id.secret_key_btn);
        buttonLogout = findViewById(R.id.logout_btn);
        buttonBack = findViewById(R.id.buttonBack);
        deleteButton = findViewById(R.id.delete_button);
        Thread t = new Thread(this::setImage);
        t.start();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        buttonBack.setOnClickListener(v -> {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            back();
        });
        userImage.setOnClickListener(view -> {
            ImagePicker.Companion.with(this).provider(ImageProvider.BOTH).crop().cropSquare().cropOval().maxResultSize(512, 512, true).createIntentFromDialog(
                    new Function1(){
                        public Object invoke(Object var1){
                            this.invoke((Intent)var1);
                            return Unit.INSTANCE;
                        }
                        public void invoke(@NotNull Intent it){
                            Intrinsics.checkNotNullParameter(it,"it");
                            launcher.launch(it);
                        }
                    }
            );
        });
        userImage.setOnLongClickListener(v -> {
            AtomicReference<Integer> res = new AtomicReference<>(0);
            ArrayList<String> authData = getAuthData(getApplicationContext());
            Thread thread = new Thread(() -> res.set(get(authData.get(0), authData.get(1)).getTypeOfImage()));
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (res.get() == 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.delete_ava));
                builder.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    Thread thread1 = new Thread(() -> {
                        User userResult = get(authData.get(0), authData.get(1));
                        if (userResult != null) {
                            String data = Base64.getEncoder().encodeToString(getBytesOfBitmap(getGeneratedBitmap(userResult.getName())));
                            changeImage(data);
                            setTypeOfImage(authData.get(0), authData.get(1), 2);
                            setImage();
                        } else {
                            Toast.makeText(UserActivity.this, getString(R.string.connection_problems), Toast.LENGTH_SHORT).show();
                        }
                    });
                    thread1.start();
                });
                builder.setNegativeButton(R.string.no, (dialogInterface, i) -> {
                    // ничего не делаем, диалог закроется автоматически
                });
                builder.create().show();
                return true;
            }
            return false;
        });
        // заполняем фейковыми данными:
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (!isNameOnChange()) {
            ft.replace(R.id.name_fragment_placeholder, new UserActivityNameViewFragment());
        } else {
            ft.replace(R.id.name_fragment_placeholder, new UserActivtyNameChangeFragment());
        }
        if (!isNicknameOnChange()) {
            ft.replace(R.id.nickname_fragment_placeholder, new UserActivityNicknameViewFragment());
        } else {
            ft.replace(R.id.nickname_fragment_placeholder, new UserActivityNicknameChangeFragment());
        }
        ft.commit();
        // биометиричесакя аунтефикация
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(UserActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                                getString(R.string.authentication_error), Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Intent intent;
                if (onPasswordChange) {
                    intent = new Intent(UserActivity.this, PasswordCreationActivity.class);
                    intent.putExtra("launchMode", 3);
                } else {
                    intent = new Intent(UserActivity.this, ChangeSecretKeyActivity.class);
                }
                startActivity(intent);
                onPasswordChange = false;
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), getString(R.string.authentication_failed),
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.fingerprint_hint))
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build();
        } else {
            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.fingerprint_hint))
                    .setDeviceCredentialAllowed(true)
                    .build();
        }

        BiometricManager biometricManager = BiometricManager.from(this);
        int res = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG
                | BiometricManager.Authenticators.BIOMETRIC_WEAK
                | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        buttonChangePassword.setOnClickListener(v -> {
            onPasswordChange = true;
            if (res == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ||
                    res == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ||
                    res == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                Intent intent = new Intent(UserActivity.this, PasswordCreationActivity.class);
                intent.putExtra("launchMode", 3);
                // сразу запускаем активность
            } else {
                biometricPrompt.authenticate(promptInfo);
            }
        });

        buttonChangeSecretKey.setOnClickListener(v -> {
            if (res == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ||
                    res == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ||
                    res == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                Intent intent = new Intent(UserActivity.this, ChangeSecretKeyActivity.class);
                startActivity(intent);
            } else {
                biometricPrompt.authenticate(promptInfo);
            }
        });

        buttonLogout.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.logout_confirmation));
            builder.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                logout(getApplicationContext());
                Intent intent = new Intent(UserActivity.this, WelcomeActivity.class);
                startActivity(intent);
            });
            builder.setNegativeButton(R.string.no, (dialogInterface, i) -> {
                // ничего не делаем, диалог закроется автоматически
            });
            builder.create().show();
        });
        deleteButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.account_deleting_confirmation));
            builder.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                Thread tread = new Thread(() -> {
                    ArrayList<String> authData = getAuthData(getApplicationContext());
                    int deleteCode = deleteUser(authData.get(0), authData.get(1));
                    if (deleteCode == 1) {
                        logout(getApplicationContext());
                        Intent intent = new Intent(UserActivity.this, WelcomeActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                        getString(R.string.authentication_error), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
                tread.start();
            });
            builder.setNegativeButton(R.string.no, (dialogInterface, i) -> {
                // ничего не делаем, диалог закроется автоматически
            });
            builder.create().show();
        });
    }

    @Override
    public void onBackPressed() {
        back();
    }
}
