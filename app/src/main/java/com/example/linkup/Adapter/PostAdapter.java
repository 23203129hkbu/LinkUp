package com.example.linkup.Adapter;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.media.browse.MediaBrowser;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.CommunityOperation.ArticleActivity;
import com.example.linkup.CommunityOperation.CommunityMenu;
import com.example.linkup.CommunityOperation.MyArticlesActivity;
import com.example.linkup.Fragment.CommunityFragment;
import com.example.linkup.HomeOperation.PostMenu;
import com.example.linkup.HomeOperation.UserProfile;
import com.example.linkup.Object.ArticleComments;
import com.example.linkup.Object.Articles;
import com.example.linkup.Object.Posts;
import com.example.linkup.Object.Users;
import com.example.linkup.R;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Posts> postsArrayList;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef, databaseLikeRef, databaseFollowingRef, databaseFollowerRef; // real-time db ref
    // following checker
    Boolean checker;

    // Constructor
    public PostAdapter(Context context, ArrayList<Posts> postsArrayList) {
        this.context = context;
        this.postsArrayList = postsArrayList;
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]
    }


    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        final Posts post = postsArrayList.get(position);
        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user").child(post.getUID());
        databaseLikeRef = Rdb.getReference().child("likedPost");
        databaseFollowingRef = Rdb.getReference().child("following").child(auth.getUid());
        databaseFollowerRef = Rdb.getReference().child("follower").child(post.getUID());
        // [END config_firebase reference]

        // [START config_layout]
        holder.btnLike.setImageResource(R.drawable.baseline_favorite_border_24); // 默认状态
        holder.btnLike.setTag(post.getPostID()); // 绑定唯一标识
        holder.description.setText(post.getDescription());
        holder.dateAndTime.setText(post.getDate()+" "+post.getTime());
        if (post.getUID().equals(auth.getUid())) {
            holder.btnFollow.setVisibility(View.INVISIBLE);
        } else {
            holder.btnFollow.setVisibility(View.VISIBLE);
            databaseFollowingRef.child(post.getUID()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot followingSnapshot) {
                    if (followingSnapshot.exists()){
                        checker = true;
                        holder.btnFollow.setText("Following");
                    }else{
                        checker = false;
                        holder.btnFollow.setText("Follow");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
        if (post.getType().equals("video")) {
            holder.video.setVisibility(View.VISIBLE);
            holder.image.setVisibility(View.INVISIBLE);
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
        } else {
            holder.image.setVisibility(View.VISIBLE);
            holder.video.setVisibility(View.INVISIBLE);
            Picasso.get().load(post.getPostURL()).into(holder.image);
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
                Log.w(TAG, "Personal information cannot be obtained: "+error.getMessage());
            }
        });
        // Load Button Like / Gain existing likes
        databaseLikeRef.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Count total likes
                int noOfLikes = (int) snapshot.getChildrenCount();
                holder.likes.setText(String.valueOf(noOfLikes));
                // Check if the current user has liked the article
                // There also use .child(....) -> if (snapshot.exists()) {..}
                // But count the number of likes
                if (snapshot.hasChild(auth.getUid())) {
                    // User has liked the article
                    holder.btnLike.setImageResource(R.drawable.baseline_favorite_24); // Change to liked icon
                } else {
                    // User has not liked the article
                    holder.btnLike.setImageResource(R.drawable.baseline_favorite_border_24); // Change to unliked icon
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load likes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // [END config_layout]

        // [START layout component function]
        // Handle save button click (toggle save/remove article)
        holder.btnLike.setOnClickListener(view -> {
            String currentPostId = (String) holder.btnLike.getTag();
            if (currentPostId == null || !currentPostId.equals(post.getPostID())) {
                return; // 防止异步操作导致的错位
            }
            DatabaseReference currentRef = databaseLikeRef.child(currentPostId).child(auth.getUid());

            currentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!currentPostId.equals(holder.btnLike.getTag())) {
                        return; // 二次验证防止错位
                    }

                    if (snapshot.exists()) {
                        // 移除保存
                        currentRef.removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    holder.btnLike.setImageResource(R.drawable.baseline_favorite_border_24);
                                });
                    } else {
                        Users user = new Users();
                        user.setUID(auth.getUid());
                        // 添加保存
                        currentRef.setValue(user)
                                .addOnSuccessListener(aVoid -> {
                                    holder.btnLike.setImageResource(R.drawable.baseline_favorite_24);
                                });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        holder.btnMenu.setOnClickListener(view -> {
            PostMenu pm = new PostMenu (post.getPostID(),post.getPostURL());
            pm.show(((AppCompatActivity) context).getSupportFragmentManager(), "bottom");
        });
        // Follow / UnFollow user
        databaseFollowingRef.child(post.getUID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot followingSnapshot) {
                boolean isFollowing = followingSnapshot.exists();
                holder.btnFollow.setText(isFollowing ? "Following" : "Follow");

                holder.btnFollow.setOnClickListener(view -> {
                    if (isFollowing) {
                        databaseFollowingRef.child(post.getUID()).removeValue();
                        databaseFollowerRef.child(auth.getUid()).removeValue();
                        Toast.makeText(context, "Unfollowed this user", Toast.LENGTH_SHORT).show();
                    } else {
                        Users storedUser = new Users();
                        storedUser.setUID(post.getUID());
                        databaseFollowingRef.child(post.getUID()).setValue(storedUser);
                        storedUser.setUID(auth.getUid());
                        databaseFollowerRef.child(auth.getUid()).setValue(storedUser);
                        Toast.makeText(context, "Followed this user", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        // If the creator of the post is the user, can't move on to the user profile
        // Open article details on item click
        holder.userProfile.setOnClickListener(view -> {
            Users user = new Users();
            user.setUID(post.getUID());
            if (!user.getUID().equals(auth.getUid())){
                Intent intent = new Intent(context, UserProfile.class);
                intent.putExtra("user", user);  // Pass the article object
                context.startActivity(intent);
            }
        });
        // [END layout component function]
    }

    @Override
    public int getItemCount() {
        return postsArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout userProfile;
        CircleImageView avatar;
        TextView username, btnFollow, likes, comments, description, dateAndTime;
        ImageView btnMenu, image, btnLike, btnComment;
        PlayerView video;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfile = itemView.findViewById(R.id.userProfile);
            avatar = itemView.findViewById(R.id.avatar);
            username = itemView.findViewById(R.id.username);
            btnFollow = itemView.findViewById(R.id.btnFollow);
            likes = itemView.findViewById(R.id.likes);
            comments = itemView.findViewById(R.id.comments);
            description = itemView.findViewById(R.id.description);
            dateAndTime = itemView.findViewById(R.id.dateAndTime);
            btnMenu = itemView.findViewById(R.id.btnMenu);
            image = itemView.findViewById(R.id.image);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            video = itemView.findViewById(R.id.video);
        }
    }
}