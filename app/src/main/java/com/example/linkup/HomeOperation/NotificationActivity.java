package com.example.linkup.HomeOperation;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.linkup.EventOperation.InvitationActivity;
import com.example.linkup.EventOperation.TabbedView.InvitationPagerAdapter;
import com.example.linkup.HomeOperation.TabbedView.NotificationPagerAdapter;
import com.example.linkup.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class NotificationActivity extends AppCompatActivity {
    // Layout objects
    ImageView btnBack;
    // Tabbed View
    NotificationPagerAdapter notificationPagerAdapter;
    ViewPager2 tabbedView;
    TabLayout tab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        // Tabbed view
        tabbedView = findViewById(R.id.tabbedView);
        tab = findViewById(R.id.tab);
        // [END gain]

        // [START config_layout]
        notificationPagerAdapter= new NotificationPagerAdapter(NotificationActivity.this);
        tabbedView.setAdapter(notificationPagerAdapter);
        // Link TabLayout with ViewPager2 using TabLayoutMediator
        new TabLayoutMediator(tab, tabbedView, (tab, position) -> {
            if (position == 0) {
                tab.setIcon(R.drawable.baseline_person_add_alt_1_24);
            } else if (position == 1) {
                tab.setIcon(R.drawable.baseline_event_repeat_24);
            } else if (position == 2) {
                tab.setIcon(R.drawable.baseline_assignment_late_24);
            }
        }).attach();
        // [END config_layout]

        // [START layout component function]
        // Switch the screen
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // [END layout component function]
    }
}