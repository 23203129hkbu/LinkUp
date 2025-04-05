package com.example.linkup.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.ChatOperation.ChatRoomActivity;
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

// ✅
public class FollowerAdapter extends RecyclerView.Adapter<FollowerAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Users> usersArrayList;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    // private AC
    Boolean privateAC;



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
        DatabaseReference databaseUserRef, databaseFollowingRef, databaseRequestedRef, databaseFollowerRef, databaseMyFollowerRef,databaseOtherSideFollowingRef; // real-time db ref
        databaseUserRef = Rdb.getReference().child("user").child(user.getUID());
        databaseFollowingRef = Rdb.getReference().child("following").child(auth.getUid()).child(user.getUID());
        databaseRequestedRef = Rdb.getReference().child("requested").child(user.getUID()).child(auth.getUid());
        databaseFollowerRef = Rdb.getReference().child("follower").child(user.getUID()).child(auth.getUid());
        databaseMyFollowerRef = Rdb.getReference().child("follower").child(auth.getUid()).child(user.getUID());
        databaseOtherSideFollowingRef = Rdb.getReference().child("following").child(user.getUID()).child(auth.getUid());


        // For private Account

        // [END config_firebase reference]
        // [START config_layout]
        Picasso.get().load(user.getAvatarURL()).into(holder.avatar);
        holder.username.setText(user.getUsername());
        // Load / Gain User Data -> Determine user status private / public
        databaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // If User change the user state/privacy
                if (snapshot.exists()){
                    if (snapshot.child("privacy").getValue(String.class).equals("Private")) {
                        privateAC = true;
                    } else {
                        privateAC = false;
                    }
                    updateButtonAction(holder,databaseFollowingRef, databaseRequestedRef);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Check if you are a following
        databaseFollowingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updateButtonAction(holder,databaseFollowingRef, databaseRequestedRef);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load likes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // If follow request is change -> when private Account allow the user follow
        databaseRequestedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updateButtonAction(holder,databaseFollowingRef, databaseRequestedRef);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // [END config_layout]

        // [START layout component function]
        // Follow, Message, Wait Request
        holder.btnAction.setOnClickListener(view -> {
            if (holder.btnAction.getText().toString().equals("Message")){
                // Move Chat Room
                Intent intent = new Intent(context, ChatRoomActivity.class);
                intent.putExtra("user", user);  // Pass the article object
                context.startActivity(intent);
            }else if (holder.btnAction.getText().toString().equals("Follow Back")){
                if (!privateAC){
                    // directly follow
                    Users storedUser = new Users();
                    storedUser.setUID(auth.getUid());
                    databaseFollowerRef.child(auth.getUid()).setValue(storedUser);
                    storedUser.setUID(user.getUID());
                    databaseFollowingRef.child(user.getUID()).setValue(storedUser);
                    Toast.makeText(context, "followed this user", Toast.LENGTH_SHORT).show();

                }else{
                    // send follow request
                    Users storedUser = new Users();
                    storedUser.setUID(auth.getUid());
                    databaseRequestedRef.setValue(storedUser);
                    Toast.makeText(context, "Send follow request", Toast.LENGTH_SHORT).show();
                }
            }else{
                // cancel follow request
                databaseRequestedRef.removeValue();
                Toast.makeText(context, "Cancel follow request", Toast.LENGTH_SHORT).show();
            }
        });
        // -> Remove Follower
        holder.btnRemove.setOnClickListener(view -> {
            databaseOtherSideFollowingRef.removeValue();
            databaseMyFollowerRef.removeValue();
            Toast.makeText(context, "remove this follower", Toast.LENGTH_SHORT).show();
        });
        // Open user profile on item click
        holder.itemView.setOnClickListener(view -> {
            if (!user.getUID().equals(auth.getUid())){
                Intent intent = new Intent(context, UserProfile.class);
                intent.putExtra("user", user);  // Pass the article object
                context.startActivity(intent);
            }
        });
        // [END layout component function]
    }

    private void updateButtonAction(ViewHolder holder, DatabaseReference followingRef, DatabaseReference requestedRef) {
        followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isFollowed = snapshot.exists();
                requestedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isRequested = snapshot.exists();
                        // Update button text based on follow status
                        if (isFollowed) {
                            holder.btnAction.setText("Message");
                            holder.btnAction.setBackgroundTintList(null);
                        } else {
                            if (isRequested) {
                                holder.btnAction.setText("Requested");
                                holder.btnAction.setBackgroundTintList(null);
                            } else {
                                holder.btnAction.setText("Follow Back");
                                holder.btnAction.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.purple_2));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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