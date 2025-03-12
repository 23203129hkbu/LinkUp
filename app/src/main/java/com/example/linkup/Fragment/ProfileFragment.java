package com.example.linkup.Fragment;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.example.linkup.R;
import com.example.linkup.RegistrationActivity;
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
    ImageView btnMenu, btnEdit, btnSetting, btnLogout, avatar;
    TextView username, introduction, website, btnPost;
    // Firebase features
    FirebaseAuth auth; // auth
    FirebaseFirestore Fdb; // firestore db
    DocumentReference documentUserRef; // firestore db ref
    String userUsername, userIntroduction, userWebsite, userAvatar;
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
        btnLogout = view.findViewById(R.id.btnLogout);
        // [END gain]

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Fdb = FirebaseFirestore.getInstance();
        documentUserRef = Fdb.collection("user").document(auth.getUid());
        //[END configuration]

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

                            Picasso.get().load(userAvatar).into(avatar);
                            // Glide.with(getContext()).load(userAvatar).into(avatar);
                            username.setText(userUsername);
                            introduction.setText(userIntroduction);
                            website.setText(userWebsite);
                        } else {
                            Intent intent = new Intent(getContext(), CreateProfile.class);
                            startActivity(intent);
                        }
                    }
                });

        // [START layout component function]
        //
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                updateUI("Login");
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
        if (screen.equals("Login")) {
            intent = new Intent(getContext(), LoginActivity.class);
        } else if (screen.equals("Google")) {
            intent = new Intent(getContext(), MainActivity.class);
        }
        startActivity(intent);
    }
    // [END Method]
}