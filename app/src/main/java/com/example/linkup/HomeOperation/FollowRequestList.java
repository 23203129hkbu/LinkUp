package com.example.linkup.HomeOperation;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Adapter.FollowRequestAdapter;
import com.example.linkup.Adapter.PostAdapter;
import com.example.linkup.Adapter.UserAdapter;
import com.example.linkup.Object.Posts;
import com.example.linkup.Object.Users;
import com.example.linkup.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FollowRequestList extends AppCompatActivity {
    // layout object
    ImageView btnBack;
    RecyclerView requestRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseRequestedRef; // real-time db ref
    // convert post data into RecyclerView by Adapter
    ArrayList<Users> usersArrayList = new ArrayList<>();
    FollowRequestAdapter followRequestAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_request_list);
        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        requestRV = findViewById(R.id.requestRV);
        // [END gain]

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseRequestedRef = Rdb.getReference().child("user").child(auth.getUid()).child("requested");
        // [END config_firebase reference]

        // Load users from Firebase
        databaseRequestedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                usersArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user != null) {
                        usersArrayList.add(user);
                    }
                }
                followRequestAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(FollowRequestList.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize Adapter with an empty list
        followRequestAdapter = new FollowRequestAdapter(this, usersArrayList); // Only filteredUsers are shown
        requestRV.setLayoutManager(new LinearLayoutManager(this));
        requestRV.setHasFixedSize(true);
        requestRV.setAdapter(followRequestAdapter);

        // [START layout component function]
        // Switch the screen - Home Fragment
        btnBack.setOnClickListener(v -> finish());
        // [END layout component function]
    }
}