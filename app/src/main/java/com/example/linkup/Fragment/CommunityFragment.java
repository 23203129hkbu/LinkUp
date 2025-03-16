package com.example.linkup.Fragment;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Adapter.ArticleAdapter;
import com.example.linkup.CommunityOperation.CreateCommunityPost;
import com.example.linkup.Object.Articles;
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

public class CommunityFragment extends Fragment {
    View view;
    // layout object
    ImageView btnAdd, btnMenu;
    RecyclerView articleRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef,databaseArticleRef; // real-time db ref ; SavedArticleSortByUser -> SASBU
    // convert article data into RecyclerView by Adapter
    ArrayList<Articles> articlesArrayList = new ArrayList<>();
    ArticleAdapter articleAdapter;
    // identify whether article was saved
    boolean checker = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_community_fragment, container, false);
        // [START gain layout objects]
        btnAdd = view.findViewById(R.id.btnAdd);
        btnMenu = view.findViewById(R.id.btnMenu);
        articleRV = view.findViewById(R.id.articleRV);
        // [END gain]

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user");
        databaseArticleRef = Rdb.getReference().child("article");
        // [END config_firebase reference]

        databaseArticleRef.orderByChild("date").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                articlesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Articles article = dataSnapshot.getValue(Articles.class);

                    // Ensure article is not null before proceeding
                    if (article != null) {
                        // Fetch user data and update the article inside the callback
                        databaseUserRef.child(article.getUID()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                if (userSnapshot.exists()) {
                                    article.setUsername(userSnapshot.child("username").getValue(String.class));
                                    article.setImageURL(userSnapshot.child("imageURL").getValue(String.class));
                                    article.setPrivacy(userSnapshot.child("privacy").getValue(String.class));
                                    articlesArrayList.add(article);

                                    // **Sort by date and time before updating UI**
                                    articlesArrayList.sort((a1, a2) -> {
                                        // First, compare by date
                                        int dateComparison = a1.getDate().compareTo(a2.getDate());
                                        if (dateComparison == 0) {
                                            // If dates are equal, compare by time
                                            return a1.getTime().compareTo(a2.getTime());
                                        }
                                        return dateComparison;
                                    });

                                    articleAdapter.notifyDataSetChanged(); // Notify adapter after sorting
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getContext(), "Failed to load user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Grant value - which view, articles array list
        articleAdapter = new ArticleAdapter(CommunityFragment.this, articlesArrayList);
        // Set up the layout manager, adapter
        articleRV.setLayoutManager(new LinearLayoutManager(getContext()));
        articleRV.setHasFixedSize(true);
        articleRV.setAdapter(articleAdapter);

        // [START layout component function]
        // Switch the screen - Create Community Post
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI("Add");
            }
        });

        // this line must be finalized
        return view;
    }

    // [START Method]
    // handling UI update
    private void updateUI(String screen) {
        Intent intent = null;
        if (screen.equals("Add")) {
            intent = new Intent(getContext(), CreateCommunityPost.class);
        }
//        } else if (screen.equals("Update")) {
//            intent = new Intent(getContext(), UpdateProfile.class);
//        }
        if (intent != null) {
            startActivity(intent);
        }
    }
    // [END Method]
}

