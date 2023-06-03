package com.example.vchatmessenger.gui.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.vchatmessenger.gui.activities.GroupViewActivity;
import com.example.vchatmessenger.R;
import com.example.vchatmessenger.gui.activities.ChatViewActivity;

public class SearchMessageFragment extends Fragment {
    ImageButton button_back;
    EditText text_of_message;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_search_message, container, false);
        button_back = contentView.findViewById(R.id.buttonBack);
        text_of_message = contentView.findViewById(R.id.text_of_message);
        button_back.setOnClickListener(v -> {
            FragmentManager fm = requireActivity().getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            GroupViewFragment groupViewFragment = new GroupViewFragment();
            groupViewFragment.setArguments(getArguments());
            ft.replace(R.id.empty_dialog_horizontal, groupViewFragment);
            ft.commit();
        });
        return contentView;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Bundle data = getArguments();
        data.putBoolean("start_search", true);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (getArguments().getInt("id") >= 0) {
                Intent intent = new Intent(requireActivity().getApplicationContext(), GroupViewActivity.class);
                intent.putExtras(data);
                startActivity(intent);
            }
        } else {
            if (getArguments().getInt("id") >= 0) {
                Intent intent = new Intent(requireActivity().getApplicationContext(), ChatViewActivity.class);
                intent.putExtras(data);
                startActivity(intent);
            }
        }
    }
}
