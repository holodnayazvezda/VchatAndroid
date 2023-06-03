package com.example.vchatmessenger.gui.fragments;

import static com.example.vchatmessenger.core.api.server.NameRestClient.checkName;
import static com.example.vchatmessenger.core.api.server.UserRestClient.changeName;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;

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

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class UserActivtyNameChangeFragment extends Fragment {

    View contentView;
    EditText newNameText;
    TextView errorMessageForName;
    ImageButton save;
    ImageButton cancel;

    private static String changedName;
    public static String getChangedName() {
        return changedName;
    }
    public static void setChangedName(String newName) {
        changedName = newName;
    }
    public static boolean onNameChange = false;

    protected int checkUserName() {
        AtomicReference<Integer> res = new AtomicReference<>(-1);
        Thread t = new Thread(() -> res.set(checkName(newNameText.getText().toString())));
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
        contentView = inflater.inflate(R.layout.user_activity_name_change_fragment, container, false);
        newNameText = contentView.findViewById(R.id.new_name_text);
        errorMessageForName = contentView.findViewById(R.id.errorMessageForGroupName);
        save = contentView.findViewById(R.id.save);
        cancel = contentView.findViewById(R.id.cancel);
        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        newNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                setChangedName(newNameText.getText().toString());
                Thread t = new Thread(() -> {
                    int checkResult = checkUserName();
                    if (checkResult == 200) {
                        requireActivity().runOnUiThread(() -> errorMessageForName.setText(""));
                    } else if (checkResult == 500) {
                        requireActivity().runOnUiThread(() -> errorMessageForName.setText(R.string.error_message_for_name));
                    } else {
                        requireActivity().runOnUiThread(() -> errorMessageForName.setText(R.string.connection_problems));
                    }
                });
                t.start();
            }
        });
        cancel.setOnClickListener(v -> {
            UserActivity.setNameOnChange(false);
            setChangedName(null);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.name_fragment_placeholder, new UserActivityNameViewFragment()).commit();
        });
        save.setOnClickListener(v -> {
            if (checkUserName() == 200) {
                Thread t = new Thread(() -> {
                    if (!onNameChange) {
                        onNameChange = true;
                        ArrayList<String> authData = getAuthData(requireActivity().getApplicationContext());
                        int nameChangeResult = changeName(authData.get(0), authData.get(1), newNameText.getText().toString());
                        if (nameChangeResult == 1) {
                            setChangedName(null);
                            UserActivity.setNameOnChange(false);
                            requireActivity().runOnUiThread(() -> errorMessageForName.setText(""));
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.name_changed), Toast.LENGTH_SHORT).show());
                            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.name_fragment_placeholder, new UserActivityNameViewFragment()).commit();
                        } else {
                            requireActivity().runOnUiThread(() -> errorMessageForName.setText(R.string.connection_problems));
                        }
                        onNameChange = false;
                    }
                });
                t.start();
            }
        });
        newNameText.setText(getChangedName());
        newNameText.setSelection(getChangedName().length());
    }
}
