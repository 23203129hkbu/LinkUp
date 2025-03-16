package com.example.linkup.CommunityOperation;

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

public class CreateCommunityPost extends AppCompatActivity {
    // layout object
    ImageView btnClose, btnCreate;
    EditText headline, content;
    ProgressBar progressbar;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef,databaseArticleRef; // real-time db ref ; ArticleSortByUser -> ASBU
    // Calendar & DateFormat
    Calendar date, time;
    SimpleDateFormat currentDate, currentTime;
    // User-Info
    String userUsername, userAvatar, userStatus;
    // Article-Info
    Articles article = new Articles();
    String articleID,articleHeadline, articleContent, createdDate, createdTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_community_post);
        // [START gain layout objects]
        btnClose = findViewById(R.id.btnClose);
        btnCreate = findViewById(R.id.btnCreate);
        headline = findViewById(R.id.headline);
        content = findViewById(R.id.content);
        progressbar = findViewById(R.id.progressbar);
        // [END gain]

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user").child(auth.getUid());
        databaseArticleRef = Rdb.getReference().child("article");
        // [END config_firebase reference]

        //[START Calender / Date Format configuration]
        date = Calendar.getInstance();
        time = Calendar.getInstance();
        currentDate = new SimpleDateFormat("dd-mm-yy");
        currentTime = new SimpleDateFormat("HH:mm");
        //[END Calender / Date Format configuration]

        // Load / Gain existing user data
        databaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userUsername = snapshot.child("username").getValue(String.class);
                    userAvatar = snapshot.child("imageURI").getValue(String.class);
                    userStatus = snapshot.child("privacy").getValue(String.class);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateCommunityPost.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // [START layout component function]
        // Switch the screen -  Community Fragment
        btnClose.setOnClickListener(new View.OnClickListener() {
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
                createdTime = currentTime.format(time.getTime());

                if ((TextUtils.isEmpty(articleHeadline))) {
                    Toast.makeText(CreateCommunityPost.this, "Headline is required", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(articleContent)) {
                    Toast.makeText(CreateCommunityPost.this, "Content is required", Toast.LENGTH_SHORT).show();
                } else {
                    progressbar.setVisibility(View.VISIBLE);
                    // Create Article
                    CreateArticle();
                    // Update UI
                    updateUI();
                }
            }
        });

    }

    // [START Method]
    // handling UI update
    private void updateUI() {
        Toast.makeText(CreateCommunityPost.this, "Article Created", Toast.LENGTH_SHORT).show();
        // Delay execution to allow enough time for data to be uploaded
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },2000);
    }


    private void CreateArticle() {
        // Create Article
        article.setUID(auth.getUid());
        article.setHeadline(articleHeadline);
        article.setContent(articleContent);
        article.setDate(createdDate);
        article.setTime(createdTime);
        // Gain Article ID from real-time DB
        articleID = databaseArticleRef.push().getKey();
        // Save article Sort by User
        // Save article
        article.setArticleID(articleID);
        databaseArticleRef.child(articleID).setValue(article);
    }
    // [END Method]
}