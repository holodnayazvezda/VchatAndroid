package com.example.vchatmessenger.gui.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.gui.activities.ChatViewActivity;
import com.example.vchatmessenger.gui.recyclerviews.ChatRecyclerAdapter;

public class SelectChatFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_chat, container, false);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (ChatViewActivity.isStartSearchChat()) {
                int position = ((LinearLayoutManager) SearchChatFragment.list_of_chats.getLayoutManager()).findFirstVisibleItemPosition();
                TopOfSearchChatFragment.setCurrentPosition(position);
            }
            Intent intent = new Intent(requireActivity().getApplicationContext(), ChatViewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("scrollToChat", ChatRecyclerAdapter.getScrolledPosition());
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }
}