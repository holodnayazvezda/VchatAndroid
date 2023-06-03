package com.example.vchatmessenger.gui.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.gui.activities.ChatViewActivity;
import com.example.vchatmessenger.core.interfaces.IOnBackPressed;

public class TopOfSearchChatFragment extends Fragment implements IOnBackPressed {
    View contentView;
    ImageButton buttonBack;
    EditText name_of_chat;
    private static int positionsOfChatListBeforeSearch = 0;
    private static int currentPosition = 0;

    public static int getPositionsOfChatListBeforeSearch() {
        return positionsOfChatListBeforeSearch;
    }

    public static void setPositionsOfChatListBeforeSearch(int positionsOfChatListBeforeSearch) {
        TopOfSearchChatFragment.positionsOfChatListBeforeSearch = positionsOfChatListBeforeSearch;
    }

    public static int getCurrentPosition() {
        return currentPosition;
    }

    public static void setCurrentPosition(int currentPosition) {
        TopOfSearchChatFragment.currentPosition = currentPosition;
    }

    private void back() {
        TopOfSearchChatFragment.setCurrentPosition(0);
        ChatViewActivity.setStartSearchChat(false);
        ((ChatViewActivity) requireActivity()).setNameOfSearchedChat("");
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        TopOfChatViewFragment topOfChatViewFragment = new TopOfChatViewFragment();
        ft.replace(R.id.header_of_chat, topOfChatViewFragment);
        ChatViewFragment chatViewFragment = new ChatViewFragment();
        Bundle b = new Bundle();
        if (getArguments() != null) {
            b = getArguments();
        }
        b.putInt("scrollToChat", TopOfSearchChatFragment.getPositionsOfChatListBeforeSearch());
        chatViewFragment.setArguments(b);
        ChatViewFragment.noChatUpdate = true;
        SearchChatFragment.noWork = true;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            ft.replace(R.id.empty_dialog_vertical, chatViewFragment);
        } else {
            ft.replace(R.id.fragment_chats_layout, chatViewFragment);
        }
        ft.commit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.top_search_of_chat_fragment, container, false);
        buttonBack = contentView.findViewById(R.id.buttonBack);
        name_of_chat = contentView.findViewById(R.id.name_of_chat);
        buttonBack.setOnClickListener(v -> back());
        name_of_chat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                ((ChatViewActivity) requireActivity()).setNameOfSearchedChat(name_of_chat.getText().toString());
                SearchChatFragment.updateListOfChats(requireActivity());
            }
        });
        if (ChatViewActivity.getNameOfSearchedChat() != null) {
            name_of_chat.setText(ChatViewActivity.getNameOfSearchedChat());
        }
        Bundle bundle = new Bundle();
        bundle.putString("data", name_of_chat.getText().toString());
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        SearchChatFragment searchChatFragment = new SearchChatFragment();
        searchChatFragment.setArguments(bundle);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            ft.replace(R.id.empty_dialog_vertical, searchChatFragment);
        } else {
            ft.replace(R.id.fragment_chats_layout, searchChatFragment);
        }
        ft.commit();
        return contentView;
    }

    @Override
    public boolean onBackPressed() {
        Fragment fragment = requireActivity().getSupportFragmentManager().findFragmentById(R.id.empty_dialog_horizontal);
        try {
            ((IOnBackPressed) fragment).onBackPressed();
        } catch (Exception e) {
            back();
        }
        return false;
    }
}
