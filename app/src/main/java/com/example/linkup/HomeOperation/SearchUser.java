package com.example.linkup.HomeOperation;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class SearchUser extends AppCompatActivity {
    // Layout objects
    ImageView btnBack;
    SearchView searchBar;
    RecyclerView userRV;

    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb;
    DatabaseReference databaseUserRef;

    // User list and adapter
    ArrayList<Users> usersArrayList = new ArrayList<>();
    ArrayList<Users> filteredUsers = new ArrayList<>();
    UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        // Initialize UI components
        btnBack = findViewById(R.id.btnBack);
        searchBar = findViewById(R.id.searchBar);
        userRV = findViewById(R.id.userRV);

        // Firebase setup
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        databaseUserRef = Rdb.getReference().child("user");

        // Initialize Adapter with an empty list
        userAdapter = new UserAdapter(this, filteredUsers); // Only filteredUsers are shown
        userRV.setLayoutManager(new LinearLayoutManager(this));
        userRV.setHasFixedSize(true);
        userRV.setAdapter(userAdapter);

        // Load users from Firebase (but do not display them initially)
        loadUsers();

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
    }

    // Load users from Firebase
    private void loadUsers() {
        databaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                usersArrayList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user != null) {
                        usersArrayList.add(user);
                    }
                }

                // Do not display any users until a search query is entered
                filteredUsers.clear();
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SearchUser.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Filter users based on search input
    private void filterUsers(String text) {
        filteredUsers.clear();

        if (!text.isEmpty()) {
            for (Users user : usersArrayList) {
                if (user.getUsername().toLowerCase().contains(text.toLowerCase())) {
                    filteredUsers.add(user);
                }
            }
        }

        userAdapter.notifyDataSetChanged(); // Refresh the adapter with the filtered list
    }
}