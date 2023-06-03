package com.example.vchatmessenger.gui.fragments;

import static com.example.vchatmessenger.core.api.server.GroupRestClient.getChat;
import static com.example.vchatmessenger.core.api.server.UserRestClient.canEditChat;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.dto.Group;
import com.example.vchatmessenger.gui.activities.ChatViewActivity;
import com.example.vchatmessenger.gui.activities.CreateChannelActivity;
import com.example.vchatmessenger.gui.activities.CreateGroupActivity;
import com.example.vchatmessenger.gui.activities.GroupViewActivity;
import com.example.vchatmessenger.gui.recyclerviews.ChatRecyclerAdapter;
import com.example.vchatmessenger.gui.recyclerviews.GroupRecyclerAdapter;
import com.example.vchatmessenger.core.interfaces.IOnBackPressed;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class TopOfGroupViewFragment extends Fragment implements IOnBackPressed {

    View contentView;
    TextView chatName;
    ImageButton buttonBack;
    ImageButton searchButton;
    ConstraintLayout chatInfo;
    ShapeableImageView group_image;
    Vibrator vibrator;
    long chatId;

    private static void back(Activity activity) {
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Intent intent = new Intent(activity, ChatViewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("scrollToChat", ChatRecyclerAdapter.getScrolledPosition());
            intent.putExtras(bundle);
            activity.startActivity(intent);
        } else {
            ((FragmentActivity) activity).getSupportFragmentManager().
                    beginTransaction().
                    replace(R.id.empty_dialog_horizontal, new SelectChatFragment()).commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.top_of_group_fragment, container, false);
        chatName = contentView.findViewById(R.id.group_name);
        searchButton = contentView.findViewById(R.id.search_button);
        buttonBack = contentView.findViewById(R.id.buttonBack);
        chatInfo = contentView.findViewById(R.id.chatInfo);
        group_image = contentView.findViewById(R.id.group_image);
        assert getArguments() != null;
        chatId = getArguments().getLong("chatId");
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        searchButton.setOnClickListener(v -> {
            FragmentManager fm = requireActivity().getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            TopOfSearchMessageFragment topOfSearchMessageFragment = new TopOfSearchMessageFragment();
            Bundle data;
            data = getArguments();
            data.remove("position");
            data.putInt("type", MiddleOfGroupViewFragment.chatType);
            topOfSearchMessageFragment.setArguments(data);
            ft.replace(R.id.empty_space_for_top, topOfSearchMessageFragment);
            ft.commit();
        });
        buttonBack.setOnClickListener(v -> back(requireActivity()));
        chatInfo.setOnClickListener(v -> new Thread(() -> {
            ArrayList<String> authData = getAuthData(requireActivity().getApplicationContext());
            int editChatResult = canEditChat(authData.get(0), authData.get(1), chatId);
            if (editChatResult == 1) {
                Bundle data;
                if (getArguments() != null) {
                    data = getArguments();
                } else {
                    data = new Bundle();
                }
                data.putInt("scrollTo", ((LinearLayoutManager) MiddleOfGroupViewFragment.list_of_messages.getLayoutManager()).findFirstVisibleItemPosition());
                data.putLong("chatId", chatId);
                if (GroupRecyclerAdapter.chatType == 1) {  // TODO ???????
                    Intent intent = new Intent(requireActivity(), CreateGroupActivity.class);
                    intent.putExtras(data);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(requireActivity(), CreateChannelActivity.class);
                    intent.putExtras(data);
                    startActivity(intent);
                }
            }
        }).start());
        return contentView;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Bundle bundle = new Bundle();
        if (getArguments() != null) {
            bundle = getArguments();
        }
        bundle.putInt("scrollTo", ((LinearLayoutManager) MiddleOfGroupViewFragment.list_of_messages.getLayoutManager()).findFirstVisibleItemPosition());
        bundle.putInt("scrollToChat", ChatRecyclerAdapter.getScrolledPosition());
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (chatId >= 0) {
                Intent intent = new Intent(requireActivity().getApplicationContext(), GroupViewActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (chatId >= 0) {
                Intent intent = new Intent(requireActivity().getApplicationContext(), ChatViewActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (chatId > 0) {
            new Thread(() -> {
                ArrayList<String> authData = getAuthData(requireActivity());
                Group chat = getChat(authData.get(0), authData.get(1), chatId);
                if (chat != null) {
                    new Thread(() -> {
                        byte[] imageData = android.util.Base64.decode(chat.getImageData(), Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                        group_image.post(() -> group_image.setImageBitmap(bitmap));
                    }).start();
                    new Thread(() -> chatName.post(() -> chatName.setText(chat.getName()))).start();
                }
            }).start();
        }
    }

    @Override
    public boolean onBackPressed() {
        back(requireActivity());
        return true;
    }
}
