package com.example.vchatmessenger.gui.fragments;

import static com.example.vchatmessenger.core.api.server.GroupRestClient.searchChatsWithOffset;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.dto.Group;
import com.example.vchatmessenger.gui.activities.ChatViewActivity;
import com.example.vchatmessenger.gui.recyclerviews.ChatRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchChatFragment extends Fragment {

    public static RecyclerView list_of_chats;
    public static List<Group> foundChatList;
    static boolean noChat = false;
    static boolean noChatUpdate = false;
    static int lastVisibleItemPosition;
    static int limit = 12;
    public static int offset = 0;
    static boolean loading = false;
    public static boolean noWork = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_search_chat, container, false);
        list_of_chats = contentView.findViewById(R.id.list_of_chats);
        list_of_chats.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItemPosition = ((LinearLayoutManager) list_of_chats.getLayoutManager()).findLastVisibleItemPosition();
                if (!loading && (lastVisibleItemPosition == offset || (lastVisibleItemPosition - 1) == offset || (lastVisibleItemPosition + 1) == offset) && !noWork) {
                    loading = true;
                    new Thread(() -> {
                        ArrayList<String> authData = getAuthData(requireActivity().getApplicationContext());
                        List<Group> foundChats = searchChatsWithOffset(
                                authData.get(0), authData.get(1), ChatViewActivity.getNameOfSearchedChat(),
                                limit, offset
                        );
                        if (foundChats != null) {
                            foundChatList.addAll(foundChats);
                            if (list_of_chats.getAdapter() != null) {
                                list_of_chats.post(() -> list_of_chats.getAdapter().notifyItemRangeChanged(foundChatList.size() - foundChats.size(), foundChats.size()));
                            } else {
                                ChatRecyclerAdapter chatRecyclerAdapter = new ChatRecyclerAdapter(foundChatList);
                                list_of_chats.setAdapter(chatRecyclerAdapter);
                                list_of_chats.post(() -> list_of_chats.getAdapter().notifyItemRangeChanged(foundChatList.size() - foundChats.size(), foundChats.size()));
                            }
                            offset += foundChats.size();
                        }
                        loading = false;
                    }).start();
                }
            }
        });
        updateListOfChats(requireActivity());
        return contentView;
    }

    public static void updateListOfChats(Context context) {
        if (!noWork) {
            ArrayList<String> authData = getAuthData(context.getApplicationContext());
            if (list_of_chats != null) {
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context.getApplicationContext());
                list_of_chats.setLayoutManager(layoutManager);
                if (!noChatUpdate) {
                    if (ChatViewActivity.getNameOfSearchedChat() != null && !ChatViewActivity.getNameOfSearchedChat().equals("")) {
                        new Thread(() -> {
                            List<Group> foundChats;
                            if (TopOfSearchChatFragment.getCurrentPosition() >= 0) {
                                foundChats = searchChatsWithOffset(
                                        authData.get(0), authData.get(1), ChatViewActivity.getNameOfSearchedChat(),
                                        TopOfSearchChatFragment.getCurrentPosition() + limit, 0
                                );
                            } else {
                                foundChats = searchChatsWithOffset(
                                        authData.get(0), authData.get(1), ChatViewActivity.getNameOfSearchedChat(),
                                        limit, 0
                                );
                            }
                            if (!foundChats.equals(foundChatList)) {
                                foundChatList = foundChats;
                            }
                            if (foundChatList.size() > 0) {
                                if (noChat) {
                                    noChat = false;
                                    FragmentManager fm = ((ChatViewActivity) context).getSupportFragmentManager();
                                    FragmentTransaction ft = fm.beginTransaction();
                                    SearchChatFragment searchChatFragment = new SearchChatFragment();
                                    if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                        ft.replace(R.id.empty_dialog_vertical, searchChatFragment);
                                    } else {
                                        ft.replace(R.id.fragment_chats_layout, searchChatFragment);
                                    }
                                    ft.commit();
                                } else {
                                    ChatRecyclerAdapter chatRecyclerAdapter = new ChatRecyclerAdapter(foundChatList);
                                    list_of_chats.post(() -> list_of_chats.setAdapter(chatRecyclerAdapter));
                                    offset = foundChatList.size();
                                }
                                list_of_chats.post(() -> list_of_chats.scrollToPosition(TopOfSearchChatFragment.getCurrentPosition()));
                            } else {
                                setNoChatFragment(context);
                            }
                        }).start();
                    } else {
                        setNoChatFragment(context);
                    }
                }
            } else {
                ChatRecyclerAdapter chatRecyclerAdapter = new ChatRecyclerAdapter(foundChatList);
                list_of_chats.post(() -> list_of_chats.setAdapter(chatRecyclerAdapter));
                noChatUpdate = false;
                list_of_chats.post(() -> list_of_chats.scrollToPosition(TopOfSearchChatFragment.getCurrentPosition()));
            }
        }
    }

    public static void setNoChatFragment(Context context) {
        if (!noWork) {
            noChat = true;
            FragmentManager fm = ((ChatViewActivity) context).getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            NoChatFoundFragment noChatFoundFragment = new NoChatFoundFragment();
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                ft.replace(R.id.empty_dialog_vertical, noChatFoundFragment);
            } else {
                ft.replace(R.id.fragment_chats_layout, noChatFoundFragment);
            }
            ft.commit();
        }
    }
}
