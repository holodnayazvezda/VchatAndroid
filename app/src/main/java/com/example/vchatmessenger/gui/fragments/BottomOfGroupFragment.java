package com.example.vchatmessenger.gui.fragments;

import static com.example.vchatmessenger.core.api.server.MessageRestClient.createMessage;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.dto.CreateMessageDto;
import com.example.vchatmessenger.dto.Message;

import java.util.ArrayList;

public class BottomOfGroupFragment extends Fragment {
    View contentView;
    EditText messageToSend;
    ImageButton sendButton;
    long id;
    private static boolean onSending;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.bottom_of_group_fragment, container, false);
        sendButton = contentView.findViewById(R.id.send_button);
        messageToSend = contentView.findViewById(R.id.message_to_send);
        assert getArguments() != null;
        id = getArguments().getLong("chatId");
        if (id > 0) {
            sendButton.setOnClickListener(v -> {
                if (messageToSend.getText().toString().length() > 0 && !onSending) {
                    onSending = true;
                    ArrayList<String> authData = getAuthData(requireContext());
                    CreateMessageDto createMessageDto = new CreateMessageDto();
                    createMessageDto.setContent(messageToSend.getText().toString());
                    createMessageDto.setMessageChatId(id);
                    new Thread(() -> {
                        Message messageResult = createMessage(authData.get(0), authData.get(1), createMessageDto);
                        if (messageResult != null) {
                            messageToSend.post(() -> messageToSend.setText(""));
                        }
                        onSending = false;
                    }).start();
                }
            });
        }
        return contentView;
    }
}
