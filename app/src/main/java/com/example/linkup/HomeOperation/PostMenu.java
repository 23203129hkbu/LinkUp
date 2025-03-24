package com.example.linkup.HomeOperation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.example.linkup.CommunityOperation.ArticleActivity;
import com.example.linkup.CommunityOperation.MyArticlesActivity;
import com.example.linkup.CommunityOperation.SavedArticlesActivity;
import com.example.linkup.ProfileOperation.SettingActivity;
import com.example.linkup.ProfileOperation.UpdateProfile;
import com.example.linkup.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PostMenu extends BottomSheetDialogFragment {

    private String postId; // Added to store the postID
    CardView btnShare, btnCopy, btnDelete;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databasePostRef; // real-time db ref ;

    // Added constructor to accept postID
    public PostMenu(String postId) {
        this.postId = postId; // Store the postID
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.post_menu,null);
        // [START gain layout objects]
        btnShare = view.findViewById(R.id.btnShare);
        btnCopy = view.findViewById(R.id.btnCopy);
        btnDelete = view.findViewById(R.id.btnDelete);
        // [END gain]

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databasePostRef = Rdb.getReference().child("post").child(postId);
        // [END config_firebase reference]

        // [START config_layout]
        // Determine whether the delete button appears
        databasePostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Layout Control
                    if (snapshot.child("uid").getValue(String.class).equals(auth.getUid())){
                        btnDelete.setVisibility(View.VISIBLE);
                    }else{
                        btnDelete.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // [END config_layout]


        // [START layout component function]
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Confirmation Notification")
                        .setMessage("Delete your post?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                builder.setTitle("Warning")
                                        .setMessage("Your post will be deleted and cannot be recovered.\n\nAre you sure you still want to perform this operation?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                databasePostRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            // Article is already saved, remove it
                                                            databasePostRef.removeValue()
                                                                    .addOnCompleteListener(task -> {
                                                                        if (task.isSuccessful()) {
                                                                            Toast.makeText(getContext(), "Post deleted", Toast.LENGTH_SHORT).show();
                                                                            // close menu
                                                                            dismiss();
                                                                        } else {
                                                                            Toast.makeText(getContext(), "Failed to delete your post", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                // this line to show the dialog
                                builder.create().show();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                // this line to show the dialog
                builder.create().show();
            }
        });

        // [END layout component function]
        // this line must be finalized
        return view;
    }

    // [START Method]
//    // handling UI update
//    private void updateUI(String screen) {
//        Intent intent = null;
//        if (screen.equals("MyArticles")) {
//            intent = new Intent(getContext(), MyArticlesActivity.class);
//        } else if (screen.equals("savedArticles")) {
//            intent = new Intent(getContext(), SavedArticlesActivity.class);
//        }
//        if (intent != null) {
//            startActivity(intent);
//        }
//    }
    // [END Method]
}
