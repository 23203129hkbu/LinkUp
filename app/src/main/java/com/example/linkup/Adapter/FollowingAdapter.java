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

import com.example.linkup.ChatOperation.ChatRoomActivity;
import com.example.linkup.HomeOperation.UserProfile;
import com.example.linkup.Object.Users;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

// ✅
public class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Users> usersArrayList;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db

    // Constructor
    public FollowingAdapter(Context context, ArrayList<Users> usersArrayList) {
        this.context = context;
        this.usersArrayList = usersArrayList;

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]
    }


    @NonNull
    @Override
    public FollowingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.following_item, parent, false);

        return new FollowingAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowingAdapter.ViewHolder holder, int position) {
        final Users user = usersArrayList.get(position);
        // [START config_firebase reference]
        DatabaseReference databaseFollowingRef, databaseFollowerRef; // real-time db ref
        databaseFollowingRef = Rdb.getReference().child("following").child(auth.getUid()).child(user.getUID());
        databaseFollowerRef = Rdb.getReference().child("follower").child(user.getUID()).child(auth.getUid());
        // [END config_firebase reference]

        // [START config_layout]
        Picasso.get().load(user.getAvatarURL()).into(holder.avatar);
        holder.username.setText(user.getUsername());
        // [END config_layout]

        // [START layout component function]
        // -> Chat Room
        holder.btnMessage.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatRoomActivity.class);
            intent.putExtra("user", user);  // Pass the article object
            context.startActivity(intent);
        });
        // -> Unfollow
        holder.btnRemove.setOnClickListener(view -> {
            databaseFollowerRef.removeValue();
            databaseFollowingRef.removeValue();
            Toast.makeText(context, "unfollowed this user", Toast.LENGTH_SHORT).show();
        });
        // Open user profile on item click
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, UserProfile.class);
            intent.putExtra("user", user);  // Pass the article object
            context.startActivity(intent);
        });
        // [END layout component function]
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        ImageView btnRemove;
        TextView username, btnMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            username = itemView.findViewById(R.id.username);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            btnMessage = itemView.findViewById(R.id.btnMessage);
        }
    }
}