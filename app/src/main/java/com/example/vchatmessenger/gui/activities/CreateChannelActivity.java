package com.example.vchatmessenger.gui.activities;

import static com.example.vchatmessenger.core.api.server.ChannelRestClient.createChannel;
import static com.example.vchatmessenger.core.api.server.ChannelRestClient.editAll;
import static com.example.vchatmessenger.core.api.server.ChannelRestClient.getById;
import static com.example.vchatmessenger.core.api.server.NicknameRestClient.checkNicknameForChannel;
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
import com.example.vchatmessenger.dto.Channel;
import com.example.vchatmessenger.dto.CreateChannelDto;
import com.example.vchatmessenger.gui.fragments.ChatViewFragment;
import com.github.drjacky.imagepicker.ImagePicker;
import com.github.drjacky.imagepicker.constant.ImageProvider;
import com.google.android.material.imageview.ShapeableImageView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

public class CreateChannelActivity extends AppCompatActivity {
    ImageButton buttonBack, buttonNext;
    ShapeableImageView chooseImageView;
    EditText nameOfChannel;
    EditText nicknameOfChannel;
    TextView errorMessageForChannelName;
    private static boolean imageSelected;
    private long id;
    byte[] imageData;
    Channel currentChat;
    private static boolean onCreating;

    public void back() {
        Bundle data;
        if (getIntent().getExtras() != null) {
            data = getIntent().getExtras();
        } else {
            data = new Bundle();
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && id > 0) {
            Intent intent = new Intent(CreateChannelActivity.this, GroupViewActivity.class);
            intent.putExtras(data);
            startActivity(intent);
        } else {
            Intent intent = new Intent(CreateChannelActivity.this, ChatViewActivity.class);
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
                    } else {
                        Toast.makeText(this, "Error no data", Toast.LENGTH_SHORT).show();
                    }
                } else if (result.getResultCode()== ImagePicker.RESULT_ERROR) {
                    Toast.makeText(this, "Error no data", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "fuck", Toast.LENGTH_SHORT).show();
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

    protected boolean checkNickname() {
        AtomicReference<Boolean> res = new AtomicReference<>(false);
        Thread t = new Thread(() -> {
            int nicknameResult = checkNicknameForChannel(nicknameOfChannel.getText().toString());
            if (nicknameResult == 200) {
                runOnUiThread(() -> errorMessageForChannelName.setText(""));
                res.set(true);
            } else if (nicknameResult == 400) {
                runOnUiThread(() -> errorMessageForChannelName.setText(R.string.error_message_for_nickname));
            } else if (nicknameResult == 500) {
                if (currentChat != null && currentChat.getNickname().equals(nicknameOfChannel.getText().toString())) {
                    res.set(true);
                    runOnUiThread(() -> errorMessageForChannelName.setText(""));
                } else {
                    runOnUiThread(() -> errorMessageForChannelName.setText(R.string.nickname_is_taken));
                }
            } else {
                runOnUiThread(() -> errorMessageForChannelName.setText(R.string.connection_problems));
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

    protected boolean checkName() {
        AtomicReference<Boolean> res = new AtomicReference<>(false);
        Thread t = new Thread(() -> {
            int nameResult = NameRestClient.checkName(nameOfChannel.getText().toString());
            if (nameResult == 200) {
                runOnUiThread(() -> errorMessageForChannelName.setText(""));
                res.set(true);
            } else if (nameResult == -1) {
                runOnUiThread(() -> errorMessageForChannelName.setText(R.string.connection_problems));
            } else {
                runOnUiThread(() -> errorMessageForChannelName.setText(R.string.error_message_for_name));
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


    public boolean checkAll() {
        AtomicReference<Boolean> res = new AtomicReference<>(false);
        Thread t = new Thread(() -> {
            boolean result = checkName();
            if (result) {
                result = checkNickname();
            }
            res.set(result);
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_channel_activity);
        // добывает данные:
        if (getIntent().getExtras() != null) {
            id = getIntent().getExtras().getLong("chatId");
        }
        imageSelected = false;
        buttonBack = findViewById(R.id.buttonBack);
        buttonNext = findViewById(R.id.buttonNext);
        chooseImageView = findViewById(R.id.chooseImageView);
        nameOfChannel = findViewById(R.id.nameOfChannel);
        nicknameOfChannel = findViewById(R.id.nicknameOfChannel);
        errorMessageForChannelName = findViewById(R.id.errorMessageForChannelName);
        // первая проверка
        checkAll();
        // ставим listener_ы
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
        buttonBack.setOnClickListener(v -> finish());
        nameOfChannel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                new Thread(() -> checkAll()).start();
            }
        });
        nicknameOfChannel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                new Thread(() -> checkAll()).start();
            }
        });
        buttonNext.setOnClickListener(v -> {
            if (errorMessageForChannelName.getText().equals("") && !onCreating) {
                onCreating = true;
                ArrayList<String> authData = getAuthData(getApplicationContext());
                int typeOfImage = 1;
                if (!imageSelected) {
                    if (currentChat == null || currentChat.getTypeOfImage() == 1) {
                        Bitmap bitmap = getGeneratedBitmap(nameOfChannel.getText().toString());
                        imageData = getBytesOfBitmap(bitmap);
                    } else {
                        imageData = android.util.Base64.decode(currentChat.getImageData(), android.util.Base64.DEFAULT);
                    }
                    typeOfImage = 2;
                }
                if (id > 0) {
                    int finalTypeOfImage = typeOfImage;
                    new Thread(() -> {
                        if (currentChat.getName().equals(nameOfChannel.getText().toString()) &&
                            currentChat.getNickname().equals(nicknameOfChannel.getText().toString()) &&
                            Base64.getEncoder().encodeToString(imageData).equals(currentChat.getImageData())
                        ) {
                            back();
                        } else {
                            Channel channel = editAll(authData.get(0), authData.get(1), id, nameOfChannel.getText().toString(), nicknameOfChannel.getText().toString(), finalTypeOfImage,  Base64.getEncoder().encodeToString(imageData));
                            if (channel != null) {
                                back();
                            } else {
                                runOnUiThread(() -> Toast.makeText(this, getString(R.string.connection_problems), Toast.LENGTH_SHORT).show());
                            }
                        }
                        onCreating = false;
                    }).start();
                } else {
                    CreateChannelDto createChannelDto = new CreateChannelDto();
                    createChannelDto.setName(nameOfChannel.getText().toString());
                    createChannelDto.setNickname(nicknameOfChannel.getText().toString());
                    createChannelDto.setImageData(Base64.getEncoder().encodeToString(imageData));
                    createChannelDto.setTypeOfImage(typeOfImage);
                    createChannelDto.setMessagesIds(new ArrayList<>());
                    createChannelDto.setMembersIds(new ArrayList<>());
                    createChannelDto.setUnreadMsgCount(0L);
                    Thread t = new Thread(() -> {
                        Channel channelResult = createChannel(authData.get(0), authData.get(1), createChannelDto);
                        if (channelResult != null) {
                            ChatViewFragment.scrollToChat = 0;
                            runOnUiThread(() -> ChatViewFragment.updateListOfChats(getApplicationContext()));
                            Bundle data = new Bundle();
                            data.putLong("chatId", channelResult.getId());
                            data.putString("nameOfChat", channelResult.getName());
                            data.putLong("userId", channelResult.getOwnerId());
                            data.putInt("chatType", channelResult.getType());
                            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                Intent intent = new Intent(CreateChannelActivity.this, GroupViewActivity.class);
                                intent.putExtras(data);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(CreateChannelActivity.this, ChatViewActivity.class);
                                intent.putExtras(data);
                                startActivity(intent);
                            }
                        } else {
                            runOnUiThread(() -> Toast.makeText(this, getString(R.string.connection_problems), Toast.LENGTH_SHORT).show());
                        }
                        onCreating = false;
                    });
                    t.start();
                }
            } else {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        });

        if (id > 0) {
            new Thread(() -> {
                ArrayList<String> authData = getAuthData(getApplicationContext());
                currentChat = getById(authData.get(0), authData.get(1), id);
                if (currentChat != null) {
                    nameOfChannel.post(() -> nameOfChannel.setText(currentChat.getName()));
                    nicknameOfChannel.post(() -> nicknameOfChannel.setText(currentChat.getNickname()));
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
