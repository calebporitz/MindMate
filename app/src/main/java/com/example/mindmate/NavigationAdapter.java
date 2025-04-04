package com.example.mindmate;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class NavigationAdapter extends FragmentStateAdapter {

    public NavigationAdapter(@NonNull NavigationActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Here you can return different fragments for each tab
        switch (position) {
            case 0:
                return new ChatOverviewFragment();
            case 2:
                return new ProfileFragment();
            default:
                return new MatchingFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;  // We have 3 main pages
    }
}
