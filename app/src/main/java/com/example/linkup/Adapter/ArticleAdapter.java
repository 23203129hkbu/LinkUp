package com.example.linkup.Adapter;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.example.linkup.Object.Users;
import com.example.linkup.ProfileOperation.UpdateProfile;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

// ✅
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Articles> articlesArrayList;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db


    // Constructor
    public ArticleAdapter(Context context, ArrayList<Articles> articlesArrayList) {
        this.context = context;
        this.articlesArrayList = articlesArrayList;

        // [START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        // [END configuration]
    }


    @NonNull
    @Override
    public ArticleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.article_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleAdapter.ViewHolder holder, int position) {
        // Add final to ensure the closure captures the correct object
        final Articles article = articlesArrayList.get(position);

        // [START config_firebase reference]
        // Move the declaration of Ref into onBindViewHolder() to avoid sharing the same variable
        DatabaseReference databaseUserRef, databaseSavedArticleRef; // real-time db ref
        databaseUserRef = Rdb.getReference().child("user").child(article.getUID());
        databaseSavedArticleRef = Rdb.getReference().child("savedArticle").child(auth.getUid()).child(article.getArticleID());
        // [END config_firebase reference]

        // [START config_layout]
        holder.date.setText(article.getDate());
        holder.headline.setText(article.getHeadline());
        // Check if the current user is the creator of the article
        if (article.getUID().equals(auth.getUid())) {
            // Hide save button if the user is the creator
            holder.btnSave.setVisibility(View.GONE);
        } else {
            // Show save button for other users
            holder.btnSave.setVisibility(View.VISIBLE);
        }
        // [Start Gain Article Creator Info]
        databaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Layout Control
                    Picasso.get().load(snapshot.child("avatarURL").getValue(String.class)).into(holder.avatar);
                    holder.username.setText(snapshot.child("username").getValue(String.class));
                } else {
                    Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Personal information cannot be obtained: " + error.getMessage());
            }
        });
        // Check saved state of the article
        databaseSavedArticleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Saved
                    holder.btnSave.setImageResource(R.drawable.baseline_turned_in_24);
                } else {
                    // Unsaved
                    holder.btnSave.setImageResource(R.drawable.baseline_turned_in_not_24);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // [END config_layout]

        // [START layout component function]
        // Handle save button click (toggle save/remove article)
        holder.btnSave.setOnClickListener(view -> {
            databaseSavedArticleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Article is already saved, unsaved it
                        databaseSavedArticleRef.removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Article unsaved", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to unsaved article", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Articles storedArticle = new Articles();
                        storedArticle.setArticleID(article.getArticleID());
                        // Article is not saved, save it
                        databaseSavedArticleRef.setValue(storedArticle).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Article saved", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to save article", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    // * When every save changes -> Notify of changes to data set
                    notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Save/unsave operation failed: " + error.getMessage());
                }
            });
        });
        // Open article details on item click
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ArticleActivity.class);
            intent.putExtra("article", article);  // Pass the article object
            context.startActivity(intent);
        });
        // [END layout component function]
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