package com.example.linkup.CommunityOperation;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
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

import de.hdodenhof.circleimageview.CircleImageView;

// ✅
public class ArticleActivity extends AppCompatActivity {
    // layout object
    CircleImageView posterAvatar;
    ImageView btnBack, btnSave, btnDelete, btnEdit, btnLike, btnSend;
    TextView posterName, createdDate, headline, content, likes;
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
    Calendar date;
    SimpleDateFormat currentDate;
    // ArticleID - retrieve data form adapter
    Articles article = new Articles();
    ArticleComments articleComment = new ArticleComments();
    // Comment
    String userComment, commentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        // [START gain value from other activity]
        article = (Articles) getIntent().getSerializableExtra("article");
        // [END gain]

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user").child(article.getUID()); // creator
        databaseSavedArticleRef = Rdb.getReference().child("savedArticle").child(auth.getUid()).child(article.getArticleID()); // saved / unsaved
        databaseLikeRef = Rdb.getReference().child("likedArticle").child(article.getArticleID()); // count article's likes
        databaseArticleRef = Rdb.getReference().child("article").child(article.getArticleID());
        databaseCommentRef = databaseArticleRef.child("commentArticle").child(article.getArticleID());
        // [END config_firebase reference]

        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        posterAvatar = findViewById(R.id.posterAvatar);
        posterName = findViewById(R.id.posterName);
        createdDate = findViewById(R.id.createdDate);
        headline = findViewById(R.id.headline);
        content = findViewById(R.id.content);
        btnLike = findViewById(R.id.btnLike);
        likes = findViewById(R.id.likes);
        commentRV = findViewById(R.id.commentRV);
        comment = findViewById(R.id.comment);
        btnSend = findViewById(R.id.btnSend);
        // Only content creator
        btnDelete = findViewById(R.id.btnDelete);
        btnEdit = findViewById(R.id.btnEdit);
        // Only netizen
        btnSave = findViewById(R.id.btnSave);
        // [END gain]

        //[START Calender / Date Format configuration]
        date = Calendar.getInstance();
        currentDate = new SimpleDateFormat("dd-MM-yy HH:mm");
        //[END Calender / Date Format configuration]

