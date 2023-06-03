package com.example.vchatmessenger.gui.fragments;

import static com.example.vchatmessenger.core.api.server.UserRestClient.get;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;

import android.content.Context;
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
import com.example.vchatmessenger.dto.User;

import java.util.ArrayList;

public class UserActivityNameViewFragment extends Fragment {

    View contentView;
    TextView usernameText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.user_activity_name_view_fragment, container, false);
        usernameText = contentView.findViewById(R.id.username_text);
        contentView.setOnClickListener(v -> {
            UserActivity.setNameOnChange(true);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(
                    R.id.name_fragment_placeholder, new UserActivtyNameChangeFragment()
            ).commit();
        });
        return contentView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Thread t = new Thread(() -> {
            ArrayList<String> authData = getAuthData(context.getApplicationContext());
            User userResult = get(authData.get(0), authData.get(1));
            if (userResult != null) {
                ((UserActivity) context).runOnUiThread(() -> usernameText.setText(userResult.getName()));
            }
        });
        t.start();
    }
}
