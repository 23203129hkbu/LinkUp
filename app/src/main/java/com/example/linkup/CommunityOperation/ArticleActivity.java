package com.example.linkup.CommunityOperation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.linkup.Object.ArticleComments;
import com.example.linkup.Object.Articles;
import com.example.linkup.ProfileOperation.SettingActivity;
import com.example.linkup.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ArticleActivity extends AppCompatActivity {
    ImageView btnClose, btnSave, btnDelete, posterAvatar, btnLike, btnSend;
    TextView posterName, createdDate, createdTime, headline, content;
    EditText comment;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseArticleRef, databaseSavedArticleRef, databaseCommentRef; // real-time db ref ;
    // Calendar & DateFormat
    Calendar date, time;
    SimpleDateFormat currentDate, currentTime;
    // Article - retrieve data form adapter
    Articles article = new Articles();
    ArticleComments articleComment = new ArticleComments();
    // Comment
    String userComment, commentDate, commentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        // [START gain value from other activity]
        article = (Articles) getIntent().getSerializableExtra("article");
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
        databaseArticleRef = Rdb.getReference().child("article").child(article.getArticleID());
        databaseSavedArticleRef = Rdb.getReference().child("savedArticle").child(auth.getUid());
        databaseCommentRef = databaseArticleRef.child("Comment");
        // [END config_firebase reference]

        //[START Calender / Date Format configuration]
        date = Calendar.getInstance();
        time = Calendar.getInstance();
        currentDate = new SimpleDateFormat("dd-MM-yy");
        currentTime = new SimpleDateFormat("HH:mm");
        //[END Calender / Date Format configuration]

        // Load / Gain existing article data
        // ✅ Now, updating UI elements inside the callback
        Picasso.get().load(article.getImageURL()).into(posterAvatar);
        posterName.setText(article.getUsername());
        createdDate.setText(article.getDate());
        createdTime.setText(article.getTime());
        headline.setText(article.getHeadline());
        content.setText(article.getContent());
        if (article.getUID() != null) {
            // Layout Control (moved here to ensure UID is initialized)
            if (article.getUID().equals(auth.getUid())) {
                // Creator layout
                btnSave.setVisibility(View.GONE);
                btnDelete.setVisibility(View.VISIBLE);
            } else {
                // Netizen layout
                btnSave.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.GONE);
                // Check if the article is already saved
                databaseSavedArticleRef.child(article.getArticleID()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            btnSave.setImageResource(R.drawable.baseline_turned_in_24);
                        } else {
                            btnSave.setImageResource(R.drawable.baseline_turned_in_not_24);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ArticleActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        // [START layout component function]
        // Switch the screen -  Community Fragment
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Handle save button click (toggle save/remove article)
        btnSave.setOnClickListener(view -> {
            databaseSavedArticleRef.child(article.getArticleID()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Article is already saved, remove it
                        databaseSavedArticleRef.child(article.getArticleID()).removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ArticleActivity.this, "Article Removed", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ArticleActivity.this, "Failed to remove article", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Article is not saved, save it
                        databaseSavedArticleRef.child(article.getArticleID()).setValue(article)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ArticleActivity.this, "Article Saved", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ArticleActivity.this, "Failed to save article", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ArticleActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Delete article with related record
        btnDelete.setOnClickListener(view -> {
            databaseArticleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ArticleActivity.this);
                        builder.setTitle("Confirmation Notification")
                                .setMessage("Delete your article ?")
                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        builder.setTitle("Warning")
                                                .setMessage("Your article will be deleted and cannot be recovered.\n\nAre you sure you still want to perform this operation?")
                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        databaseArticleRef.removeValue().addOnCompleteListener(task -> {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(ArticleActivity.this, "Article Deleted", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            } else {
                                                                Toast.makeText(ArticleActivity.this, "Failed to deleted article", Toast.LENGTH_SHORT).show();
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
                    } else {
                        // Article is not saved, save it
                        Toast.makeText(ArticleActivity.this, "Article does not exist ", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ArticleActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Send comment
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userComment = comment.getText().toString();
                commentDate = currentDate.format(date.getTime());
                commentTime = currentTime.format(time.getTime());
                if ((TextUtils.isEmpty(userComment))) {
                    Toast.makeText(ArticleActivity.this, "Comment cannot be empty ", Toast.LENGTH_SHORT).show();
                } else {
                    // Create Comment
                    CreateComment();
                    // empty comment
                    comment.setText("");
                    Toast.makeText(ArticleActivity.this, "Comment sent successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // [END layout component function]
    }
    // [START Method]
//    // handling UI update
//    private void updateUI() {
//        Toast.makeText(CreateCommunityPost.this, "Article Created", Toast.LENGTH_SHORT).show();
//        // Delay execution to allow enough time for data to be uploaded
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                finish();
//            }
//        },2000);
//    }
//

    private void CreateComment() {
        // Gain Comment ID from real-time DB - No need to store
        String CID = databaseCommentRef.push().getKey();
        // Create Comment
        articleComment.setUID(auth.getUid());
        articleComment.setDate(commentDate);
        articleComment.setTime(commentTime);
        articleComment.setComment(userComment);
        articleComment.setUsername(article.getUsername());
        articleComment.setImageURL(article.getImageURL());
        // Save comment
        databaseCommentRef.child(CID).setValue(article);
    }
    // [END Method]
}