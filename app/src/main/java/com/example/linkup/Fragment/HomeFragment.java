package com.example.linkup.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Adapter.ArticleAdapter;
import com.example.linkup.Adapter.PostAdapter;
import com.example.linkup.HomeOperation.CreatePost;
import com.example.linkup.HomeOperation.FollowRequestList;
import com.example.linkup.HomeOperation.SearchUser;
import com.example.linkup.HomeOperation.UserProfile;
import com.example.linkup.Object.Articles;
import com.example.linkup.Object.Posts;
import com.example.linkup.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    View view;
    // layout object
    ImageView btnSearch, btnNotification;
    FloatingActionButton btnAdd;
    RecyclerView postRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databasePostRef, databaseRequestedRef; // real-time db ref
    // convert post data into RecyclerView by Adapter
    ArrayList<Posts> postsArrayList = new ArrayList<>();
    PostAdapter postAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_home_fragment, container, false);
        // [START gain layout objects]
        btnSearch = view.findViewById(R.id.btnSearch);
        btnNotification = view.findViewById(R.id.btnNotification);
        btnAdd = view.findViewById(R.id.btnAdd);
        postRV = view.findViewById(R.id.postRV);
        // [END gain]

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databasePostRef = Rdb.getReference().child("post");
        databaseRequestedRef = Rdb.getReference().child("user").child(auth.getUid()).child("requested");
        // [END config_firebase reference]

        // Gain the adapter data object
        databasePostRef.orderByChild("date").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postsArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Posts post = dataSnapshot.getValue(Posts.class);
                    // Ensure article is not null before proceeding
                    if (post != null) {
                        postsArrayList.add(post);
                    }
                }

                // Sort the articles after all have been added to the list
                postsArrayList.sort((a1, a2) -> {
                    // First, compare by date
                    int dateComparison = a2.getDate().compareTo(a1.getDate());
                    if (dateComparison == 0) {
                        // If dates are equal, compare by time
                        return a2.getTime().compareTo(a1.getTime());
                    }
                    return dateComparison;
                });
                // Notify adapter after sorting
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
        // Determine if there are any tracking requests
        databaseRequestedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // layout control
                    // Set btnNotification layout
                    btnNotification.setImageResource(R.drawable.baseline_notifications_active_24);
                }else{
                    btnNotification.setImageResource(R.drawable.baseline_notifications_none_24);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Grant value - which view, posts array list
        postAdapter = new PostAdapter(getContext(), postsArrayList);
        // Set up the layout manager, adapter
        postRV.setLayoutManager(new LinearLayoutManager(getContext()));
        postRV.setHasFixedSize(true);
        postRV.setAdapter(postAdapter);

        // [START layout component function]
        // Switch the screen - Search User
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI("Search");
            }
        });
        // Switch the screen - Update Profile
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI("Create");
            }
        });
        // Switch the screen - Notification List
        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI("Notification");
            }
        });
        // [END layout component function]

        // this line must be finalized
        return view;
    }
    // [START Method]
    // handling UI update
    private void updateUI(String screen) {
        Intent intent = null;
        if (screen.equals("Search")) {
            intent = new Intent(getContext(), SearchUser.class);
        } else if (screen.equals("Notification")) {
            intent = new Intent(getContext(), FollowRequestList.class);
        } else if (screen.equals("Create")) {
            intent = new Intent(getContext(), CreatePost.class);
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
    // [END Method]
}
