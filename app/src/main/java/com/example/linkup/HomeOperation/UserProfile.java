package com.example.linkup.HomeOperation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

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

public class UserProfile extends AppCompatActivity {
    // layout object
    CircleImageView avatar;
    ImageView btnBack;
    TextView username, introduction, website, state, posts, btnFollow;
    // Tabbed View
    SectionsPagerAdapter adapter;
    ViewPager2 tabbedView;
    TabLayout tab;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef, databasePostRef; // real-time db ref
    // Article - retrieve data form adapter
    Users user = new Users();
    // default user info
    String userWebsite;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        // [START gain value from other activity]
        user = (Users) getIntent().getSerializableExtra("user");
        // [END gain]

        // [START gain layout objects]
        avatar = findViewById(R.id.avatar);
        username = findViewById(R.id.username);
        website = findViewById(R.id.website);
        introduction = findViewById(R.id.introduction);
        state = findViewById(R.id.state);
        btnBack = findViewById(R.id.btnBack);
        posts = findViewById(R.id.posts);
        btnFollow = findViewById(R.id.btnFollow);
        // Tabbed view
        tabbedView = findViewById(R.id.tabbedView);
        tab = findViewById(R.id.tab);
        // [END gain]

        // [START config_firebase]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        // [END config_firebase]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user").child(user.getUID());
        databasePostRef = Rdb.getReference().child("post");
        // [END config_firebase reference]

        //[Determine whether the user is logging in for the first time]
        databaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userWebsite = snapshot.child("website").getValue(String.class);
                    // Layout Control
                    username.setText(snapshot.child("username").getValue(String.class));
                    Picasso.get().load(snapshot.child("avatarURL").getValue(String.class)).into(avatar);
                    state.setText(snapshot.child("privacy").getValue(String.class));
                    website.setText(userWebsite);
                    introduction.setText(snapshot.child("introduction").getValue(String.class));
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
                int numOfPost = 0; // Reset count before counting
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Posts post = dataSnapshot.getValue(Posts.class);
                    if (post != null && post.getUID().equals(user.getUID())) {
                        numOfPost += 1;
                    }
                }
                posts.setText(String.valueOf(numOfPost)); // Set the count after counting
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
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

        // [START layout component function]
        // Switch the screen - Home Page
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Logout with dialog message
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
        // [END layout component function]

    }

    // [START Method]
    // [END Method]
}