        // [START config_layout]
        if (article.getUID() != null) {
            // Layout Control (moved here to ensure UID is initialized)
            if (article.getUID().equals(auth.getUid())) {
                // Creator layout
                btnSave.setVisibility(View.GONE);
                btnDelete.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
            } else {
                // Netizen layout
                btnSave.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
            }
        }
        // Load / Gain existing article -> Because it needs to be real-time, it is not possible to directly obtain the transmitted value
        databaseArticleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    createdDate.setText(snapshot.child("date").getValue(String.class));
                    headline.setText(snapshot.child("headline").getValue(String.class));
                    content.setText(snapshot.child("content").getValue(String.class));
                }else{
                    // If the article is deleted, will be forced to leave
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ArticleActivity.this, "Failed to load articles: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Load / Gain existing article creator data
        databaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Layout Control
                    Picasso.get().load(snapshot.child("avatarURL").getValue(String.class)).into(posterAvatar);
                    posterName.setText(snapshot.child("username").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ArticleActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Check if the article is already saved
        databaseSavedArticleRef.addValueEventListener(new ValueEventListener() {
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
        // Load Button Like / Gain existing likes
        databaseLikeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int noOfLikes = 0;
                noOfLikes = (int) snapshot.getChildrenCount();
                likes.setText(String.valueOf(noOfLikes));
                // Check if the current user has liked the article
                // There also use .child(....) -> if (snapshot.exists()) {..}
                // But count the number of likes
                if (snapshot.hasChild(auth.getUid())) {
                    // User has liked the article
                    btnLike.setImageResource(R.drawable.baseline_favorite_24); // Change to liked icon
                } else {
                    // User has not liked the article
                    btnLike.setImageResource(R.drawable.baseline_favorite_border_24_gray); // Change to unliked icon
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ArticleActivity.this, "Failed to load likes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Gain the adapter data object - comment
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
                    // Compare by date (descending)
                    int dateComparison = a2.getDate().compareTo(a1.getDate());
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
        // Grant value - which view, comment array list
        articleCommentAdapter = new ArticleCommentAdapter(ArticleActivity.this, commentsArrayList);
        // Set up the layout manager, adapter
        commentRV.setLayoutManager(new LinearLayoutManager(ArticleActivity.this));
        commentRV.setHasFixedSize(true);
        commentRV.setAdapter(articleCommentAdapter);
        // [END config_layout]

        // [START layout component function]
        // Switch the screen -  Community Fragment
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Handle save button click (toggle save/remove article)
        btnSave.setOnClickListener(view -> {
            databaseSavedArticleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Article is already saved, unsaved it
                        databaseSavedArticleRef.removeValue().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ArticleActivity.this, "Article unsaved", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ArticleActivity.this, "Failed to unsaved article", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Articles storedArticle = new Articles();
                        storedArticle.setArticleID(article.getArticleID());
                        // Article is not saved, save it
                        databaseSavedArticleRef.setValue(storedArticle).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ArticleActivity.this, "Article saved", Toast.LENGTH_SHORT).show();
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
                                .setMessage("Delete your article?")
                                .setPositiveButton("Confirm", (dialog, which) -> {
                                    builder.setTitle("Warning")
                                            .setMessage("Your article will be deleted and cannot be recovered.\n\nAre you sure you still want to perform this operation?")
                                            .setPositiveButton("Yes", (dialog1, which1) -> {
                                                // Delete article and related data
                                                // Remove all users' saved articles, likes and comments
                                                databaseLikeRef.removeValue();
                                                databaseCommentRef.removeValue();
                                                DatabaseReference currentRef = Rdb.getReference("savedArticle");
                                                currentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                                            userSnapshot.getRef().child(article.getArticleID()).removeValue();
                                                        }
                                                        // Remove article after all related data already deleted
                                                        databaseArticleRef.removeValue();
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                    }
                                                });
                                                finish();
                                            })
                                            .setNegativeButton("No", (dialog12, which12) -> {
                                            });
                                    builder.create().show();
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> {
                                });
                        builder.create().show();
                    } else {
                        Toast.makeText(ArticleActivity.this, "Article does not exist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ArticleActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        // Update article
        // Switch the screen - Community Menu
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateCommunityPost updateForm = new UpdateCommunityPost();
                // Pass data to the bottom sheet if needed, for example:
                Bundle bundle = new Bundle();
                bundle.putSerializable("article", article);
                updateForm.setArguments(bundle);
                updateForm.show(getSupportFragmentManager(), "bottom");
            }
        });
        // Like/Unlike the article
        btnLike.setOnClickListener(view -> {
            databaseLikeRef.child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Article is already liked, remove it
                        databaseLikeRef.child(auth.getUid()).removeValue().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ArticleActivity.this, "Unlike", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ArticleActivity.this, "Failed to unlike", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Users storedUser = new Users();
                        storedUser.setUID(auth.getUid());
                        // Article is not liked, save it
                        databaseLikeRef.child(auth.getUid()).setValue(storedUser).addOnCompleteListener(task -> {
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
                if ((TextUtils.isEmpty(userComment))) {
                    Toast.makeText(ArticleActivity.this, "Comment cannot be empty ", Toast.LENGTH_SHORT).show();
                } else {
                    // Empty comment
                    comment.setText("");
                    // Create Comment
                    CreateComment();
                }
            }
        });
        // [END layout component function]
    }

    // [START Method]
    private void CreateComment() {
        // Gain Comment ID from real-time DB - No need to store
        String CID = databaseCommentRef.push().getKey();
        // Create Comment
        articleComment.setUID(auth.getUid());
        articleComment.setDate(commentDate);
        articleComment.setComment(userComment);
        // Save comment
        databaseCommentRef.child(CID).setValue(articleComment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ArticleActivity.this, "Comment sent successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ArticleActivity.this, "Failed to sent comment", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // [END Method]
}
