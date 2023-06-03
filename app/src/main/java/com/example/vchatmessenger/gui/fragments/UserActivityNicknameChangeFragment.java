package com.example.vchatmessenger.gui.fragments;

import static com.example.vchatmessenger.core.api.server.NicknameRestClient.checkNicknameForUser;
import static com.example.vchatmessenger.core.api.server.UserRestClient.changeNickname;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.setAuthData;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.gui.activities.UserActivity;
import com.example.vchatmessenger.core.shared_preferences.AuthWorker;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class UserActivityNicknameChangeFragment extends Fragment {

    View contentView;
    EditText newNicknameText;
    TextView errorMessageForNickname;
    ImageButton save;
    ImageButton cancel;
    private static String changedNickname;

    public static String getChangedNickname() {
        return changedNickname;
    }

    public static void setChangedNickname(String changedNickname) {
        UserActivityNicknameChangeFragment.changedNickname = changedNickname;
    }

    public static boolean onNicknameChange = false;

    protected int checkUserNickname() {
        AtomicReference<Integer> res = new AtomicReference<>(-1);
        Thread t = new Thread(() -> res.set(checkNicknameForUser(newNicknameText.getText().toString())));
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return res.get();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.user_activity_nickname_change_fragment, container, false);
        newNicknameText = contentView.findViewById(R.id.new_nickname_text);
        errorMessageForNickname = contentView.findViewById(R.id.error_message_for_nickname);
        save = contentView.findViewById(R.id.save);
        cancel = contentView.findViewById(R.id.cancel);
        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        newNicknameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                setChangedNickname(newNicknameText.getText().toString());
                Thread t = new Thread(() -> {
                    int checkResult = checkUserNickname();
                    if (checkResult == 200) {
                        requireActivity().runOnUiThread(() -> errorMessageForNickname.setText(""));
                    } else if (checkResult == 400) {
                        requireActivity().runOnUiThread(() -> errorMessageForNickname.setText(R.string.error_message_for_nickname));
                    } else if (checkResult == 500) {
                        requireActivity().runOnUiThread(() -> errorMessageForNickname.setText(R.string.nickname_is_taken));
                    } else {
                        requireActivity().runOnUiThread(() -> errorMessageForNickname.setText(R.string.connection_problems));
                    }
                });
                t.start();
            }
        });
        cancel.setOnClickListener(v -> {
            UserActivity.setNicknameOnChange(false);
            setChangedNickname(null);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nickname_fragment_placeholder, new UserActivityNicknameViewFragment()).commit();
        });
        save.setOnClickListener(v -> {
            if (checkUserNickname() == 200) {
                Thread t = new Thread(() -> {
                    if (!onNicknameChange) {
                        onNicknameChange = true;
                        ArrayList<String> authData = getAuthData(requireActivity().getApplicationContext());
                        int nicknameChangeResult = changeNickname(authData.get(0), authData.get(1),
                                newNicknameText.getText().toString());
                        if (nicknameChangeResult == 1) {
                            setChangedNickname(null);
                            UserActivity.setNameOnChange(false);
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.nickname_changed), Toast.LENGTH_SHORT).show());
                            setAuthData(requireActivity().getApplicationContext(), newNicknameText.getText().toString(), authData.get(1), AuthWorker.getId(requireActivity().getApplicationContext()));
                            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nickname_fragment_placeholder, new UserActivityNicknameViewFragment()).commit();
                        } else {
                            requireActivity().runOnUiThread(() -> errorMessageForNickname.setText(R.string.connection_problems));
                        }
                        onNicknameChange = false;
                    }
                });
                t.start();
            }
        });
        newNicknameText.setText(getChangedNickname());
        newNicknameText.setSelection(getChangedNickname().length());
    }
}
