package com.example.linkup.CommunityOperation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ArticleActivity extends AppCompatActivity {
    ImageView btnClose,btnSave,btnDelete,posterAvatar, btnLike, btnSend;
    TextView posterName, createdDate, createdTime, headline, content;
    EditText comment;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef, databaseArticleRef; // real-time db ref ;
    String articleID, articleContent, articleDate, articleHeadline, articleTime, UID, username, imageURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        // [START gain value from other activity]
        Intent articleActivity = getIntent();
        articleID = articleActivity.getStringExtra("articleID");
        // [END gain]

        // [START gain layout objects]
        btnClose = findViewById(R.id.btnClose);
        btnSave = findViewById(R.id.btnSave);
        posterAvatar = findViewById(R.id.posterAvatar);
        posterName = findViewById(R.id.posterName);
        createdDate = findViewById(R.id.createdDate);
        createdTime = findViewById(R.id.createdTime);
        headline = findViewById(R.id.headline);
        content = findViewById(R.id.content);
        btnLike = findViewById(R.id.btnLike);
        comment = findViewById(R.id.comment);
        btnSend = findViewById(R.id.btnSend);
        // Only content creator
        btnDelete = findViewById(R.id.btnDelete);
        // [END gain]

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user");
        databaseArticleRef = Rdb.getReference().child("article").child(articleID);
        // [END config_firebase reference]

        // Load / Gain existing user data
        databaseArticleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    articleContent = snapshot.child("content").getValue(String.class);
                    articleDate = snapshot.child("date").getValue(String.class);
                    articleHeadline = snapshot.child("headline").getValue(String.class);
                    articleTime = snapshot.child("time").getValue(String.class);
                    UID = snapshot.child("uid").getValue(String.class);

                    // Fetch user data and update the article inside the callback
                    databaseUserRef.child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {
                                username = userSnapshot.child("username").getValue(String.class);
                                imageURL = userSnapshot.child("imageURL").getValue(String.class);
                                // ✅ Now, updating UI elements inside the callback
                                Picasso.get().load(imageURL).into(posterAvatar);
                                posterName.setText(username);
                                createdDate.setText(articleDate);
                                createdTime.setText(articleTime);
                                headline.setText(articleHeadline);
                                content.setText(articleContent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ArticleActivity.this, "Failed to load user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ArticleActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
        // [END layout component function]
    }
}