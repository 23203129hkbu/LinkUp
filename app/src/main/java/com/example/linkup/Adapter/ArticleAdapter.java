package com.example.linkup.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.CommunityOperation.ArticleActivity;
import com.example.linkup.CommunityOperation.MyArticlesActivity;
import com.example.linkup.Fragment.CommunityFragment;
import com.example.linkup.Object.Articles;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Articles> articlesArrayList;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb;
    DatabaseReference databaseSavedArticleRef;

    // Constructor
    public ArticleAdapter(Context context, ArrayList<Articles> articlesArrayList) {
        this.context = context;
        this.articlesArrayList = articlesArrayList;
    }


    @NonNull
    @Override
    public ArticleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.article_item, parent, false);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseSavedArticleRef = Rdb.getReference().child("savedArticle").child(auth.getUid());
        // [END config_firebase reference]
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleAdapter.ViewHolder holder, int position) {
        Articles article = articlesArrayList.get(position);

        Picasso.get().load(article.getImageURL()).into(holder.avatar);
        holder.username.setText(article.getUsername());
        holder.date.setText(article.getDate());
        holder.headline.setText(article.getHeadline());

        // Check if the current user is the creator of the article
        if (article.getUID().equals(auth.getUid())) {
            // Hide save button if the user is the creator
            holder.btnSave.setVisibility(View.GONE);
        } else {
            // Show save button for other users
            holder.btnSave.setVisibility(View.VISIBLE);

            // Check if the article is already saved
            databaseSavedArticleRef.child(article.getArticleID()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        holder.btnSave.setImageResource(R.drawable.baseline_turned_in_24);
                    } else {
                        holder.btnSave.setImageResource(R.drawable.baseline_turned_in_not_24);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Handle save button click (toggle save/remove article)
            holder.btnSave.setOnClickListener(view -> {
                databaseSavedArticleRef.child(article.getArticleID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Article is already saved, remove it
                            databaseSavedArticleRef.child(article.getArticleID()).removeValue()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "Article Removed", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, "Failed to remove article", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Article is not saved, save it
                            databaseSavedArticleRef.child(article.getArticleID()).setValue(article)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "Article Saved", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, "Failed to save article", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        // Open article details on item click
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ArticleActivity.class);
            intent.putExtra("article", article);  // Pass the article object
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return articlesArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar, btnSave;
        TextView username, date, headline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            username = itemView.findViewById(R.id.username);
            date = itemView.findViewById(R.id.date);
            headline = itemView.findViewById(R.id.headline);
            btnSave = itemView.findViewById(R.id.btnSave);
        }
    }
}