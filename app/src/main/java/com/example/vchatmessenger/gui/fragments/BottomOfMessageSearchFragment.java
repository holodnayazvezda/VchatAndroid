package com.example.vchatmessenger.gui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.vchatmessenger.R;

public class BottomOfMessageSearchFragment extends Fragment {

    View contentView;
    TextView amount_of_overlaps;
    int amount;
    int position;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.bottom_of_search_fragment, container, false);
        amount_of_overlaps = contentView.findViewById(R.id.amount_of_overlaps);
        position = getArguments().getInt("position");
        amount = getArguments().getInt("amount_of_overlaps");
        if (amount < 0) {
            amount_of_overlaps.setText("");
        } else if (amount == 0) {
            amount_of_overlaps.setText(R.string.no_matches);
        } else {
            String text = (position + 1) + " " + getString(R.string.from) + " " + amount;
            amount_of_overlaps.setText(text);
        }
        return contentView;
    }
}
