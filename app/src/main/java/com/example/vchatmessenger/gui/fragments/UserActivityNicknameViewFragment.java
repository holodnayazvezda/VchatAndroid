package com.example.vchatmessenger.gui.fragments;

import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.gui.activities.UserActivity;

public class UserActivityNicknameViewFragment extends Fragment {

    View contentView;
    TextView nicknameText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.user_activity_nickname_view_fragment, container, false);
        nicknameText = contentView.findViewById(R.id.nickname_text);
        nicknameText.setText(getAuthData(requireActivity().getApplicationContext()).get(0));
        contentView.setOnClickListener(v -> {
            UserActivity.setNicknameOnChange(true);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nickname_fragment_placeholder, new UserActivityNicknameChangeFragment()).commit();
        });
        return contentView;
    }
}
