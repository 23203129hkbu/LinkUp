package com.example.linkup.EventOperation.TabbedView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.linkup.Object.Events;
import com.example.linkup.ProfileOperation.TabbedView.ImageTab;
import com.example.linkup.ProfileOperation.TabbedView.VideoTab;

public class InviteePagerAdapter extends FragmentStateAdapter {
    Events event;

    public InviteePagerAdapter(FragmentActivity fragmentActivity, Events event) {
        super(fragmentActivity);
        this.event = event;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ImageTab(event);
            case 1:
                return new VideoTab(event);
            default:
                return new ImageTab(event);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Number of tabs
    }
}