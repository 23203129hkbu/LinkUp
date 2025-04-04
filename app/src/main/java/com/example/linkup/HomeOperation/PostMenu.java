package com.example.linkup.HomeOperation;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.example.linkup.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// ✅
public class PostMenu extends BottomSheetDialogFragment {
    // layout object
    private String postId; // Added to store the postID
    private String postUrl; // URL to share/copy
    CardView btnShare, btnCopy, btnDelete;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databasePostRef, databaseLikeRef; // real-time db ref ;

    // Added constructor to accept postID and post URL
    public PostMenu(String postId, String postUrl) {
        this.postId = postId; // Store the postID
        this.postUrl = postUrl; // Store the post URL
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.post_menu, null);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databasePostRef = Rdb.getReference().child("post").child(postId);
        databaseLikeRef = Rdb.getReference().child("likedPost").child(postId);
        // [END config_firebase reference]

        // [START gain layout objects]
        btnShare = view.findViewById(R.id.btnShare);
        btnCopy = view.findViewById(R.id.btnCopy);
        btnDelete = view.findViewById(R.id.btnDelete);
        // [END gain]

        // [START layout component function]
        // Determine whether the delete button appears
        databasePostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("uid").getValue(String.class).equals(auth.getUid())) {
                        btnDelete.setVisibility(View.VISIBLE);
                    } else {
                        btnDelete.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Share post
        btnShare.setOnClickListener(v -> {
            String shareText = "Check out this post: " + postUrl;
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_TEXT, shareText);
            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, "Share via"));
            dismiss();
        });

        // Copy URL
        btnCopy.setOnClickListener(v -> {
            ClipboardManager cp = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Post URL", postUrl);
            cp.setPrimaryClip(clip);
            Toast.makeText(getContext(), "URL copied to clipboard", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        // Delete post
        btnDelete.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirmation Notification")
                    .setMessage("Delete your post?")
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        builder.setTitle("Warning")
                                .setMessage("Your post will be deleted and cannot be recovered.\n\nAre you sure you still want to perform this operation?")
                                .setPositiveButton("Yes", (dialog1, which1) -> {
                                    databasePostRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                databasePostRef.removeValue().addOnCompleteListener(task -> {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(getContext(), "Post deleted", Toast.LENGTH_SHORT).show();
                                                                // delete related data
                                                                databaseLikeRef.removeValue();
                                                                dismiss(); // Close menu
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
                                })
                                .setNegativeButton("No", null);
                        builder.create().show();
                    })
                    .setNegativeButton("Cancel", null);
            builder.create().show();
        });
        // [END layout component function]
        return view;
    }
}