package com.example.linkup.Fragment;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.linkup.LoginActivity;
import com.example.linkup.MainActivity;
import com.example.linkup.ProfileOperation.CreateProfile;
import com.example.linkup.ProfileOperation.UpdateProfile;
import com.example.linkup.R;
import com.example.linkup.RegistrationActivity;
import com.example.linkup.SettingActivity;
import com.example.linkup.SocialLogin.FacebookSignInActivity;
import com.example.linkup.SocialLogin.GoogleSignInActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {
    View view;
    // layout object
    ImageView btnEdit, btnSetting, avatar;
    TextView username, introduction, website, state, btnPost;
    // Firebase features
    FirebaseAuth auth; // auth
    FirebaseFirestore Fdb; // firestore db
    DocumentReference documentUserRef; // firestore db ref
    // default user info
    String userUsername, userIntroduction, userWebsite, userAvatar, userState;

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

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Fdb = FirebaseFirestore.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        documentUserRef = Fdb.collection("user").document(auth.getUid());
        // [END config_firebase reference]

        //[Determine whether the user is logging in for the first time]
        documentUserRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()) {
                            userAvatar = task.getResult().getString("avatar");
                            userUsername = task.getResult().getString("username");
                            userIntroduction = task.getResult().getString("introduction");
                            userWebsite = task.getResult().getString("website");
                            userState = task.getResult().getString("privacy");

                            Picasso.get().load(userAvatar).into(avatar);
                            // Glide.with(getContext()).load(userAvatar).into(avatar);
                            username.setText(userUsername);
                            introduction.setText(userIntroduction);
                            website.setText(userWebsite);
                            state.setText(userState);
                        } else {
                            Intent intent = new Intent(getContext(), CreateProfile.class);
                            startActivity(intent);
                        }
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
                }catch (Exception e){
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
        }
        startActivity(intent);
    }
    // [END Method]
}