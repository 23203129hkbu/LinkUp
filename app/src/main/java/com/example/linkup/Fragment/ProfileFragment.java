package com.example.linkup.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.linkup.CommunityOperation.ArticleActivity;
import com.example.linkup.ProfileOperation.CreateProfile;
import com.example.linkup.ProfileOperation.UpdateProfile;
import com.example.linkup.R;
import com.example.linkup.ProfileOperation.SettingActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {
    View view;
    // layout object
    ImageView btnEdit, btnSetting, avatar;
    TextView username, introduction, website, state, btnPost;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef; // real-time db ref
    // default user info
    String userWebsite;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_profile_fragment, container, false);
        // [START gain layout objects]
        avatar = view.findViewById(R.id.avatar);
        username = view.findViewById(R.id.username);
        website = view.findViewById(R.id.website);
        introduction = view.findViewById(R.id.introduction);
        state = view.findViewById(R.id.state);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnSetting = view.findViewById(R.id.btnSetting);
        // [END gain]

        // [START config_firebase]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        // [END config_firebase]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user").child(auth.getUid());
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
                } else {
                    updateUI("Create");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

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
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
    // [END Method]
}