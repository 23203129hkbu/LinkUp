package com.example.linkup.HomeOperation;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Adapter.FollowingAdapter;
import com.example.linkup.Adapter.UserAdapter;
import com.example.linkup.Object.Users;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;

public class FollowingActivity extends AppCompatActivity {
    // Layout objects
    ImageView btnBack;
    SearchView searchBar;
    RecyclerView followingRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb;
    DatabaseReference databaseUserRef, databaseFollowingRef;
    // User Info
    Users user = new Users();
    // User list and adapter
    ArrayList<Users> usersArrayList = new ArrayList<>(); // store all user data
    ArrayList<Users> filteredUsers = new ArrayList<>(); // Update the result (after filter)
    UserAdapter userAdapter;
    FollowingAdapter followingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);
        // [START gain value from other activity]
        user = (Users) getIntent().getSerializableExtra("user");
        // [END gain]

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user");
        databaseFollowingRef = Rdb.getReference().child("following").child(user.getUID());
        // [END config_firebase reference]

        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        searchBar = findViewById(R.id.searchBar);
        followingRV = findViewById(R.id.followingRV);
        // [END gain]

        // [START config_layout]
        // Load users from Firebase (but do not display them initially)
        databaseFollowingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(FollowingActivity.this, "You are not following anyone.", Toast.LENGTH_SHORT).show();
                    return;
                }

                HashSet<String> followingUIDs = new HashSet<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    followingUIDs.add(dataSnapshot.getKey());
                }

                databaseUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        usersArrayList.clear();
                        filteredUsers.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Users following = dataSnapshot.getValue(Users.class);
                            if (following != null && followingUIDs.contains(following.getUID())) {
                                usersArrayList.add(following);
                            }
                        }

                        filteredUsers.addAll(usersArrayList);
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(FollowingActivity.this, "Failed to get users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FollowingActivity.this, "Failed to get following list: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Initialize Adapter with an empty list
        userAdapter = new UserAdapter(this, filteredUsers); // Only filteredUsers are shown
        followingRV.setLayoutManager(new LinearLayoutManager(this));
        followingRV.setHasFixedSize(true);
        // Depends on whether the profile is used by the user
        if (user.getUID().equals(auth.getUid())){
            followingRV.setAdapter(userAdapter);
        }else {
            followingRV.setAdapter(followingAdapter);
        }

        // [END config_layout]

        // [START layout component function]
        // Back button action
        btnBack.setOnClickListener(v -> finish());
        // Search functionality
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });

        // [END layout component function]
    }

    // [START Method]
    // Filter users based on search input
    // essential for case-insensitive searchc
    private void filterUsers(String text) {
        filteredUsers.clear();
        if (text.isEmpty()) {
            filteredUsers.addAll(usersArrayList);
        } else {
            for (Users user : usersArrayList) {
                if (user.getUsername().toLowerCase().contains(text.toLowerCase())) {
                    filteredUsers.add(user);
                }
            }
        }

        userAdapter.notifyDataSetChanged(); // Refresh the adapter with the filtered list
    }
    // [END Method]
}