package com.example.linkup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.linkup.Fragment.ChatFragment;
import com.example.linkup.Fragment.CommunityFragment;
import com.example.linkup.Fragment.EventFragment;
import com.example.linkup.Fragment.HomeFragment;
import com.example.linkup.Fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    // Firebase features
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // [START gain layout objects]
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navbar);
        // [END gain]
        // setting - bottom navigation bar
        bottomNav.setSelectedItemId(R.id.profile);
        bottomNav.setOnItemSelectedListener(navListener);

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        //[END configuration]

        // Check if the user is logged in
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Fragment selectedFragment = new ProfileFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }
    }

    // Navigation Bar View Direction
    private NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int itemId = item.getItemId(); /* obtain the selected item ID from your source */
                Fragment selectedFragment = null;
                if (itemId == R.id.home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.community) {
                    selectedFragment = new CommunityFragment();
                } else if (itemId == R.id.chat) {
                    selectedFragment = new ChatFragment();
                } else if (itemId == R.id.event) {
                    selectedFragment = new EventFragment();
                } else if (itemId == R.id.profile) {
                    selectedFragment = new ProfileFragment();
                } else {
                    return false;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            };
}

