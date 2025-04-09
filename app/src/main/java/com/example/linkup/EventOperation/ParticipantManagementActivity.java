package com.example.linkup.EventOperation;

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
import com.example.linkup.Adapter.ParticipantAdapter;
import com.example.linkup.Adapter.UserAdapter;
import com.example.linkup.HomeOperation.FollowingActivity;
import com.example.linkup.Object.Events;
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

public class ParticipantManagementActivity extends AppCompatActivity {
    // Layout objects
    ImageView btnBack;
    SearchView searchBar;
    RecyclerView participantRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb;
    DatabaseReference databaseUserRef,databaseParticipantRef;
    // Event info
    Events event = new Events();
    ArrayList<Users> usersArrayList = new ArrayList<>(); // store all user data
    ArrayList<Users> filteredUsers = new ArrayList<>(); // Update the result (after filter)
    ParticipantAdapter participantAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_management);
        // Retrieve the event object from the intent
        event = (Events) getIntent().getSerializableExtra("event");
        // [END gain]

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user");
        databaseParticipantRef = Rdb.getReference().child("eventParticipant").child(event.getEventID());
        // [END config_firebase reference]

        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        searchBar = findViewById(R.id.searchBar);
        participantRV = findViewById(R.id.participantRV);
        // [END gain]


        // [START config_layout]
        // Load users from Firebase (but do not display them initially)
        databaseParticipantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashSet<String> participantUIDs = new HashSet<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    participantUIDs.add(dataSnapshot.getKey());
                }

                databaseUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        usersArrayList.clear();
                        filteredUsers.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Users participant = dataSnapshot.getValue(Users.class);
                            if (participant != null && participantUIDs.contains(participant.getUID())) {
                                usersArrayList.add(participant);
                            }
                        }

                        filteredUsers.addAll(usersArrayList);
                        participantAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ParticipantManagementActivity.this, "Failed to get users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParticipantManagementActivity.this, "Failed to get following list: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Initialize Adapter with an empty list
        participantAdapter = new ParticipantAdapter(this, filteredUsers,event); // Only filteredUsers are shown
        participantRV.setLayoutManager(new LinearLayoutManager(this));
        participantRV.setHasFixedSize(true);
        // Depends on whether the profile is used by the user
        participantRV.setAdapter(participantAdapter);

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

        participantAdapter.notifyDataSetChanged(); // Refresh the adapter with the filtered list
    }
    // [END Method]
}