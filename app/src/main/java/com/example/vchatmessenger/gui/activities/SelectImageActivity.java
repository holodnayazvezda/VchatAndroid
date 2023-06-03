package com.example.vchatmessenger.gui.activities;

import static com.example.vchatmessenger.core.constants.Constants.getBytesOfBitmap;
import static com.example.vchatmessenger.core.constants.Constants.getBytesOfUri;
import static com.example.vchatmessenger.core.constants.Constants.getGeneratedBitmap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vchatmessenger.R;
import com.github.drjacky.imagepicker.ImagePicker;
import com.github.drjacky.imagepicker.constant.ImageProvider;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

public class SelectImageActivity extends AppCompatActivity {
    ImageButton buttonBack;
    Button continueButton;
    Button skipButton;
    ImageView imageView;

    protected void launchNextActivity(byte[] imageData, int typeOfImage, boolean vibrate) {
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        Intent intent = new Intent(SelectImageActivity.this, SecretKeyActivity.class);
        Bundle bundle = getIntent().getExtras();
        bundle.putInt("type_of_image", typeOfImage);
        intent.putExtras(bundle);
        SecretKeyActivity.imageData = imageData;
        startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_your_image);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        buttonBack = findViewById(R.id.buttonBack);
        continueButton = findViewById(R.id.continue_button);
        skipButton = findViewById(R.id.skip_button);
        imageView = findViewById(R.id.ava);
        buttonBack.setOnClickListener(v -> {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            finish();
        });
        continueButton.setOnClickListener(v -> showPicDialog());
        skipButton.setOnClickListener(v -> {
            Bitmap bitmap = getGeneratedBitmap(getIntent().getExtras().getString("name"));
            launchNextActivity(getBytesOfBitmap(bitmap), 2, true);
        });
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void showPicDialog() {
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
    }


    ActivityResultLauncher<Intent> launcher=
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),(ActivityResult result)->{
                if(result.getResultCode()==RESULT_OK){
                    if (result.getData() != null) {
                        byte[] imageData;
                        try {
                            imageData = getBytesOfUri(result.getData().getData(), getContentResolver());
                        } catch (IOException e) {
                            imageData = getBytesOfBitmap(getGeneratedBitmap(getIntent().getExtras().getString("name")));
                        }
                        launchNextActivity(imageData, 1, false);
                    }
                }
            });
}