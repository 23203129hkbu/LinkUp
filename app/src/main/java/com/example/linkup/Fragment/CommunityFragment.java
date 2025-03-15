package com.example.linkup.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.linkup.CommunityOperation.CreateCommunityPost;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CommunityFragment extends Fragment {
    View view;
    // layout object
    ImageView btnAdd,btnMenu;
    // Firebase features
    FirebaseAuth auth;
    FirebaseFirestore Fdb; // firestore db
    DocumentReference documentUserRef; // firestore db ref
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_community_fragment, container, false);
        // [START gain layout objects]
        btnAdd = view.findViewById(R.id.btnAdd);
        btnMenu = view.findViewById(R.id.btnMenu);
        // [END gain]

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Fdb = FirebaseFirestore.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        documentUserRef = Fdb.collection("user").document(auth.getUid());
        // [END config_firebase reference]

        // [START layout component function]
        // Switch the screen - Create Community Post
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI("Add");
            }
        });

        // this line must be finalized
        return view;
    }
    // [START Method]
    // handling UI update
    private void updateUI(String screen) {
        Intent intent = null;
        if (screen.equals("Add")) {
            intent = new Intent(getContext(), CreateCommunityPost.class);
        }
//        } else if (screen.equals("Update")) {
//            intent = new Intent(getContext(), UpdateProfile.class);
//        }
        if (intent != null) {
            startActivity(intent);
        }
    }
    // [END Method]
}

