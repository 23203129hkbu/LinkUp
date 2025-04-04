package com.example.linkup.HomeOperation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.linkup.CommunityOperation.ArticleActivity;
import com.example.linkup.Object.Articles;
import com.example.linkup.Object.Posts;
import com.example.linkup.Object.Users;
import com.example.linkup.ProfileOperation.CreateProfile;
import com.example.linkup.ProfileOperation.SettingActivity;
import com.example.linkup.ProfileOperation.TabbedView.SectionsPagerAdapter;
import com.example.linkup.ProfileOperation.UpdateProfile;
import com.example.linkup.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

// ✅
public class UserProfile extends AppCompatActivity {
    // layout object
    LinearLayout profile, privateAccountHint, btnFollowers, btnFollowing;
    CircleImageView avatar;
    ImageView btnBack;
    TextView username, introduction, website, posts, followers, following, btnFollow, usernameTopBar;
    // Tabbed View
    SectionsPagerAdapter adapter;
    ViewPager2 tabbedView;
    TabLayout tab;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef, databaseFollowerRef, databaseFollowingRef, databaseRequestedRef, databaseYourFollowingRef, databasePostRef; // real-time db ref
    // User Info
    Users user = new Users();
    String userWebsite; // for url click
    // Checker Followed / UnFollowed / Requested / Public / Private
    Boolean Followed = false;
    Boolean privateAC = false;
    Boolean Requested = false;
    // Confirm button The next action depends on the account type

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        // [START gain value from other activity]
        user = (Users) getIntent().getSerializableExtra("user");
        // [END gain]

        // [START config_firebase]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        // [END config_firebase]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user").child(user.getUID());
        databasePostRef = Rdb.getReference().child("post");
        databaseFollowerRef = Rdb.getReference().child("follower").child(user.getUID());
        databaseFollowingRef = Rdb.getReference().child("following").child(user.getUID());
        databaseYourFollowingRef = Rdb.getReference().child("following").child(auth.getUid());
        // For private Account
        databaseRequestedRef = Rdb.getReference().child("requested").child(user.getUID());
        // [END config_firebase reference]

        // [START gain layout objects]
        usernameTopBar = findViewById(R.id.usernameTopBar);
        avatar = findViewById(R.id.avatar);
        username = findViewById(R.id.username);
        website = findViewById(R.id.website);
        introduction = findViewById(R.id.introduction);
        btnBack = findViewById(R.id.btnBack);
        posts = findViewById(R.id.posts);
        followers = findViewById(R.id.followers);
        following = findViewById(R.id.following);
        btnFollow = findViewById(R.id.btnFollow);
        // Tabbed view
        tabbedView = findViewById(R.id.tabbedView);
        tab = findViewById(R.id.tab);
        // Profile
        profile = findViewById(R.id.profile);
        privateAccountHint = findViewById(R.id.privateAccountHint);
        btnFollowers = findViewById(R.id.btnFollowers);
        btnFollowing = findViewById(R.id.btnFollowing);
        // [END gain]

        // [START config_layout]
        // Load / Gain User Data -> Determine user status private / public
        databaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userWebsite = snapshot.child("website").getValue(String.class);
                    // Layout Control
                    Picasso.get().load(snapshot.child("avatarURL").getValue(String.class)).into(avatar);
                    username.setText(snapshot.child("username").getValue(String.class));
                    usernameTopBar.setText(snapshot.child("username").getValue(String.class));
                    website.setText(userWebsite);
                    introduction.setText(snapshot.child("introduction").getValue(String.class));
                    if (snapshot.child("privacy").getValue(String.class).equals("Private")) {
                        privateAC = true;
                    } else {
                        privateAC = false;
                        Requested = false;
                    }
                    // If User info updated -> When the user privacy setting is changed to Private or Public
                    handleUserProfile();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfile.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                    if (post != null && post.getUID().equals(user.getUID())) {
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
        // Count Followers and Check Followers -> show button Follow text
        databaseFollowerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int numOfFollower = 0;
                // Count total follower
                numOfFollower = (int) snapshot.getChildrenCount();
                followers.setText(String.valueOf(numOfFollower));
                // check if a follow request exists
                if (snapshot.hasChild(auth.getUid())) {
                    Followed = true;
                } else {
                    Followed = false;
                }
                handleUserProfile();
                handleButtonFollow();
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
        // If follow request is change -> when private Account allow the user follow
        databaseRequestedRef.child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Requested = true;
                } else {
                    Requested = false;
                }
                handleUserProfile();
                handleButtonFollow();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfile.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Setup ViewPager
        adapter = new SectionsPagerAdapter(UserProfile.this, user.getUID());
        tabbedView.setAdapter(adapter);
        // Use TabLayoutMediator to link TabLayout with ViewPager2
        new TabLayoutMediator(tab, tabbedView, (tab, position) -> {
            if (position == 0) {
                tab.setIcon(R.drawable.baseline_camera_alt_24);  // Set tab icon
            } else if (position == 1) {
                tab.setIcon(R.drawable.baseline_videocam_24);
            }
        }).attach();
        // [END config_layout]

