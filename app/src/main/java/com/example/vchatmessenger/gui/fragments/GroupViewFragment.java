package com.example.vchatmessenger.gui.fragments;

import static com.example.vchatmessenger.core.api.server.GroupRestClient.getChat;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.dto.Group;
import com.example.vchatmessenger.core.interfaces.IOnBackPressed;

import java.util.ArrayList;

public class GroupViewFragment extends Fragment implements IOnBackPressed {

    public static long id;
    long userId;
    boolean search;
    private Group chat;
    View contentView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        // получаем данные из getArguments
        assert getArguments() != null;
        id = getArguments().getLong("chatId");
        search = getArguments().getBoolean("search", false);
        userId = getArguments().getLong("userId");
        Thread t = new Thread(() -> {
            ArrayList<String> authData = getAuthData(requireActivity());
            chat = getChat(authData.get(0), authData.get(1), id);
            if (getArguments().getInt("chatType") == 1) {
                contentView = inflater.inflate(R.layout.group_fragment, container, false);
                if (!search) {
                    if (chat.getMembersIds().contains(userId)) {
                        BottomOfGroupFragment bottomOfGroupFragment = new BottomOfGroupFragment();
                        bottomOfGroupFragment.setArguments(getArguments());
                        ft.replace(R.id.empty_space_for_bottom, bottomOfGroupFragment);
                    } else {
                        AddChatFragment addChatFragment = new AddChatFragment();
                        addChatFragment.setArguments(getArguments());
                        ft.replace(R.id.empty_space_for_bottom, addChatFragment);
                    }
                } else {
                    BottomOfMessageSearchFragment bottomOfMessageSearchFragment = new BottomOfMessageSearchFragment();
                    bottomOfMessageSearchFragment.setArguments(getArguments());
                    ft.replace(R.id.empty_space_for_bottom, bottomOfMessageSearchFragment);
                }
            } else {
                contentView = inflater.inflate(R.layout.channel_fragment, container, false);
                if (!search) {
                    if (chat.getMembersIds().contains(userId)) {
                        if (chat.getOwnerId().equals(getArguments().getLong("userId"))) {
                            BottomOfGroupFragment bottomOfGroupFragment = new BottomOfGroupFragment();
                            bottomOfGroupFragment.setArguments(getArguments());
                            ft.replace(R.id.empty_space_for_bottom, bottomOfGroupFragment);
                        } else {
                            BottomOfChannelFragment bottomOfChannelFragment = new BottomOfChannelFragment();
                            bottomOfChannelFragment.setArguments(getArguments());
                            ft.replace(R.id.empty_space_for_bottom, bottomOfChannelFragment);
                        }
                    } else {
                        AddChatFragment addChatFragment = new AddChatFragment();
                        addChatFragment.setArguments(getArguments());
                        ft.replace(R.id.empty_space_for_bottom, addChatFragment);
                    }
                } else {
                    BottomOfMessageSearchFragment bottomOfMessageSearchFragment = new BottomOfMessageSearchFragment();
                    bottomOfMessageSearchFragment.setArguments(getArguments());
                    ft.replace(R.id.empty_space_for_bottom, bottomOfMessageSearchFragment);
                }
            }
            if (!search) {
                TopOfGroupViewFragment topOfGroupViewFragment = new TopOfGroupViewFragment();
                topOfGroupViewFragment.setArguments(getArguments());
                MiddleOfGroupViewFragment middleOfGroupViewFragment = new MiddleOfGroupViewFragment();
                middleOfGroupViewFragment.setArguments(getArguments());
                ft.replace(R.id.empty_space_for_top, topOfGroupViewFragment);
                ft.replace(R.id.empty_space_for_middle, middleOfGroupViewFragment);
            } else {
                TopOfSearchMessageFragment topOfSearchMessageFragment = new TopOfSearchMessageFragment();
                topOfSearchMessageFragment.setArguments(getArguments());
                MiddleOfGroupViewFragment middleOfGroupViewFragment = new MiddleOfGroupViewFragment();
                middleOfGroupViewFragment.setArguments(getArguments());
                ft.replace(R.id.empty_space_for_top, topOfSearchMessageFragment);
                ft.replace(R.id.empty_space_for_middle, middleOfGroupViewFragment);
            }
            ft.commit();
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return contentView;
    }

    @Override
    public boolean onBackPressed() {
        Fragment fragment = requireActivity().getSupportFragmentManager().findFragmentById(R.id.empty_space_for_top);
        if (fragment != null) {
            ((IOnBackPressed) fragment).onBackPressed();
        }
        return true;
    }
}
