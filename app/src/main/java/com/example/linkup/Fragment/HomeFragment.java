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
import com.example.linkup.HomeOperation.NotificationActivity;
import com.example.linkup.HomeOperation.SearchUser;
import com.example.linkup.HomeOperation.UserProfile;
import com.example.linkup.Object.Articles;
import com.example.linkup.Object.Modifications;
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
import java.util.HashSet;
/*
Case 1: delete post
Case 2: private account
Case 3: unfollow user -> private
*/

// ✅
public class HomeFragment extends Fragment {
    View view;
    // layout object
    ImageView btnSearch, btnNotification;
    FloatingActionButton btnAdd;
    RecyclerView postRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databasePostRef, databaseRequestedRef, databaseUserRef, databaseFollowingRef, databaseInvitationRef, databaseModificationRef; // real-time db ref
    // convert post data into RecyclerView by Adapter
    ArrayList<Posts> postsArrayList = new ArrayList<>();
    PostAdapter postAdapter;
    HashSet<String> addedPostIDs = new HashSet<>();
    // Notification
    Boolean requestChecker = false;
    Boolean invitationChecker = false;
    Boolean modificationChecker = false;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_home_fragment, container, false);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databasePostRef = Rdb.getReference().child("post");
        databaseRequestedRef = Rdb.getReference().child("requested").child(auth.getUid());
        databaseUserRef = Rdb.getReference().child("user");
        databaseFollowingRef = Rdb.getReference().child("following").child(auth.getUid());
        databaseInvitationRef = Rdb.getReference().child("invitation").child(auth.getUid());
        databaseModificationRef = Rdb.getReference().child("modification").child(auth.getUid());
        // [END config_firebase reference]

        // [START gain layout objects]
        btnSearch = view.findViewById(R.id.btnSearch);
        btnNotification = view.findViewById(R.id.btnNotification);
        btnAdd = view.findViewById(R.id.btnAdd);
        postRV = view.findViewById(R.id.postRV);
        // [END gain]

        // [START config_layout]
        // Grant value - which view, posts array list
        postAdapter = new PostAdapter(getContext(), postsArrayList);
        // Set up the layout manager, adapter
        postRV.setLayoutManager(new LinearLayoutManager(getContext()));
        postRV.setHasFixedSize(true);
        postRV.setAdapter(postAdapter);
        // Determine if there are any follow requests
        databaseRequestedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    requestChecker = true;
                } else {
                    requestChecker = false;
                }
                loadNotification();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Determine if there are any follow invitation
        databaseInvitationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    invitationChecker = true;
                } else {
                    invitationChecker = false;
                }
                loadNotification();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Determine if there are any follow invitation
        databaseModificationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                modificationChecker = false;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Modifications modification = dataSnapshot.getValue(Modifications.class);
                    if (modification != null && !modification.getRead()) {
                        modificationChecker = true;
                    }
                }
                loadNotification();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // monitor for user, post, following relationship
        monitorDataChanges();
        // [END config_layout]

        // [START layout component function]
        // Switch the screen - Search User
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI("Search");
            }
        });
        // Switch the screen - Create Post
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

    private void loadNotification() {
        if (requestChecker||invitationChecker||modificationChecker){
            btnNotification.setImageResource(R.drawable.baseline_notifications_active_24);
        }else {
            btnNotification.setImageResource(R.drawable.baseline_notifications_none_24);
        }
    }

    private void monitorDataChanges() {
        // Pass data into arrayList , then post adapter received
        databasePostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot postSnapshot) {
                reloadPosts(postSnapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load articles: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Public user checker
        // Listen for changes in user privacy settings
        databaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                    reloadPosts(null); // Reload the post
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load posts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Followed user checker
        databaseFollowingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot followingSnapshot) {
                reloadPosts(null); // Reload the post
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load posts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    // [START Method]
    // refresh posts
    private void reloadPosts(@Nullable DataSnapshot postSnapshot) {
        postsArrayList.clear();
        addedPostIDs.clear();

        if (postSnapshot == null) {
            // If postSnapshot is null, reload data from Firebase
            databasePostRef.orderByChild("date").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Posts post = dataSnapshot.getValue(Posts.class);
                        if (post != null) {
                            if (post.getUID().equals(auth.getUid())) {
                                addPostIfNotExists(post);
                            } else {
                                checkPostVisibility(post);
                            }
                        }
                    }
                    finalizePostLoading();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to reload posts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // If postSnapshot is not null, process the data directly
            for (DataSnapshot dataSnapshot : postSnapshot.getChildren()) {
                Posts post = dataSnapshot.getValue(Posts.class);
                if (post != null) {
                    if (post.getUID().equals(auth.getUid())) {
                        addPostIfNotExists(post);
                    } else {
                        checkPostVisibility(post);
                    }
                }
            }
            finalizePostLoading();
        }
    }

    private void checkPostVisibility(Posts post) {
        databaseUserRef.child(post.getUID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    String privacy = userSnapshot.child("privacy").getValue(String.class);
                    if ("Public".equals(privacy)) {
                        addPostIfNotExists(post);
                    } else {
                        checkIfUserIsFollowed(post);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to check user privacy: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfUserIsFollowed(Posts post) {
        databaseFollowingRef.child(post.getUID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot followingSnapshot) {
                if (followingSnapshot.exists()) {
                    addPostIfNotExists(post);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to check following status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPostIfNotExists(Posts post) {
        if (addedPostIDs.add(post.getPostID())) {
            postsArrayList.add(post);
        }
    }

    private void finalizePostLoading() {
        postsArrayList.sort((a1, a2) -> a2.getDate().compareTo(a1.getDate()));
        postAdapter.notifyDataSetChanged();
    }
    // handling UI update
    private void updateUI(String screen) {
        Intent intent = null;
        if (screen.equals("Search")) {
            intent = new Intent(getContext(), SearchUser.class);
        } else if (screen.equals("Notification")) {
            intent = new Intent(getContext(), NotificationActivity.class);
        } else if (screen.equals("Create")) {
            intent = new Intent(getContext(), CreatePost.class);
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
    // [END Method]
}
