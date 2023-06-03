package com.example.vchatmessenger.gui.fragments;

import static com.example.vchatmessenger.core.api.server.UserRestClient.get;
import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.gui.activities.UserActivity;
import com.example.vchatmessenger.dto.User;
import com.example.vchatmessenger.gui.activities.ChatViewActivity;
import com.example.vchatmessenger.gui.recyclerviews.ChatRecyclerAdapter;
import com.example.vchatmessenger.core.interfaces.IOnBackPressed;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.Base64;

public class TopOfChatViewFragment extends Fragment implements IOnBackPressed {

    View contentView;
    Button buttonSearch;
    ShapeableImageView buttonUserInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.top_of_chat_fragment, container, false);
        buttonSearch = contentView.findViewById(R.id.button_search);
        buttonUserInfo = contentView.findViewById(R.id.user_info);
        buttonSearch.setOnClickListener(v -> {
            ChatViewActivity.setStartSearchChat(true);
            SearchChatFragment.noWork = false;
            FragmentManager fm = requireActivity().getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            TopOfSearchChatFragment topOfSearchChatFragment = new TopOfSearchChatFragment();
            TopOfSearchChatFragment.setPositionsOfChatListBeforeSearch(ChatRecyclerAdapter.getScrolledPosition());
            ft.replace(R.id.header_of_chat, topOfSearchChatFragment);
            ft.commit();
        });
        buttonUserInfo.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), UserActivity.class);
            startActivity(intent);
        });
        return contentView;
    }

    @Override
    public boolean onBackPressed() {
        Fragment fragment = requireActivity().getSupportFragmentManager().findFragmentById(R.id.empty_dialog_horizontal);
        try {
            ((IOnBackPressed) fragment).onBackPressed();
        } catch (Exception e) {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(() -> {
            ArrayList<String> authData = getAuthData(requireActivity());
            User userResult = get(authData.get(0), authData.get(1));
            if (userResult != null) {
                String imageData = userResult.getImageData();
                byte[] data = Base64.getDecoder().decode(imageData);
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                buttonUserInfo.post(() -> buttonUserInfo.setImageBitmap(bitmap));
            }
        }).start();
    }
}
