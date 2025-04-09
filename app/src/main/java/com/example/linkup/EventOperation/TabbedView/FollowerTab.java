package com.example.linkup.EventOperation.TabbedView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Adapter.FollowerAdapter;
import com.example.linkup.Adapter.ImageAdapter;
import com.example.linkup.Adapter.InvitationAdapter;
import com.example.linkup.Adapter.UserAdapter;
import com.example.linkup.HomeOperation.FollowerActivity;
import com.example.linkup.Object.Events;
import com.example.linkup.Object.Posts;
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

public class FollowerTab extends Fragment {
    View view;
    // layout object
    RecyclerView followerRV;
    SearchView searchBar;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef, databaseFollowerRef; // real-time db ref
    // convert article data into RecyclerView by Adapter
    ArrayList<Users> usersArrayList = new ArrayList<>();
    ArrayList<Users> filteredUsers = new ArrayList<>(); // Update the result (after filter)
    InvitationAdapter invitationAdapter;
    // Store which event
    Events event;

    public FollowerTab(Events event) {
        this.event = event;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_follower_tab, container, false);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user");
        databaseFollowerRef = Rdb.getReference().child("follower").child(auth.getUid());
        // [END config_firebase reference]

        // [START gain layout objects]
        searchBar = view.findViewById(R.id.searchBar);
        followerRV = view.findViewById(R.id.followerRV);
        // [END gain]

        databaseFollowerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashSet<String> followerUIDs = new HashSet<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    followerUIDs.add(dataSnapshot.getKey());
                }

                databaseUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        usersArrayList.clear();
                        filteredUsers.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Users follower = dataSnapshot.getValue(Users.class);
                            if (follower!= null && followerUIDs.contains(follower.getUID())) {
                                usersArrayList.add(follower);
                            }
                        }

                        filteredUsers.addAll(usersArrayList);
                        invitationAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to get users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to get following list: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Initialize Adapter with an empty list
        invitationAdapter = new InvitationAdapter(getContext(), filteredUsers, event); // Only filteredUsers are shown
        followerRV.setLayoutManager(new LinearLayoutManager(getContext()));
        followerRV.setHasFixedSize(true);
        // Depends on whether the profile is used by the user
        followerRV.setAdapter(invitationAdapter);
        // [END config_layout]

        // [START layout component function]
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
        // this line must be finalized
        return view;
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

        invitationAdapter.notifyDataSetChanged(); // Refresh the adapter with the filtered list
    }
    // [END Method]
}