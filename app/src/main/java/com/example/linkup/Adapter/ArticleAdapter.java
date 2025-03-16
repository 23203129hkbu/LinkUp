package com.example.linkup.Adapter;

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
    private final CommunityFragment communityFragment;
    private final ArrayList<Articles> articlesArrayList;
    private final FirebaseAuth auth;
    private final FirebaseDatabase Rdb;
    private DatabaseReference databaseSavedArticleRef;

    public ArticleAdapter(CommunityFragment communityFragment, ArrayList<Articles> articlesArrayList) {
        this.communityFragment = communityFragment;
        this.articlesArrayList = articlesArrayList;
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
    }

    @NonNull
    @Override
    public ArticleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(communityFragment.getContext()).inflate(R.layout.article_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleAdapter.ViewHolder holder, int position) {
        Articles article = articlesArrayList.get(position);

        Picasso.get().load(article.getImageURL()).into(holder.avatar);
        holder.username.setText(article.getUsername());
        holder.date.setText(article.getDate());
        holder.headline.setText(article.getHeadline());

        databaseSavedArticleRef = Rdb.getReference().child("savedArticle").child(auth.getUid());

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
                Toast.makeText(communityFragment.getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Open article details on item click
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(communityFragment.getContext(), ArticleActivity.class);
            intent.putExtra("articleID", article.getArticleID());
            communityFragment.startActivity(intent);
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
                                        Toast.makeText(communityFragment.getContext(), "Article Removed", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(communityFragment.getContext(), "Failed to remove article", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Article is not saved, save it
                        databaseSavedArticleRef.child(article.getArticleID()).setValue(article)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(communityFragment.getContext(), "Article Saved", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(communityFragment.getContext(), "Failed to save article", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(communityFragment.getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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