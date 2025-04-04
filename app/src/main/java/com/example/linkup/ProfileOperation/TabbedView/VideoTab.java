package com.example.linkup.ProfileOperation.TabbedView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Adapter.ImageAdapter;
import com.example.linkup.Adapter.VideoAdapter;
import com.example.linkup.Object.Posts;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

// ✅
public class VideoTab extends Fragment {
    View view;
    // layout object
    RecyclerView videoRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databasePostRef; // real-time db ref
    // convert article data into RecyclerView by Adapter
    ArrayList<Posts> postsArrayList = new ArrayList<>();
    VideoAdapter videoAdapter;
    // Determine which user it belongs to
    String uid;

    public VideoTab(String uid) {
        this.uid = uid;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_video_tab, container, false);
        // [START gain layout objects]
        videoRV = view.findViewById(R.id.videoRV);
        // [END gain]

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databasePostRef = Rdb.getReference().child("post");
        // [END config_firebase reference]

        // Gain the adapter data object
        databasePostRef.orderByChild("date").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postsArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Posts post = dataSnapshot.getValue(Posts.class);
                    // post created by this user , type = video
                    if (post != null && post.getUID().equals(uid) && post.getType().equals("video")) {
                        postsArrayList.add(post);
                    }
                }

                // Sort the video post
                postsArrayList.sort((a1, a2) -> {
                    // Compare by date (descending)
                    int dateComparison = a2.getDate().compareTo(a1.getDate());
                    return dateComparison;
                });
                // Notify adapter after sorting
                videoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
        // Grant value - which view, posts array list
        videoAdapter = new VideoAdapter(getContext(), postsArrayList);
        // Set up the layout manager, adapter
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        videoRV.setLayoutManager(gridLayoutManager);
        videoRV.setHasFixedSize(true);
        videoRV.setAdapter(videoAdapter);
        // this line must be finalized
        return view;
    }
}