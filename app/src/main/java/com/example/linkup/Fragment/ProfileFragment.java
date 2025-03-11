package com.example.linkup.Fragment;

import android.content.Intent;
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

import com.example.linkup.ProfileOperation.CreateProfile;
import com.example.linkup.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    View view;
    // Firebase features
    FirebaseAuth auth; // auth
    FirebaseFirestore Fdb; // firestore db
    DocumentReference documentUserRef; // firestore db ref

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_profile_fragment, container, false);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Fdb = FirebaseFirestore.getInstance();
        documentUserRef = Fdb.collection("user").document(auth.getCurrentUser().getUid());
        //[END configuration]

        //[Determine whether the user is logging in for the first time]
        documentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()){

                }else {
                    Intent intent = new Intent(getActivity(), CreateProfile.class);
                    startActivity(intent);
                }
            }
        });

        // this line must be finalized
        return view;
    }
}