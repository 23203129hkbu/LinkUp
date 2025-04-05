package com.example.linkup.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.linkup.HomeOperation.FollowerActivity;
import com.example.linkup.HomeOperation.FollowingActivity;
import com.example.linkup.HomeOperation.UserProfile;
import com.example.linkup.Object.Posts;
import com.example.linkup.Object.Users;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.example.linkup.ProfileOperation.CreateProfile;
import com.example.linkup.ProfileOperation.TabbedView.SectionsPagerAdapter;
import com.example.linkup.ProfileOperation.UpdateProfile;
import com.example.linkup.R;
import com.example.linkup.ProfileOperation.SettingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

// ✅
public class ProfileFragment extends Fragment {
    View view;
    // layout object
    LinearLayout btnFollowers, btnFollowing;
    CircleImageView avatar;
    ImageView btnEdit, btnSetting;
    TextView username, introduction, website, state, posts, followers, following;
    // Tabbed View
    SectionsPagerAdapter adapter;
    ViewPager2 tabbedView;
    TabLayout tab;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef, databaseFollowerRef, databaseFollowingRef, databasePostRef; // real-time db ref
    // default user info
    // User Info
    Users user = new Users();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_profile_fragment, container, false);
        // [START config_firebase]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        // [END config_firebase]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user").child(auth.getUid());
        databasePostRef = Rdb.getReference().child("post");
        databaseFollowerRef = Rdb.getReference().child("follower").child(auth.getUid());
        databaseFollowingRef = Rdb.getReference().child("following").child(auth.getUid());
        // [END config_firebase reference]

        // [START gain layout objects]
        avatar = view.findViewById(R.id.avatar);
        username = view.findViewById(R.id.username);
        website = view.findViewById(R.id.website);
        introduction = view.findViewById(R.id.introduction);
        state = view.findViewById(R.id.state);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnSetting = view.findViewById(R.id.btnSetting);
        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        // Tabbed view
        tabbedView = view.findViewById(R.id.tabbedView);
        tab = view.findViewById(R.id.tab);
        // Search Follower, Following
        btnFollowers = view.findViewById(R.id.btnFollowers);
        btnFollowing = view.findViewById(R.id.btnFollowing);
        // [END gain]

        // [START config_layout]
        // [Gain User Profile]
        databaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    user = snapshot.getValue(Users.class);
                    // Layout Control
                    username.setText(user.getUsername());
                    Picasso.get().load(user.getAvatarURL()).into(avatar);
                    state.setText(user.getPrivacy());
                    website.setText(user.getWebsite());
                    introduction.setText(user.getIntroduction());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Count Posts
        databasePostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int numOfPost = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Posts post = dataSnapshot.getValue(Posts.class);
                    // post created by this user
                    if (post != null && post.getUID().equals(auth.getUid())) {
                        numOfPost += 1;
                    }
                }
                // Count total posts
                posts.setText(String.valueOf(numOfPost));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
        // Count Followers
        databaseFollowerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int numOfFollower = 0;
                // Count total followers
                numOfFollower = (int) snapshot.getChildrenCount();
                followers.setText(String.valueOf(numOfFollower));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
        // Count Following
        databaseFollowingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int numOfFollowing = 0;
                // Count total following
                numOfFollowing = (int) snapshot.getChildrenCount();
                following.setText(String.valueOf(numOfFollowing));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
        // [END config_layout]

        // Setup ViewPager
        adapter = new SectionsPagerAdapter(getActivity(), auth.getUid());
        tabbedView.setAdapter(adapter);
        // Use TabLayoutMediator to link TabLayout with ViewPager2
        new TabLayoutMediator(tab, tabbedView, (tab, position) -> {
            if (position == 0) {
                tab.setIcon(R.drawable.baseline_camera_alt_24);  // Set tab icon
            } else if (position == 1) {
                tab.setIcon(R.drawable.baseline_videocam_24);
            }
        }).attach();


        // [START layout component function]
        // Switch the screen - Update Profile
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI("Update");
            }
        });
        // Logout with dialog message
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI("Setting");
            }
        });
        // Switch the screen - User Followers List
        btnFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI("Follower");
            }
        });
        // Switch the screen - User Following List
        btnFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI("Following");
            }
        });
        // Logout with dialog message
        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String webUrl = user.getWebsite();
                    Intent webIntent = new Intent(Intent.ACTION_VIEW);
                    webIntent.setData(Uri.parse(webUrl));
                    startActivity(webIntent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Invalid Link", Toast.LENGTH_SHORT).show();
                }
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
        if (screen.equals("Setting")) {
            intent = new Intent(getContext(), SettingActivity.class);
        } else if (screen.equals("Update")) {
            intent = new Intent(getContext(), UpdateProfile.class);
        } else if (screen.equals("Create")) {
            intent = new Intent(getContext(), CreateProfile.class);
        } else if (screen.equals("Follower")) {
            intent = new Intent(getContext(), FollowerActivity.class);
            intent.putExtra("user", user);  // Pass the user object
        }else if (screen.equals("Following")) {
            intent = new Intent(getContext(), FollowingActivity.class);
            intent.putExtra("user", user);  // Pass the user object
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
    // [END Method]
}