package com.example.linkup.CommunityOperation;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.linkup.Object.Articles;
import com.example.linkup.Object.Users;
import com.example.linkup.ProfileOperation.UpdateProfile;
import com.example.linkup.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UpdateCommunityPost extends BottomSheetDialogFragment {
    // layout object
    ImageView btnUpdate;
    EditText headline, content;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseArticleRef; // real-time db ref
    // Dialog
    ProgressDialog progressDialog;
    // Calendar & DateFormat
    Calendar date;
    SimpleDateFormat currentDate;
    // Article-Info
    Articles article = new Articles();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.form_update_communtiy_post,null);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseArticleRef = Rdb.getReference().child("article");
        // [END config_firebase reference]

        // [START gain layout objects]
        headline = view.findViewById(R.id.headline);
        content = view.findViewById(R.id.content);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        // [END gain]

        // [START config_dialog]
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Updating...");
        progressDialog.setCancelable(false);
        // [END config_dialog]

        // [START config_layout]
        // [Gain Article Info]
        if (getArguments() != null && getArguments().containsKey("article")) {
            article = (Articles) getArguments().getSerializable("article");

            if (article != null) {
                headline.setText(article.getHeadline());
                content.setText(article.getContent());
            }
        }
        // [END config_layout]

        // [START layout component function]
        // Update Article
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                article.setHeadline(headline.getText().toString());
                article.setContent(content.getText().toString());

                progressDialog.show(); // Show the dialog before dismissing it
                if ((TextUtils.isEmpty(article.getHeadline()))) {
                    Toast.makeText(getContext(), "Headline is required", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(article.getContent())) {
                    Toast.makeText(getContext(), "Content is required", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getContext(), "Updating...", Toast.LENGTH_SHORT).show();
                    updateArticleToDatabase();
                }
            }
        });
        // [END layout component function]
        // this line must be finalized
        return view;
    }

    // [START Method]
    // Update Article to real time db
    private void updateArticleToDatabase() {
        databaseArticleRef.child(article.getArticleID()).setValue(article).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Article updated successfully!", Toast.LENGTH_SHORT).show();
                    dismiss(); // Close the dialog after successful update
                } else {
                    Toast.makeText(getContext(), "Failed to update article.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // [END Method]
}
