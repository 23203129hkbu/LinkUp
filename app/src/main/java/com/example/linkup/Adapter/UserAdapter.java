package com.example.linkup.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.ChatOperation.ChatRoomActivity;
import com.example.linkup.CommunityOperation.ArticleActivity;
import com.example.linkup.HomeOperation.UserProfile;
import com.example.linkup.Object.Posts;
import com.example.linkup.Object.Users;
import com.example.linkup.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

// ✅
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Users> usersArrayList;
    String location;

    // Constructor
    public UserAdapter(Context context, ArrayList<Users> usersArrayList, String location) {
        this.context = context;
        this.usersArrayList = usersArrayList;
        this.location = location;
    }


    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        final Users user = usersArrayList.get(position);
        // [START config_layout]
        Picasso.get().load(user.getAvatarURL()).into(holder.avatar);
        holder.username.setText(user.getUsername());
        // [END config_layout]

        // [START layout component function]
        // Open user profile on item click
        holder.itemView.setOnClickListener(view -> {
            Intent intent;
            if (location.equals("ChatRoom")){
                intent = new Intent(context, ChatRoomActivity.class);
            }else{
                intent = new Intent(context, UserProfile.class);
            }
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
        TextView username;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            username = itemView.findViewById(R.id.username);
        }
    }
}