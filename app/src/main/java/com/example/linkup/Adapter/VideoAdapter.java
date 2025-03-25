package com.example.linkup.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Object.Posts;
import com.example.linkup.R;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Posts> postsArrayList;

    // Constructor
    public VideoAdapter(Context context, ArrayList<Posts> postsArrayList) {
        this.context = context;
        this.postsArrayList = postsArrayList;
    }


    @NonNull
    @Override
    public VideoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_post_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoAdapter.ViewHolder holder, int position) {
        final Posts post = postsArrayList.get(position);
        // [START config_layout]
        try {
            SimpleExoPlayer simpleExoPlayer = new SimpleExoPlayer.Builder(context).build();
            holder.video.setPlayer(simpleExoPlayer);
            MediaItem mediaItem = MediaItem.fromUri(post.getPostURL());
            simpleExoPlayer.setMediaItem(mediaItem);
            simpleExoPlayer.prepare();
            simpleExoPlayer.setPlayWhenReady(false);
        } catch (Exception e) {
            Toast.makeText(context, "Error loading video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        // [END config_layout]
    }

    @Override
    public int getItemCount() {
        return postsArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        PlayerView video;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            video = itemView.findViewById(R.id.video);
        }
    }
}
