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
    FirebaseDatabase Rdb;

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

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleCommentAdapter.ViewHolder holder, int position) {
        ArticleComments comment = commentsArrayList.get(position);

        Picasso.get().load(comment.getImageURL()).into(holder.netizenAvatar);
        holder.comment.setText(comment.getComment());
        holder.date.setText(comment.getDate());
        holder.netizenName.setText(comment.getUsername());
    }

    @Override
    public int getItemCount() {
        return commentsArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView netizenAvatar, btnLike;
        TextView netizenName, comment, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            netizenAvatar = itemView.findViewById(R.id.netizenAvatar);
            netizenName = itemView.findViewById(R.id.netizenName);
            date = itemView.findViewById(R.id.date);
            comment = itemView.findViewById(R.id.comment);
            btnLike = itemView.findViewById(R.id.btnLike);
        }
    }
}