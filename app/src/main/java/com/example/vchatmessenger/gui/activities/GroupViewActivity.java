package com.example.vchatmessenger.gui.activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.vchatmessenger.R;
import com.example.vchatmessenger.gui.fragments.GroupViewFragment;
import com.example.vchatmessenger.core.interfaces.IOnBackPressed;

public class GroupViewActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_view_layout);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        GroupViewFragment groupViewFragment = new GroupViewFragment();
        groupViewFragment.setArguments(getIntent().getExtras());
        ft.replace(R.id.empty_dialog_horizontal, groupViewFragment);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.empty_space_for_top);
        if (fragment != null) {
            ((IOnBackPressed) fragment).onBackPressed();
        }
    }
}
