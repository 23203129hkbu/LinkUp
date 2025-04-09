package com.example.linkup.HomeOperation.TabbedView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Adapter.FollowRequestAdapter;
import com.example.linkup.Adapter.ImageAdapter;
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

public class FollowRequestTab extends Fragment {
    View view;
    // layout object
    RecyclerView followRequestRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseRequestedRef; // real-time db ref
    // convert post data into RecyclerView by Adapter
    ArrayList<Users> usersArrayList = new ArrayList<>();
    FollowRequestAdapter followRequestAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_follow_request_tab, container, false);

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseRequestedRef = Rdb.getReference().child("requested").child(auth.getUid());
        // [END config_firebase reference]

        // [START gain layout objects]
        followRequestRV = view.findViewById(R.id.followerRequestRV);
        // [END gain]


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
                Toast.makeText(getContext(), "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize Adapter with an empty list
        followRequestAdapter = new FollowRequestAdapter(getContext(), usersArrayList); // Only filteredUsers are shown
        followRequestRV.setLayoutManager(new LinearLayoutManager(getContext()));
        followRequestRV.setHasFixedSize(true);
        followRequestRV.setAdapter(followRequestAdapter);
        // this line must be finalized
        return view;
    }
}