        // [START layout component function]
        // Switch the screen - Home Page
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Switch the screen - User Followers List
        btnFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Followed || !privateAC) {
                    Intent intent = new Intent(UserProfile.this, FollowerActivity.class);
                    intent.putExtra("user", user);  // Pass the user object
                    startActivity(intent);
                }
            }
        });
        // Switch the screen - User Following List
        btnFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Followed || !privateAC) {
                    Intent intent = new Intent(UserProfile.this, FollowingActivity.class);
                    intent.putExtra("user", user);  // Pass the user object
                    startActivity(intent);
                }
            }
        });
        // website url Link
        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String webUrl = userWebsite;
                    Intent webIntent = new Intent(Intent.ACTION_VIEW);
                    webIntent.setData(Uri.parse(webUrl));
                    startActivity(webIntent);
                } catch (Exception e) {
                    Toast.makeText(UserProfile.this, "Invalid Link", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Follow / Unfollowed the user
        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Followed) {
                    removeFollowerAndFollowing();
                } else {
                    if (privateAC) {
                        if (Requested) {
                            cancelFollowRequest();
                        } else {
                            sendFollowRequest();
                        }
                    } else {
                        insertFollowerAndFollowing();
                    }
                }
            }
        });
        // [END layout component function]

    }

    // [START Method]
    // [START User Profile Handler && Button Follow Status]
    private void handleButtonFollow() {
        btnFollow.setBackgroundTintList(null);
        if (Followed){
            btnFollow.setText("Following");
        }else{
            if (Requested){
                btnFollow.setText("Requested");
            }else{
                btnFollow.setBackgroundTintList(ContextCompat.getColorStateList(UserProfile.this, R.color.purple_2));
                btnFollow.setText("Follow");
            }
        }
    }
    private void handleUserProfile() {
        if (privateAC) {
            if (Followed) {
                loadUserProfile(); // if following
            } else {
                hideUserProfile(); // Private User and unfollowed
            }
        } else {
            loadUserProfile(); // Public user (regardless of whether there is a follow)
        }
    }
    // load User Profile
    private void loadUserProfile() {
        profile.setVisibility(View.VISIBLE);
        tabbedView.setVisibility(View.VISIBLE);
        tab.setVisibility(View.VISIBLE);
        privateAccountHint.setVisibility(View.GONE);
    }

    // hide User Profile
    private void hideUserProfile() {
        profile.setVisibility(View.GONE);
        tabbedView.setVisibility(View.GONE);
        tab.setVisibility(View.GONE);
        privateAccountHint.setVisibility(View.VISIBLE);
    }
    // [END User Profile Handler]

    // [START Follow,Unfollow,Request Action]
    // insert one follower and following
    private void insertFollowerAndFollowing() {
        Users storedUser = new Users();
        storedUser.setUID(auth.getUid());
        databaseFollowerRef.child(auth.getUid()).setValue(storedUser);
        storedUser.setUID(user.getUID());
        databaseYourFollowingRef.child(user.getUID()).setValue(storedUser);
        Toast.makeText(UserProfile.this, "followed this user", Toast.LENGTH_SHORT).show();

    }
    // remove one follower and following
    private void removeFollowerAndFollowing() {
        databaseFollowerRef.child(auth.getUid()).removeValue();
        databaseYourFollowingRef.child(user.getUID()).removeValue();
        Toast.makeText(UserProfile.this, "unfollowed this user", Toast.LENGTH_SHORT).show();
    }

    // Send a Follow request
    private void sendFollowRequest() {
        Users storedUser = new Users();
        storedUser.setUID(auth.getUid());
        databaseRequestedRef.child(auth.getUid()).setValue(storedUser);
        Toast.makeText(UserProfile.this, "Send follow request", Toast.LENGTH_SHORT).show();
    }

    // Cancel Follow request
    private void cancelFollowRequest() {
        databaseRequestedRef.child(auth.getUid()).removeValue();
        Toast.makeText(UserProfile.this, "Cancel follow request", Toast.LENGTH_SHORT).show();
    }
    // [END Follow,Unfollow,Request Action]
    // [END Method]
}