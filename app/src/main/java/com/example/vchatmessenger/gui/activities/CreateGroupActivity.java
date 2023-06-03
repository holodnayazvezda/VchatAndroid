package com.example.vchatmessenger.gui.activities;

import static com.example.vchatmessenger.core.api.server.GroupRestClient.createGroup;
import static com.example.vchatmessenger.core.api.server.GroupRestClient.editAll;
import static com.example.vchatmessenger.core.api.server.GroupRestClient.getChat;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;
import static com.example.vchatmessenger.core.constants.Constants.getBytesOfBitmap;
import static com.example.vchatmessenger.core.constants.Constants.getBytesOfUri;
import static com.example.vchatmessenger.core.constants.Constants.getGeneratedBitmap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.core.api.server.NameRestClient;
import com.example.vchatmessenger.dto.CreateGroupDto;
import com.example.vchatmessenger.dto.Group;
import com.example.vchatmessenger.gui.fragments.ChatViewFragment;
import com.github.drjacky.imagepicker.ImagePicker;
import com.github.drjacky.imagepicker.constant.ImageProvider;
import com.google.android.material.imageview.ShapeableImageView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

public class CreateGroupActivity extends AppCompatActivity {
    ImageButton buttonBack, buttonNext;
    ShapeableImageView chooseImageView;
    EditText nameOfGroup;
    TextView errorMessageForGroupName;
    private static boolean imageSelected;
    private long id;
    byte[] imageData;
    private static boolean onCreating;
    private Group currentChat;

    public void back() {
        Bundle data;
        if (getIntent().getExtras() != null) {
            data = getIntent().getExtras();
        } else {
            data = new Bundle();
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && id > 0) {
            Intent intent = new Intent(CreateGroupActivity.this, GroupViewActivity.class);
            intent.putExtras(data);
            startActivity(intent);
        } else {
            Intent intent = new Intent(CreateGroupActivity.this, ChatViewActivity.class);
            intent.putExtras(data);
            startActivity(intent);
        }
    }

