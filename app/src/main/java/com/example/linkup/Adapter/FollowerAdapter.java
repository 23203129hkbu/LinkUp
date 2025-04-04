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

import com.example.linkup.HomeOperation.UserProfile;
import com.example.linkup.Object.Users;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FollowerAdapter extends RecyclerView.Adapter<FollowerAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Users> usersArrayList;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db

    // Constructor
    public FollowerAdapter(Context context, ArrayList<Users> usersArrayList) {
        this.context = context;
        this.usersArrayList = usersArrayList;

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]
    }


    @NonNull
    @Override
    public FollowerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.followers_item, parent, false);

        return new FollowerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowerAdapter.ViewHolder holder, int position) {
        final Users user = usersArrayList.get(position);

        // [START config_firebase reference]
        DatabaseReference databaseFollowingRef, databaseFollowerRef, databaseOtherSideFollowerRef; // real-time db ref
        databaseOtherSideFollowerRef = Rdb.getReference().child("follower").child(user.getUID());
        databaseFollowingRef = Rdb.getReference().child("following").child(auth.getUid()).child(user.getUID());
        databaseFollowerRef = Rdb.getReference().child("follower").child(auth.getUid());
        // [END config_firebase reference]
        // [START config_layout]
        Picasso.get().load(user.getAvatarURL()).into(holder.avatar);
        holder.username.setText(user.getUsername());
        // Check if you are a follower of this post
        databaseFollowingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    // holder.btnAction.setText();
                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load likes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // [END config_layout]

        // [START layout component function]
        // Open user profile on item click
        holder.itemView.setOnClickListener(view -> {
            if (user.getUID().equals(auth.getUid())){
                Intent intent = new Intent(context, UserProfile.class);
                intent.putExtra("user", user);  // Pass the article object
                context.startActivity(intent);
            }
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
        TextView username, btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            username = itemView.findViewById(R.id.username);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            btnAction = itemView.findViewById(R.id.btnAction);

        }
    }
}