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
import com.example.linkup.Object.ArticleComments;
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

public class ArticleCommentAdapter extends RecyclerView.Adapter<ArticleCommentAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<ArticleComments> commentsArrayList;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef; // real-time db ref

    // Constructor
    public ArticleCommentAdapter(Context context, ArrayList<ArticleComments> commentsArrayList) {
        this.context = context;
        this.commentsArrayList = commentsArrayList;
    }


    @NonNull
    @Override
    public ArticleCommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.article_comment_item, parent, false);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user");
        // [END config_firebase reference]

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleCommentAdapter.ViewHolder holder, int position) {
        ArticleComments comment = commentsArrayList.get(position);
        // [START config_layout]
        // [Start Gain Article Creator Info]
        databaseUserRef.child(comment.getUID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Layout Control
                    Picasso.get().load(snapshot.child("avatarURL").getValue(String.class)).into(holder.netizenAvatar);
                    holder.netizenName.setText(snapshot.child("username").getValue(String.class));
                } else {
                    Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Personal information cannot be obtained: "+error.getMessage());
            }
        });
        holder.comment.setText(comment.getComment());
        holder.date.setText(comment.getDate());
        // [END config_layout]
    }

    @Override
    public int getItemCount() {
        return commentsArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView netizenAvatar;
        TextView netizenName, comment, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            netizenAvatar = itemView.findViewById(R.id.netizenAvatar);
            netizenName = itemView.findViewById(R.id.netizenName);
            date = itemView.findViewById(R.id.date);
            comment = itemView.findViewById(R.id.comment);
        }
    }
}