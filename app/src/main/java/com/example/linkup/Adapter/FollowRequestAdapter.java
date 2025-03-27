package com.example.linkup.Adapter;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.CommunityOperation.ArticleActivity;
import com.example.linkup.HomeOperation.UserProfile;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class FollowRequestAdapter extends RecyclerView.Adapter<FollowRequestAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Users> usersArrayList;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef, databaseFollowerRef, databaseFollowingRef, databaseRequestedRef; // real-time db ref

    // Constructor
    public FollowRequestAdapter(Context context, ArrayList<Users> usersArrayList) {
        this.context = context;
        this.usersArrayList = usersArrayList;

        // [START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        // [END configuration]
    }


    @NonNull
    @Override
    public FollowRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.request_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowRequestAdapter.ViewHolder holder, int position) {
        final Users user = usersArrayList.get(position);
        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user");
        databaseFollowerRef = Rdb.getReference().child("user").child(auth.getUid()).child("follower");
        databaseFollowingRef = Rdb.getReference().child("user").child(user.getUID()).child("following");
        databaseRequestedRef = Rdb.getReference().child("user").child(auth.getUid()).child("requested").child(user.getUID());
        // [END config_firebase reference]

        // [START config_layout]
        // [Start Gain User Info]
        databaseUserRef.child(user.getUID()).addValueEventListener(new ValueEventListener() {
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
        // [END config_layout]

        // [START layout component function]
        // Reject Follow Request
        holder.btnReject.setOnClickListener(view -> {
            // Remove the user from the list, regardless of whether they accept or reject it.
            removeFollowRequest();
        });
        // Accept Follow Request
        holder.btnAccept.setOnClickListener(view -> {
            // Remove the user from the list, regardless of whether they accept or reject it.
            removeFollowRequest();
            Users storedUser = new Users();
            storedUser.setUID(user.getUID());
            databaseFollowerRef.child(auth.getUid()).setValue(user);
            storedUser.setUID(auth.getUid());
            databaseFollowingRef.child(user.getUID()).setValue(user);
            Toast.makeText(context, "The user has become your followers", Toast.LENGTH_SHORT).show();
        });
        // Open article details on item click
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, UserProfile.class);
            intent.putExtra("user", user);  // Pass the article object
            context.startActivity(intent);
        });
        // [END layout component function]
    }

    private void removeFollowRequest() {
        databaseRequestedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Article is already saved, remove it
                    databaseRequestedRef.removeValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        TextView username, btnReject, btnAccept;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            username = itemView.findViewById(R.id.username);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnAccept = itemView.findViewById(R.id.btnAccept);
        }
    }

}
