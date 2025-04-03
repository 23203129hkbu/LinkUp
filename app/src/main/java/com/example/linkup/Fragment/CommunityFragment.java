package com.example.linkup.Fragment;

import static android.content.Intent.getIntent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Adapter.ArticleAdapter;
import com.example.linkup.CommunityOperation.CommunityMenu;
import com.example.linkup.CommunityOperation.CreateCommunityPost;
import com.example.linkup.Object.Articles;
import com.example.linkup.ProfileOperation.PrivacyActivity;
import com.example.linkup.ProfileOperation.SettingActivity;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;

// ✅
public class CommunityFragment extends Fragment {
    View view;
    // layout object
    ImageView btnAdd, btnMenu;
    RecyclerView articleRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseArticleRef; // real-time db ref ; SavedArticleSortByUser -> SASBU
    // convert article data into RecyclerView by Adapter
    ArrayList<Articles> articlesArrayList = new ArrayList<>();
    ArticleAdapter articleAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_community_fragment, container, false);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseArticleRef = Rdb.getReference().child("article");
        // [END config_firebase reference]

        // [START gain layout objects]
        btnAdd = view.findViewById(R.id.btnAdd);
        btnMenu = view.findViewById(R.id.btnMenu);
        articleRV = view.findViewById(R.id.articleRV);
        // [END gain]

        // [START config_layout]
        // Pass data into arrayList , then article adapter received
        databaseArticleRef.orderByChild("date").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                articlesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Articles article = dataSnapshot.getValue(Articles.class);
                    // Ensure article is not null before proceeding
                    if (article != null) {
                        articlesArrayList.add(article);
                    }
                }
                // Sort articles by date and time to ensure the newest articles are at the top
                articlesArrayList.sort((a1, a2) -> {
                    // Compare by date (descending)
                    int dateComparison = a2.getDate().compareTo(a1.getDate());
                    return dateComparison;
                });
                // Notify adapter after sorting
                articleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load articles: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Grant value - which view, articles array list
        articleAdapter = new ArticleAdapter(getContext(), articlesArrayList);
        // Set up the layout manager, adapter
        articleRV.setLayoutManager(new LinearLayoutManager(getContext()));
        articleRV.setHasFixedSize(true);
        articleRV.setAdapter(articleAdapter);
        // [END config_layout]

        // [START layout component function]
        // Switch the screen - Create Community Post
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI();
            }
        });
        // Switch the screen - Community Menu
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommunityMenu mu = new CommunityMenu();
                mu.show(getParentFragmentManager(), "bottom");
            }
        });
        // [END layout component function]
        // this line must be finalized
        return view;
    }

    // [START Method]
    // handling UI update
    private void updateUI() {
        Intent intent = new Intent(getContext(), CreateCommunityPost.class);
        startActivity(intent);
    }
    // [END Method]
}

