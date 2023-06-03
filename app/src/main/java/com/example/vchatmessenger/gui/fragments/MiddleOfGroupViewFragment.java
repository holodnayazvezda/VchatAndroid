package com.example.vchatmessenger.gui.fragments;

import static com.example.vchatmessenger.core.api.server.MessageRestClient.getMessagesWithOffset;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.dto.Message;
import com.example.vchatmessenger.gui.recyclerviews.GroupRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MiddleOfGroupViewFragment extends Fragment {

    View contentView;
    public static RecyclerView list_of_messages;
    public static List<Message> messagesList;
    static long chatId;
    static long userId;
    public static int chatType;
    public static int scrollTo;
    public static String searchedText;
    public static String nameOfChat;
    static boolean loading = false;
    static int lastVisibleItemPosition;
    static int limit = 12;
    public static int offset = 0;
    static boolean noScroll = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.middle_of_group_fragment, container, false);
        list_of_messages = contentView.findViewById(R.id.fragment_chat);
        assert getArguments() != null;
        chatId = getArguments().getLong("chatId");
        scrollTo = getArguments().getInt("scrollTo");
        nameOfChat = getArguments().getString("nameOfChat");
        searchedText = getArguments().getString("text");
        userId = getArguments().getLong("userId");
        chatType = getArguments().getInt("chatType");
        list_of_messages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!noScroll) {
                    lastVisibleItemPosition = ((LinearLayoutManager) list_of_messages.getLayoutManager()).findLastVisibleItemPosition();
                    if (!loading && (lastVisibleItemPosition == offset || (lastVisibleItemPosition + 1) == offset)) {
                        loading = true;
                        new Thread(() -> {
                            ArrayList<String> authData = getAuthData(requireActivity().getApplicationContext());
                            List<Message> messages = getMessagesWithOffset(authData.get(0), authData.get(1), chatId, limit, offset);
                            if (messages != null) {
                                messagesList.addAll(messages);
                                list_of_messages.post(() -> {
                                    if (list_of_messages.getAdapter() != null) {
                                        list_of_messages.getAdapter().notifyItemRangeChanged(messagesList.size() - messages.size(), messages.size());
                                    } else {
                                        GroupRecyclerAdapter groupRecyclerAdapter = new GroupRecyclerAdapter(messagesList, userId, chatType, nameOfChat, searchedText);
                                        list_of_messages.setAdapter(groupRecyclerAdapter);
                                        list_of_messages.getAdapter().notifyItemRangeChanged(messagesList.size() - messages.size(), messages.size());
                                    }
                                });
                                offset += messages.size();
                            }
                            loading = false;
                        }).start();
                    }
                } else {
                    noScroll = false;
                }
            }
        });
        updateListOfMessages(requireActivity().getApplicationContext());
        return contentView;
    }

    public static void updateListOfMessages(Context context) {
        ArrayList<String> authData = getAuthData(context.getApplicationContext());
        if (list_of_messages != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(context.getApplicationContext());
            layoutManager.setReverseLayout(true);
            if (list_of_messages != null) {
                list_of_messages.setLayoutManager(layoutManager);
                new Thread(() -> {
                    if (scrollTo > 0) {
                        messagesList = getMessagesWithOffset(authData.get(0), authData.get(1), chatId, scrollTo + limit, 0);
                    } else {
                        messagesList = getMessagesWithOffset(authData.get(0), authData.get(1), chatId, limit, 0);
                    }
                    if (messagesList != null) {
                        GroupRecyclerAdapter groupRecyclerAdapter = new GroupRecyclerAdapter(messagesList, userId, chatType, nameOfChat, searchedText);
                        list_of_messages.post(() -> list_of_messages.setAdapter(groupRecyclerAdapter));
                        offset = messagesList.size();
                        if (scrollTo >= 0) {
                            list_of_messages.post(() -> list_of_messages.scrollToPosition(scrollTo));
                        }
                        noScroll = true;
                    }
                }).start();
            }
        }
    }

    public static void scrollToPosition(int position, Context context) {
        if (list_of_messages != null) {
            ArrayList<String> authData = getAuthData(context.getApplicationContext());
            LinearLayoutManager layoutManager = new LinearLayoutManager(context.getApplicationContext());
            layoutManager.setReverseLayout(true);
            list_of_messages.setLayoutManager(layoutManager);
            new Thread(() -> {
                if (position > messagesList.size()) {
                    if (position > 0) {
                        messagesList = getMessagesWithOffset(authData.get(0), authData.get(1), chatId, position + limit, 0);
                    } else {
                        messagesList = getMessagesWithOffset(authData.get(0), authData.get(1), chatId, limit, 0);
                    }
                }
                if (messagesList != null) {
                    GroupRecyclerAdapter groupRecyclerAdapter = new GroupRecyclerAdapter(messagesList, userId, chatType, nameOfChat, searchedText);
                    list_of_messages.post(() -> list_of_messages.setAdapter(groupRecyclerAdapter));
                    offset = messagesList.size();
                    if (position >= 0) {
                        list_of_messages.post(() -> list_of_messages.scrollToPosition(position));
                    }
                    noScroll = true;
                }
            }).start();
        }
    }
}