    ActivityResultLauncher<Intent> launcher=
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),(ActivityResult result)->{
                if(result.getResultCode()== Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        Uri uri = result.getData().getData();
                        try {
                            imageData = getBytesOfUri(result.getData().getData(), getContentResolver());
                        } catch (IOException e) {
                            imageData = getBytesOfBitmap(getGeneratedBitmap(getIntent().getExtras().getString("name")));
                        }
                        chooseImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        chooseImageView.setImageURI(uri);
                        imageSelected = true;
                    }
                }
            });

    private void callImagePicker() {
        ImagePicker.Companion.with(this).provider(ImageProvider.BOTH).crop().cropSquare().cropOval().createIntentFromDialog(
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

    public void checkName() {
        Thread t = new Thread(() -> {
            int nameResult = NameRestClient.checkName(nameOfGroup.getText().toString());
            if (nameResult == 200) {
                runOnUiThread(() -> errorMessageForGroupName.setText(""));
            } else if (nameResult == -1) {
                runOnUiThread(() -> errorMessageForGroupName.setText(R.string.connection_problems));
            } else {
                runOnUiThread(() -> errorMessageForGroupName.setText(R.string.error_message_for_name));
            }
        });
        t.start();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group_activity);
        // добывает данные:
        if (getIntent().getExtras() != null) {
            id = getIntent().getExtras().getLong("chatId");
        }
        imageSelected = false;
        buttonBack = findViewById(R.id.buttonBack);
        buttonNext = findViewById(R.id.buttonNext);
        chooseImageView = findViewById(R.id.chooseImageView);
        runOnUiThread(() -> nameOfGroup = findViewById(R.id.nameOfGroup));
        errorMessageForGroupName = findViewById(R.id.errorMessageForGroupName);
        checkName();
        // стаивим listener_ы
        buttonBack.setOnClickListener(v -> finish());
        nameOfGroup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                checkName();
            }
        });
        chooseImageView.setOnClickListener(v -> callImagePicker());
        chooseImageView.setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.delete_ava));
            builder.setPositiveButton(R.string.yes, (dialog, id) -> {
                chooseImageView.setImageDrawable(getDrawable(R.drawable.camera));
                chooseImageView.setScaleType(ImageView.ScaleType.CENTER);
                imageSelected = false;
            });
            builder.setNegativeButton(R.string.no, (dialog, id) -> {
                // ничего не делаем, диалог закроется атоматически
            });
            if (imageSelected) {
                builder.create().show();
            }
            return true;
        });
        buttonNext.setOnClickListener(v -> {
            if (errorMessageForGroupName.getText().equals("") && !onCreating) {
                onCreating = true;
                ArrayList<String> authData = getAuthData(getApplicationContext());
                int typeOfImage = 1;
                if (!imageSelected) {
                    if (currentChat == null || currentChat.getTypeOfImage() == 1) {
                        Bitmap bitmap = getGeneratedBitmap(nameOfGroup.getText().toString());
                        imageData = getBytesOfBitmap(bitmap);
                    } else {
                        imageData = android.util.Base64.decode(currentChat.getImageData(), android.util.Base64.DEFAULT);
                    }
                    typeOfImage = 2;
                }
                if (id > 0) {
                    int finalTypeOfImage = typeOfImage;
                    new Thread(() -> {
                        if (
                            currentChat.getName().equals(nameOfGroup.getText().toString()) &&
                            Base64.getEncoder().encodeToString(imageData).equals(currentChat.getImageData())
                        ) {
                            back();
                        } else {
                            Group groupResult = editAll(authData.get(0), authData.get(1), id, nameOfGroup.getText().toString(), finalTypeOfImage, Base64.getEncoder().encodeToString(imageData));
                            if (groupResult != null) {
                                back();
                            } else {
                                runOnUiThread(() -> Toast.makeText(this, getString(R.string.connection_problems), Toast.LENGTH_SHORT).show());
                            }
                        }
                        onCreating = false;
                    }).start();
                } else {
                    CreateGroupDto createGroupDto = new CreateGroupDto();
                    createGroupDto.setName(nameOfGroup.getText().toString());
                    createGroupDto.setImageData(Base64.getEncoder().encodeToString(imageData));
                    createGroupDto.setTypeOfImage(typeOfImage);
                    createGroupDto.setMessagesIds(new ArrayList<>());
                    createGroupDto.setMembersIds(new ArrayList<>());
                    createGroupDto.setUnreadMsgCount(0L);
                    new Thread(() -> {
                        Group groupResult = createGroup(authData.get(0), authData.get(1), createGroupDto);
                        if (groupResult != null) {
                            ChatViewFragment.scrollToChat = 0;
                            runOnUiThread(() -> ChatViewFragment.updateListOfChats(getApplicationContext()));
                            Bundle data = new Bundle();
                            data.putLong("chatId", groupResult.getId());
                            data.putString("nameOfChat", groupResult.getName());
                            data.putLong("userId", groupResult.getOwnerId());
                            data.putInt("chatType", groupResult.getType());
                            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                Intent intent = new Intent(CreateGroupActivity.this, GroupViewActivity.class);
                                intent.putExtras(data);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(CreateGroupActivity.this, ChatViewActivity.class);
                                intent.putExtras(data);
                                startActivity(intent);
                            }
                        } else {
                            runOnUiThread(() -> Toast.makeText(this, getString(R.string.connection_problems), Toast.LENGTH_SHORT).show());
                        }
                        onCreating = false;
                    }).start();
                }
            } else {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        });
        if (id > 0) {
            new Thread(() -> {
                ArrayList<String> authData = getAuthData(getApplicationContext());
                currentChat = getChat(authData.get(0), authData.get(1), id);
                if (currentChat != null) {
                    nameOfGroup.post(() -> nameOfGroup.setText(currentChat.getName()));
                    if (currentChat.getTypeOfImage() == 1) {
                        imageData = android.util.Base64.decode(currentChat.getImageData(), android.util.Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                        chooseImageView.post(() -> {
                            chooseImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            chooseImageView.setImageBitmap(bitmap);
                        });
                        imageSelected = true;
                    } else {
                        chooseImageView.post(() -> chooseImageView.setScaleType(ImageView.ScaleType.CENTER));
                    }
                }
            }).start();
        }
    }

    @Override
    public void onBackPressed() {
        back();
    }
}