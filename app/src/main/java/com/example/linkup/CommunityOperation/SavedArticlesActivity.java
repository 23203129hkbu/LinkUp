package com.example.linkup.CommunityOperation;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Adapter.ArticleAdapter;
import com.example.linkup.Object.Articles;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SavedArticlesActivity extends AppCompatActivity {
    // layout object
    ImageView btnBack;
    RecyclerView articleRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseArticleRef, databaseSavedArticleRef; // real-time db ref ; SavedArticleSortByUser -> SASBU
    // convert article data into RecyclerView by Adapter
    ArrayList<Articles> articlesArrayList = new ArrayList<>();
    ArticleAdapter articleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_articles);
        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        articleRV = findViewById(R.id.articleRV);
        // [END gain]

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseArticleRef = Rdb.getReference().child("article");
        databaseSavedArticleRef = Rdb.getReference().child("savedArticle").child(auth.getUid());
        // [END config_firebase reference]

        databaseSavedArticleRef.orderByChild("date").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                articlesArrayList.clear();
                // Create a list to hold the article IDs
                ArrayList<String> articleIDs = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Articles article = dataSnapshot.getValue(Articles.class);
                    if (article != null) {
                        articleIDs.add(article.getArticleID());
                    }
                }

                // Now check each article ID against the article database
                for (String articleID : articleIDs) {
                    databaseArticleRef.child(articleID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Articles article = snapshot.getValue(Articles.class);
                                if (article != null) {
                                    // Update or add the article to the list
                                    boolean found = false;
                                    for (int i = 0; i < articlesArrayList.size(); i++) {
                                        if (articlesArrayList.get(i).getArticleID().equals(article.getArticleID())) {
                                            articlesArrayList.set(i, article);
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        articlesArrayList.add(article);
                                    }
                                }
                            } else {
                                // Remove the article ID from savedArticles if it doesn't exist
                                databaseSavedArticleRef.child(articleID).removeValue();
                                // Remove the article from the list
                                articlesArrayList.removeIf(a -> a.getArticleID().equals(articleID));
                            }
                            // Notify adapter after changes
                            articleAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(SavedArticlesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                // Sort the articles after all have been added to the list
                articlesArrayList.sort((a1, a2) -> {
                    // First, compare by date
                    int dateComparison = a2.getDate().compareTo(a1.getDate());
                    if (dateComparison == 0) {
                        // If dates are equal, compare by time
                        return a2.getTime().compareTo(a1.getTime());
                    }
                    return dateComparison;
                });
                // Notify adapter after sorting
                articleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SavedArticlesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Grant value - which view, articles array list
        articleAdapter = new ArticleAdapter(SavedArticlesActivity.this, articlesArrayList);
        // Set up the layout manager, adapter
        articleRV.setLayoutManager(new LinearLayoutManager(SavedArticlesActivity.this));
        articleRV.setHasFixedSize(true);
        articleRV.setAdapter(articleAdapter);

        // [START layout component function]
        // Switch the screen - Main Screen
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // [END layout component function]
    }

}