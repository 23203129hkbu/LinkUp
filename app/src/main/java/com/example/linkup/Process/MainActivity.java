package com.example.linkup.Process;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.linkup.Fragment.ChatFragment;
import com.example.linkup.Fragment.CommunityFragment;
import com.example.linkup.Fragment.EventFragment;
import com.example.linkup.Fragment.HomeFragment;
import com.example.linkup.Fragment.ProfileFragment;
import com.example.linkup.ProfileOperation.CreateProfile;
import com.example.linkup.R;
import com.example.linkup.SocialLogin.FacebookSignInActivity;
import com.example.linkup.SocialLogin.GoogleSignInActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// ✅
public class MainActivity extends AppCompatActivity {
    // layout object
    BottomNavigationView bottomNav;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef;
    // Fragment
    Fragment selectedFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // Config object
        selectedFragment = null;

        // Check if the user is logged in
        if (auth.getCurrentUser() == null) {
            updateUI("Login");
        } else {
            // [START config_firebase reference]
            databaseUserRef = Rdb.getReference().child("user").child(auth.getUid());
            // [END config_firebase reference]
        }

        // [START gain layout objects]
        bottomNav = findViewById(R.id.bottom_navbar);
        // [END gain]

        // [START Configure layout]
        databaseUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    // If user profile exists, navigate to HomeFragment
                    bottomNav.setSelectedItemId(R.id.home);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();
                } else {
                    // If user profile does not exist, redirect to CreateProfile screen
                    updateUI("Create");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors
            }
        });

        // Set up bottom navigation listener
        bottomNav.setOnItemSelectedListener(navListener);
        // [END Configure layout]
    }

    // [START Method]
    // Navigation Bar View Direction
    private NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int itemId = item.getItemId(); /* obtain the selected item ID from your source */
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
                // Replace the fragment
                if (selectedFragment != null && !getSupportFragmentManager().isStateSaved()) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
                return true;
            };
    // handling UI update
    private void updateUI(String screen) {
        Intent intent = null;
        if (screen.equals("Login")) {
            intent = new Intent(MainActivity.this, LoginActivity.class);
        } else if (screen.equals("Create")) {
            intent = new Intent(MainActivity.this, CreateProfile.class);
        }
        if (intent != null) {
            startActivity(intent);
            finish();
        }
    }
    // [END Method]
}

