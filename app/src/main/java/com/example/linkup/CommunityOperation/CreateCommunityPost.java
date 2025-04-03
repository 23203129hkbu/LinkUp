package com.example.linkup.CommunityOperation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.linkup.Object.Articles;
import com.example.linkup.Process.LoginActivity;
import com.example.linkup.Process.MainActivity;
import com.example.linkup.ProfileOperation.CreateProfile;
import com.example.linkup.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;

// ✅
public class CreateCommunityPost extends AppCompatActivity {
    // layout object
    ImageView btnBack, btnCreate;
    EditText headline, content;
    ProgressBar progressbar;
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
    String articleID,articleHeadline, articleContent, createdDate, createdTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_community_post);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseArticleRef = Rdb.getReference().child("article");
        // [END config_firebase reference]

        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        btnCreate = findViewById(R.id.btnCreate);
        headline = findViewById(R.id.headline);
        content = findViewById(R.id.content);
        progressbar = findViewById(R.id.progressbar);
        // [END gain]

        //[START Calender / Date Format configuration]
        date = Calendar.getInstance();
        currentDate = new SimpleDateFormat("dd-MM-yy HH:mm");
        //[END Calender / Date Format configuration]

        // [START config_dialog]
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating...");
        progressDialog.setCancelable(false);
        // [END config_dialog]

        // [START layout component function]
        // Switch the screen -  Community Fragment
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Create Article
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                articleHeadline = headline.getText().toString();
                articleContent = content.getText().toString();
                createdDate = currentDate.format(date.getTime());

                if ((TextUtils.isEmpty(articleHeadline))) {
                    Toast.makeText(CreateCommunityPost.this, "Headline is required", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(articleContent)) {
                    Toast.makeText(CreateCommunityPost.this, "Content is required", Toast.LENGTH_SHORT).show();
                } else {
                    progressbar.setVisibility(View.VISIBLE);
                    // Show progress dialog
                    progressDialog.show();
                    // Create Article
                    CreateArticle();
                }
            }
        });
        // [END layout component function]
    }

    // [START Method]
    // handling UI update
    private void updateUI() {
        Toast.makeText(CreateCommunityPost.this, "Article Created", Toast.LENGTH_SHORT).show();
        finish();
    }
    // Create article
    private void CreateArticle() {
        // Gain Article ID from real-time DB
        articleID = databaseArticleRef.push().getKey();
        // Create Article
        article.setArticleID(articleID);
        article.setUID(auth.getUid());
        article.setHeadline(articleHeadline);
        article.setContent(articleContent);
        article.setDate(createdDate);
        // Save article
        databaseArticleRef.child(articleID).setValue(article).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(CreateCommunityPost.this, "Article created successfully!", Toast.LENGTH_SHORT).show();
                    updateUI();
                } else {
                    Toast.makeText(CreateCommunityPost.this, "Failed to create article.", Toast.LENGTH_SHORT).show();
                }
                progressbar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(CreateCommunityPost.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressbar.setVisibility(View.GONE);
        });
    }
    // [END Method]
}