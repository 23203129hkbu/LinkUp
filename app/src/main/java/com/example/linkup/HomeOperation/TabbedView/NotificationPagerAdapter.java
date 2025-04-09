package com.example.linkup.HomeOperation.TabbedView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.linkup.EventOperation.TabbedView.FollowerTab;
import com.example.linkup.EventOperation.TabbedView.FollowingTab;
import com.example.linkup.Object.Events;

public class NotificationPagerAdapter extends FragmentStateAdapter {

    public NotificationPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new FollowRequestTab();
        } else if (position == 1){
            return new InvitationTab();
        }else {
            return new ModificationTab();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Number of tabs (Following and Follower)
    }
}
