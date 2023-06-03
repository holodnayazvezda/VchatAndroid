package com.example.vchatmessenger.gui.fragments;

import static com.example.vchatmessenger.gui.fragments.MiddleOfGroupViewFragment.list_of_messages;
import static com.example.vchatmessenger.core.api.server.GroupRestClient.getChat;
import static com.example.vchatmessenger.core.api.server.MessageRestClient.getPositionsOfFoundMessages;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.dto.Group;
import com.example.vchatmessenger.gui.activities.ChatViewActivity;
import com.example.vchatmessenger.gui.activities.GroupViewActivity;
import com.example.vchatmessenger.gui.recyclerviews.GroupRecyclerAdapter;
import com.example.vchatmessenger.core.interfaces.IOnBackPressed;

import java.util.ArrayList;
import java.util.List;

public class TopOfSearchMessageFragment extends Fragment implements IOnBackPressed {

    View contentView;
    ImageButton button_back;
    public static EditText message_text;
    Button scroll_up;
    Button scroll_down;
    long id;
    private static List<Integer> messagesPositions;
    public static int currentPosition;

    public void scroll(int position) {
        GroupRecyclerAdapter.onSearch = true;
        MiddleOfGroupViewFragment.searchedText = message_text.getText().toString();
        if (position >= 0 && messagesPositions.size() > 0) {
            try {
                requireActivity().runOnUiThread(() -> MiddleOfGroupViewFragment.scrollToPosition(position, requireActivity()));
            } catch (Exception ignored) {}
        }
        Bundle data = new Bundle();
        data.putInt("position", messagesPositions.size() - currentPosition - 1);
        data.putInt("amount_of_overlaps", messagesPositions.size());
        BottomOfMessageSearchFragment bottomOfMessageSearchFragment = new BottomOfMessageSearchFragment();;
        bottomOfMessageSearchFragment.setArguments(data);
        try {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.empty_space_for_bottom, bottomOfMessageSearchFragment).commit();
        } catch (Exception ignored) {}
    }

    private void back() {
        GroupRecyclerAdapter.onSearch = false;
        Bundle data;
        if (getArguments() != null) {
            data = getArguments();
        } else {
            data = new Bundle();
        }
        data.putString("text", null);
        data.putBoolean("search", false);
        data.putInt("scrollTo", ((LinearLayoutManager) list_of_messages.getLayoutManager()).findFirstVisibleItemPosition());
        TopOfGroupViewFragment topOfGroupViewFragment = new TopOfGroupViewFragment();
        topOfGroupViewFragment.setArguments(data);
        MiddleOfGroupViewFragment middleOfGroupViewFragment = new MiddleOfGroupViewFragment();
        middleOfGroupViewFragment.setArguments(data);
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.empty_space_for_middle, middleOfGroupViewFragment).commit();
        new Thread(() -> {
            if (data.getInt("type") == 1) {
                BottomOfGroupFragment bottomOfGroupFragment = new BottomOfGroupFragment();
                bottomOfGroupFragment.setArguments(data);
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.empty_space_for_bottom, bottomOfGroupFragment).commit();
            } else {
                ArrayList<String> authData = getAuthData(requireActivity().getApplicationContext());
                Group chat = getChat(authData.get(0), authData.get(1), id);
                if (chat.getOwnerId().equals(getArguments().getLong("userId"))) {
                    BottomOfGroupFragment bottomOfGroupFragment = new BottomOfGroupFragment();
                    bottomOfGroupFragment.setArguments(data);
                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.empty_space_for_bottom, bottomOfGroupFragment).commit();
                } else {
                    BottomOfChannelFragment bottomOfChannelFragment = new BottomOfChannelFragment();
                    bottomOfChannelFragment.setArguments(data);
                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.empty_space_for_bottom, bottomOfChannelFragment).commit();
                }
            }
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.empty_space_for_top, topOfGroupViewFragment).commit();
        }).start();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.top_of_search_fragment, container, false);
        button_back = contentView.findViewById(R.id.buttonBack);
        message_text = contentView.findViewById(R.id.message_text);
        scroll_up = contentView.findViewById(R.id.scroll_up);
        scroll_down = contentView.findViewById(R.id.scroll_down);
        // получаем данные из getArguments()
        id = getArguments().getLong("chatId");
        currentPosition = getArguments().getInt("position", 0);
        button_back.setOnClickListener(v -> back());
        if (id >= 0) {
            message_text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void afterTextChanged(Editable editable) {
                    new Thread(() -> {
                        ArrayList<String> authData = getAuthData(requireActivity().getApplicationContext());
                        messagesPositions = getPositionsOfFoundMessages(authData.get(0), authData.get(1), id, message_text.getText().toString());
                        if (messagesPositions.size() > 0) {
//                            currentPosition = 0;
                            scroll(messagesPositions.get(currentPosition));
                        } else {
                            scroll(0);
                        }
                    }).start();
                }
            });
            scroll_up.setOnClickListener(v -> {
                if (messagesPositions.size() > 0) {
                    if (currentPosition + 1 < messagesPositions.size()) {
                        currentPosition += 1;
                    } else {
                        currentPosition = 0;
                    }
                    scroll(messagesPositions.get(currentPosition));
                }
            });
            scroll_down.setOnClickListener(v -> {
                if (messagesPositions.size() > 0) {
                    if (currentPosition == 0) {
                        currentPosition = messagesPositions.size() - 1;
                    } else {
                        currentPosition -= 1;
                    }
                    scroll(messagesPositions.get(currentPosition));
                }
            });
            if (getArguments().getString("text") != null) {
                message_text.setText(getArguments().getString("text"));
            } else {
                message_text.setText("");
            }
        }
        return contentView;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Toast.makeText(getContext(), newConfig.colorMode + "", Toast.LENGTH_SHORT).show();
        Bundle b;
        if (getArguments() != null) {
            b = getArguments();
        } else {
            b = new Bundle();
        }
        b.putBoolean("search", true);
        b.putInt("scrollTo", ((LinearLayoutManager) list_of_messages.getLayoutManager()).findFirstVisibleItemPosition());
        b.putString("text", message_text.getText().toString());
        b.putInt("position", currentPosition);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (id >= 0) {
                Intent intent = new Intent(requireActivity().getApplicationContext(), GroupViewActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (id >= 0) {
                Intent intent = new Intent(requireActivity().getApplicationContext(), ChatViewActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        back();
        return true;
    }
}
