package com.example.linkup.ProfileOperation.TabbedView;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.linkup.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStateAdapter {
    String uid;

    public SectionsPagerAdapter(FragmentActivity fragmentActivity, String uid) {
        super(fragmentActivity);
        this.uid = uid;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ImageTab(uid);
            case 1:
                return new VideoTab(uid);
            default:
                return new ImageTab(uid);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Number of tabs
    }
}