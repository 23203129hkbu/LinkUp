package com.example.linkup.EventOperation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.linkup.EventOperation.TabbedView.InvitationPagerAdapter;
import com.example.linkup.HomeOperation.FollowerActivity;
import com.example.linkup.HomeOperation.FollowingActivity;
import com.example.linkup.HomeOperation.UserProfile;
import com.example.linkup.Object.Events;
import com.example.linkup.ProfileOperation.TabbedView.SectionsPagerAdapter;
import com.example.linkup.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class InvitationActivity extends AppCompatActivity {
    // Layout objects
    ImageView btnBack;
    // Tabbed View
    InvitationPagerAdapter invitationPagerAdapter;
    ViewPager2 tabbedView;
    TabLayout tab;
    // Event info
    Events event = new Events();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);
        // Retrieve the event object from the intent
        event = (Events) getIntent().getSerializableExtra("event");
        // [END gain]

        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        // Tabbed view
        tabbedView = findViewById(R.id.tabbedView);
        tab = findViewById(R.id.tab);
        // [END gain]

        // [START config_layout]
        invitationPagerAdapter = new InvitationPagerAdapter(InvitationActivity.this, event);
        tabbedView.setAdapter(invitationPagerAdapter);
        // Link TabLayout with ViewPager2 using TabLayoutMediator
        new TabLayoutMediator(tab, tabbedView, (tab, position) -> {
            if (position == 0) {
                tab.setText("Following");
            } else if (position == 1) {
                tab.setText("Follower");
            }
        }).attach();
        // [END config_layout]

        // [START layout component function]
        // Switch the screen -
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // [END layout component function]
    }
}