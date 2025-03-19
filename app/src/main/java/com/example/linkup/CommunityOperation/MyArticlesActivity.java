package com.example.linkup.CommunityOperation;

import android.content.Intent;
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
import com.example.linkup.Fragment.CommunityFragment;
import com.example.linkup.Object.Articles;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyArticlesActivity extends AppCompatActivity {
    // layout object
    ImageView btnBack;
    RecyclerView articleRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseArticleRef; // real-time db ref ; SavedArticleSortByUser -> SASBU
    // convert article data into RecyclerView by Adapter
    ArrayList<Articles> articlesArrayList = new ArrayList<>();
    ArticleAdapter articleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_articles);
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
        // [END config_firebase reference]

        // Gain the adapter data object
        databaseArticleRef.orderByChild("date").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                articlesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Articles article = dataSnapshot.getValue(Articles.class);
                    // Ensure article is not null before proceeding
                    if (article != null && article.getUID().equals(auth.getUid())) {
                        articlesArrayList.add(article);
                    }
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
                // Handle possible errors
            }
        });

        // Grant value - which view, articles array list
        articleAdapter = new ArticleAdapter(MyArticlesActivity.this, articlesArrayList);
        // Set up the layout manager, adapter
        articleRV.setLayoutManager(new LinearLayoutManager(MyArticlesActivity.this));
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