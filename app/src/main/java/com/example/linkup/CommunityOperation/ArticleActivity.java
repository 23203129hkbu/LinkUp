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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Adapter.ArticleAdapter;
import com.example.linkup.Adapter.ArticleCommentAdapter;
import com.example.linkup.Object.ArticleComments;
import com.example.linkup.Object.Articles;
import com.example.linkup.Object.Users;
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
import java.util.ArrayList;
import java.util.Calendar;

public class ArticleActivity extends AppCompatActivity {
    ImageView btnClose, btnSave, btnDelete, posterAvatar, btnLike, btnSend;
    TextView posterName, createdDate, createdTime, headline, content, likes;
    EditText comment;
    RecyclerView commentRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef, databaseArticleRef, databaseSavedArticleRef, databaseCommentRef, databaseLikeRef; // real-time db ref ;
    // convert comment data into RecyclerView by Adapter
    ArrayList<ArticleComments> commentsArrayList = new ArrayList<>();
    ArticleCommentAdapter articleCommentAdapter;
    // Calendar & DateFormat
    Calendar date, time;
    SimpleDateFormat currentDate, currentTime;
    // Article - retrieve data form adapter
    Articles article = new Articles();
    ArticleComments articleComment = new ArticleComments();
    // User
    Users user = new Users();
    // Comment
    String userComment, commentDate, commentTime, userUsername, userAvatar;
    // Likes
    int noOfLikes;

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
        likes = findViewById(R.id.likes);
        commentRV = findViewById(R.id.commentRV);
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
        databaseUserRef = Rdb.getReference().child("user").child(auth.getUid());
        databaseArticleRef = Rdb.getReference().child("article").child(article.getArticleID());
        databaseSavedArticleRef = Rdb.getReference().child("savedArticle").child(auth.getUid());
        databaseCommentRef = databaseArticleRef.child("Comment");
        databaseLikeRef = databaseArticleRef.child("Like");
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
            // Load Like Button

        }
        // Load / Gain existing user data
        databaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userUsername = snapshot.child("username").getValue(String.class);
                    userAvatar = snapshot.child("imageURL").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ArticleActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Load / Gain existing likes
        databaseLikeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Count total likes
                noOfLikes = (int) snapshot.getChildrenCount();
                likes.setText(String.valueOf(noOfLikes));
                // Check if the current user has liked the article
                if (snapshot.hasChild(auth.getUid())) {
                    // User has liked the article
                    btnLike.setImageResource(R.drawable.baseline_favorite_24); // Change to liked icon
                } else {
                    // User has not liked the article
                    btnLike.setImageResource(R.drawable.baseline_favorite_border_24); // Change to unliked icon
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ArticleActivity.this, "Failed to load likes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        // Gain the adapter data object
        databaseCommentRef.orderByChild("date").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentsArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ArticleComments comment = dataSnapshot.getValue(ArticleComments.class);
                    // Ensure comment is not null before proceeding
                    if (comment != null) {
                        commentsArrayList.add(comment);
                    }
                }

                // Sort the comments after all have been added to the list
                commentsArrayList.sort((a1, a2) -> {
                    // First, compare by date
                    int dateComparison = a2.getDate().compareTo(a1.getDate());
                    if (dateComparison == 0) {
                        // If dates are equal, compare by time
                        return a2.getTime().compareTo(a1.getTime());
                    }
                    return dateComparison;
                });
                // Notify adapter after sorting
                articleCommentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });

        // Grant value - which view, articles array list
        articleCommentAdapter = new ArticleCommentAdapter(ArticleActivity.this, commentsArrayList);
        // Set up the layout manager, adapter
        commentRV.setLayoutManager(new LinearLayoutManager(ArticleActivity.this));
        commentRV.setHasFixedSize(true);
        commentRV.setAdapter(articleCommentAdapter);

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

        // Like the article
        btnLike.setOnClickListener(view -> {
            databaseLikeRef.child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Article is already saved, remove it
                        databaseLikeRef.child(auth.getUid()).removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ArticleActivity.this, "Unlike", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ArticleActivity.this, "Failed to unlike", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        user.setUID(auth.getUid());
                        // Article is not saved, save it
                        databaseLikeRef.child(auth.getUid()).setValue(user)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ArticleActivity.this, "Liked", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ArticleActivity.this, "Failed to like", Toast.LENGTH_SHORT).show();
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
    private void CreateComment() {
        // Gain Comment ID from real-time DB - No need to store
        String CID = databaseCommentRef.push().getKey();
        // Create Comment
        articleComment.setUID(auth.getUid());
        articleComment.setDate(commentDate);
        articleComment.setTime(commentTime);
        articleComment.setComment(userComment);
        articleComment.setUsername(userUsername);
        articleComment.setImageURL(userAvatar);
        // Save comment
        databaseCommentRef.child(CID).setValue(articleComment);
    }
    // [END Method]
}
