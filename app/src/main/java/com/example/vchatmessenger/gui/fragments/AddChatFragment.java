package com.example.vchatmessenger.gui.fragments;

import static com.example.vchatmessenger.core.api.server.UserRestClient.addChat;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.vchatmessenger.gui.activities.ChatViewActivity;
import com.example.vchatmessenger.R;
import com.example.vchatmessenger.dto.User;

import java.util.ArrayList;

public class AddChatFragment extends Fragment {
    View contentView;
    TextView addText;
    Long chatId;
    Integer chatType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.add_chat_fragment_layout, container, false);
        addText = contentView.findViewById(R.id.add_text);
        assert getArguments() != null;
        chatId = getArguments().getLong("chatId");
        chatType = getArguments().getInt("chatType");
        if (chatType == 1) {
            addText.setText(R.string.join_group_text);
        } else {
            addText.setText(R.string.subscribe_to_channel_text);
        }
        addText.setOnClickListener(v -> new Thread(() -> {
            ArrayList<String> authData = getAuthData(requireActivity().getApplicationContext());
            User userResult = addChat(authData.get(0), authData.get(1), chatId);
            if (userResult != null) {
                if (chatType == 1) {
                    BottomOfGroupFragment bottomOfGroupFragment = new BottomOfGroupFragment();
                    bottomOfGroupFragment.setArguments(getArguments());
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireActivity(), getString(R.string.join_group_info_text), Toast.LENGTH_SHORT).show());
                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.empty_space_for_bottom, bottomOfGroupFragment).commit();
                } else {
                    BottomOfChannelFragment bottomOfChannelFragment = new BottomOfChannelFragment();
                    bottomOfChannelFragment.setArguments(getArguments());
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireActivity(), getString(R.string.subscribe_to_channel_info_text), Toast.LENGTH_SHORT).show());
                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.empty_space_for_bottom, bottomOfChannelFragment).commit();
                }
                ChatViewActivity.startSearchChat = false;
                ChatViewActivity.setNameOfSearchedChat("");
            }
        }).start());
        return contentView;
    }
}
