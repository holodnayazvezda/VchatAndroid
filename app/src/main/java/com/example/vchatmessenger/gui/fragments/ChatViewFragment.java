package com.example.vchatmessenger.gui.fragments;

import static com.example.vchatmessenger.core.api.server.UserRestClient.getChatsWithOffset;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vchatmessenger.gui.recyclerviews.ChatRecyclerAdapter;
import com.example.vchatmessenger.gui.activities.CreateChannelActivity;
import com.example.vchatmessenger.gui.activities.CreateGroupActivity;
import com.example.vchatmessenger.R;
import com.example.vchatmessenger.dto.Group;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class ChatViewFragment extends Fragment {
    public static RecyclerView list_of_chats;
    public static int scrollToChat;
    public static List<Group> chatList = new ArrayList<>();

    static boolean loading = false;
    static int lastVisibleItemPosition;
    static int limit = 11;
    public static int offset = 0;
    static boolean noChatUpdate = false;

    @SuppressLint("ResourceAsColor")
    private void showPicDialog() {
        String[] options = {getString(R.string.group), getString(R.string.channel), getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.create));
        builder.setItems(options, (dialog, which) -> {
            Bundle data;
            if (getArguments() != null) {
                data = getArguments();
            } else {
                data = new Bundle();
            }
            if (which == 0) {
                Intent intent = new Intent(requireActivity(), CreateGroupActivity.class);
                intent.putExtras(data);
                startActivity(intent);
            } else if (which == 1) {
                Intent intent = new Intent(requireActivity(), CreateChannelActivity.class);
                intent.putExtras(data);
                startActivity(intent);
            } else {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            scrollToChat = getArguments().getInt("scrollToChat");
        } catch (Exception e) {
            scrollToChat = -1;
        }
        View contentView = inflater.inflate(R.layout.fragment_chats, container, false);
        list_of_chats = contentView.findViewById(R.id.list_of_chats);
        ShapeableImageView createChat = contentView.findViewById(R.id.createChat);
        createChat.setOnClickListener(v -> showPicDialog());
        RecyclerView.RecycledViewPool recycledViewPool = new RecyclerView.RecycledViewPool();
        list_of_chats.setRecycledViewPool(recycledViewPool);
        list_of_chats.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (list_of_chats != null) {
                    lastVisibleItemPosition = ((LinearLayoutManager) list_of_chats.getLayoutManager()).findLastVisibleItemPosition();
                    if (!loading && (lastVisibleItemPosition == offset || (lastVisibleItemPosition + 1) == offset || (lastVisibleItemPosition + 2) == offset)) {
                        loading = true;
                        new Thread(() -> {
                            ArrayList<String> authData = getAuthData(requireActivity().getApplicationContext());
                            List<Group> chats = getChatsWithOffset(authData.get(0), authData.get(1), limit, offset);
                            if (chats != null) {
                                chatList.addAll(chats);
                                if (list_of_chats.getAdapter() != null) {
                                    list_of_chats.post(() -> list_of_chats.getAdapter().notifyItemRangeInserted(chatList.size() - chats.size(), chats.size()));
                                } else {
                                    ChatRecyclerAdapter chatRecyclerAdapter = new ChatRecyclerAdapter(chatList);
                                    list_of_chats.post(() -> list_of_chats.setAdapter(chatRecyclerAdapter));
                                    list_of_chats.post(() -> list_of_chats.getAdapter().notifyItemRangeInserted(chatList.size() - chats.size(), chats.size()));
                                }
                                offset += chats.size();
                            }
                            loading = false;
                        }).start();
                    }
                }
            }
        });
        updateListOfChats(requireActivity().getApplicationContext());
        return contentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    public static void updateListOfChats(Context context) {
        if (list_of_chats != null) {
            ArrayList<String> authData = getAuthData(context.getApplicationContext());
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context.getApplicationContext());
            list_of_chats.setLayoutManager(layoutManager);
            if (!noChatUpdate) {
                new Thread(() -> {
                    if (scrollToChat >= 0) {
                        chatList = getChatsWithOffset(authData.get(0), authData.get(1), scrollToChat + limit, 0);
                    } else {
                        chatList = getChatsWithOffset(authData.get(0), authData.get(1), limit, 0);
                    }
                    if (chatList != null) {
                        ChatRecyclerAdapter chatRecyclerAdapter = new ChatRecyclerAdapter(chatList);
                        list_of_chats.post(() -> list_of_chats.setAdapter(chatRecyclerAdapter));
                        offset = chatList.size();
                    }
                    if (scrollToChat > 0) {
                        list_of_chats.post(() -> list_of_chats.scrollToPosition(scrollToChat));
                    }
                }).start();
            } else {
                ChatRecyclerAdapter chatRecyclerAdapter = new ChatRecyclerAdapter(chatList);
                list_of_chats.setAdapter(chatRecyclerAdapter);
                list_of_chats.scrollToPosition(scrollToChat);
                noChatUpdate = false;
            }
        }
    }
}