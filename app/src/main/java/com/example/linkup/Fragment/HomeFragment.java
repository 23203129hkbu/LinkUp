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
    DatabaseReference databaseUserRef, databasePostRef, databaseRequestedRef, databaseFollowingRef; // real-time db ref
    // convert post data into RecyclerView by Adapter
    ArrayList<Posts> postsArrayList = new ArrayList<>();
    ArrayList<String> followingUIDs = new ArrayList<>();
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
        databaseUserRef = Rdb.getReference().child("user");
        databasePostRef = Rdb.getReference().child("post");
        databaseFollowingRef = Rdb.getReference().child("following").child(auth.getUid());
        databaseRequestedRef = Rdb.getReference().child("requested").child(auth.getUid());
        // [END config_firebase reference]

        // Load Post
        databaseFollowingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot followingSnapshot) {
                followingUIDs.clear();
                for (DataSnapshot snap : followingSnapshot.getChildren()) {
                    followingUIDs.add(snap.getKey());
                }

                databasePostRef.orderByChild("date").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postsArrayList.clear();
                        ArrayList<Posts> tempPostsList = new ArrayList<>();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Posts post = dataSnapshot.getValue(Posts.class);
                            if (post != null) {
                                tempPostsList.add(post);
                            }
                        }

                        filterVisiblePosts(tempPostsList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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
                } else {
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

    private void filterVisiblePosts(ArrayList<Posts> allPosts) {
        ArrayList<Posts> filtered = new ArrayList<>();
        int totalToCheck = allPosts.size();
        final int[] checkedCount = {0};

        for (Posts post : allPosts) {
            String postUID = post.getUID();
            if (followingUIDs.contains(postUID) || postUID.equals(auth.getUid())) {
                filtered.add(post);
                checkedCount[0]++;
                if (checkedCount[0] == totalToCheck) finalizePosts(filtered);
            } else {
                databaseUserRef.child(postUID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                        checkedCount[0]++;
                        if (userSnapshot.exists() && "Public".equals(userSnapshot.child("privacy").getValue(String.class))) {
                            filtered.add(post);
                        }
                        if (checkedCount[0] == totalToCheck) finalizePosts(filtered);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        checkedCount[0]++;
                        if (checkedCount[0] == totalToCheck) finalizePosts(filtered);
                    }
                });
            }
        }
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
    // 檢查是否所有貼文已讀取完成
    private boolean isAllPostsLoaded(ArrayList<Posts> tempPostsList, DataSnapshot snapshot) {
        return tempPostsList.size() == snapshot.getChildrenCount();
    }

    // 排序並更新 RecyclerView
    private void finalizePosts(ArrayList<Posts> tempPostsList) {
        // 排序：日期最新優先
        tempPostsList.sort((a1, a2) -> {
            int dateComparison = a2.getDate().compareTo(a1.getDate());
            if (dateComparison == 0) {
                return a2.getTime().compareTo(a1.getTime());
            }
            return dateComparison;
        });

        postsArrayList.clear();
        postsArrayList.addAll(tempPostsList);

        // 通知適配器更新
        postAdapter.notifyDataSetChanged();
    }
    // [END Method]
}
