package com.example.vchatmessenger.gui.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.vchatmessenger.gui.fragments.ChatViewFragment;
import com.example.vchatmessenger.gui.recyclerviews.ChatRecyclerAdapter;
import com.example.vchatmessenger.gui.fragments.GroupViewFragment;
import com.example.vchatmessenger.gui.fragments.SearchMessageFragment;
import com.example.vchatmessenger.R;
import com.example.vchatmessenger.gui.fragments.SearchChatFragment;
import com.example.vchatmessenger.gui.fragments.SelectChatFragment;
import com.example.vchatmessenger.gui.fragments.TopOfChatViewFragment;
import com.example.vchatmessenger.gui.fragments.TopOfSearchChatFragment;
import com.example.vchatmessenger.core.interfaces.IOnBackPressed;
import com.example.vchatmessenger.core.api.socket_gateway.SocketService;

import java.util.List;

public class ChatViewActivity extends FragmentActivity {
    public static long id = -1;
    public static boolean startSearchChat;
    public static boolean startSearchInChat;
    private static String nameOfSearchedChat;

    public static String getNameOfSearchedChat() {
        return nameOfSearchedChat;
    }

    public static void setNameOfSearchedChat(String nameOfSearchedChat) {
        ChatViewActivity.nameOfSearchedChat = nameOfSearchedChat;
    }

    public static boolean isStartSearchChat() {
        return startSearchChat;
    }

    public static void setStartSearchChat(boolean start_search_chat) {
        ChatViewActivity.startSearchChat = start_search_chat;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (startSearchChat) {
            TopOfSearchChatFragment topOfSearchChatFragment = new TopOfSearchChatFragment();
            ft.replace(R.id.header_of_chat, topOfSearchChatFragment);
        } else {
            TopOfChatViewFragment topOfChatViewFragment = new TopOfChatViewFragment();
            ft.replace(R.id.header_of_chat, topOfChatViewFragment);
        }
        ft.commit();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            try {
                id = getIntent().getExtras().getLong("chatId");
            } catch (Exception e) {
                id = -1;
            }
            try {
                startSearchInChat = getIntent().getExtras().getBoolean("start_search");
            } catch (Exception e) {
                startSearchInChat = false;
            }
            if (!startSearchInChat) {
                if (id > 0) {
                    GroupViewFragment groupViewFragment = new GroupViewFragment();
                    groupViewFragment.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction().replace(R.id.empty_dialog_horizontal, groupViewFragment).commit();
                } else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.empty_dialog_horizontal, new SelectChatFragment()).commit();
                }
            } else {
                SearchMessageFragment searchMessageFragment = new SearchMessageFragment();
                searchMessageFragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction().replace(R.id.empty_dialog_horizontal, searchMessageFragment).commit();
            }
            if (!startSearchChat) {
                ChatViewFragment chatViewFragment = new ChatViewFragment();
                chatViewFragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_chats_layout, chatViewFragment).commit();
            }
        } else {
            if (!startSearchChat) {
                fm = getSupportFragmentManager();
                ft = fm.beginTransaction();
                ChatViewFragment chatViewFragment = new ChatViewFragment();
                chatViewFragment.setArguments(getIntent().getExtras());
                ft.replace(R.id.empty_dialog_vertical, chatViewFragment);
                ft.commit();
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (startSearchChat) {
                int position = ((LinearLayoutManager) SearchChatFragment.list_of_chats.getLayoutManager()).findFirstVisibleItemPosition();
                TopOfSearchChatFragment.setCurrentPosition(position);
            }
            Intent intent = new Intent(ChatViewActivity.this, ChatViewActivity.class);
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.empty_dialog_horizontal);
            Bundle bundle = new Bundle();
            if (fragment != null) {
                if (fragment.getArguments() != null) {
                    bundle = fragment.getArguments();
                }
            }
            bundle.putInt("scrollToChat", ChatRecyclerAdapter.getScrolledPosition());
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.header_of_chat);
        if (fragment != null) {
            ((IOnBackPressed) fragment).onBackPressed();
        } else {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, SocketService.class);
        if (!isServiceRunning(SocketService.class)) {
            getApplicationContext().startService(intent);
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
        if (runningServices != null) {
            for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
                if (serviceInfo.service.getClassName().equals(serviceClass.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
