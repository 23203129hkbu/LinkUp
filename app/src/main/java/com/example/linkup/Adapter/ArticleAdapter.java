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
import com.example.linkup.CommunityOperation.CreateCommunityPost;
import com.example.linkup.Fragment.CommunityFragment;
import com.example.linkup.Object.Articles;
import com.example.linkup.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

// enables the usage of an existing class’s interface as an additional
// converting the data to be suitable for article_item
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.viewholder> {
    CommunityFragment communityFragment; // which screen
    ArrayList<Articles> articlesArrayList; // list which store the object user

    public ArticleAdapter(CommunityFragment communityFragment, ArrayList<Articles> articlesArrayList) {
        this.communityFragment = communityFragment; // The activity in which the RecyclerView is displayed.
        this.articlesArrayList = articlesArrayList;
    }

    // is called when a new ViewHolder object is needed.
    // It inflates the layout file user_item (which defines how each item in the list should look) and creates a new viewholder object.
    @NonNull
    @Override
    public ArticleAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(communityFragment.getContext()).inflate(R.layout.article_item, parent, false);
        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleAdapter.viewholder holder, int position) {

        Articles article = articlesArrayList.get(position);
        Picasso.get().load(article.getImageURL()).into(holder.avatar);
        holder.username.setText(article.getUsername());
        holder.date.setText(article.getDate());
        holder.headline.setText(article.getHeadline());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(communityFragment.getContext(), ArticleActivity.class);
                // intent.putExtra("receiverUsername", users.getUsername());
                communityFragment.startActivity(intent);
            }
        });

        holder.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(communityFragment.getContext(), "Article Saved", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return articlesArrayList.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        ImageView avatar, btnSave;
        TextView username, date, headline;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            username = itemView.findViewById(R.id.username);
            date = itemView.findViewById(R.id.date);
            headline = itemView.findViewById(R.id.headline);
            btnSave = itemView.findViewById(R.id.btnSave);

        }
    }
}
