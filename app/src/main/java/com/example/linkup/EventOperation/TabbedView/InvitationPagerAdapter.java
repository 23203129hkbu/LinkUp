package com.example.linkup.EventOperation.TabbedView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.linkup.Object.Events;

public class InvitationPagerAdapter extends FragmentStateAdapter {
    Events event;

    public InvitationPagerAdapter(@NonNull FragmentActivity fragmentActivity, Events event) {
        super(fragmentActivity);
        this.event = event;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new FollowingTab(event);
        } else {
            return new FollowerTab(event);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Number of tabs (Following and Follower)
    }
}
