package com.example.linkup.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.ChatOperation.ChatRoomActivity;
import com.example.linkup.EventOperation.EventActivity;
import com.example.linkup.HomeOperation.UserProfile;
import com.example.linkup.Object.Events;
import com.example.linkup.Object.Modifications;
import com.example.linkup.Object.Posts;
import com.example.linkup.Object.Users;
import com.example.linkup.ProfileOperation.SettingActivity;
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

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Users> usersArrayList;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    // Event Info
    Events event = new Events();

    // Constructor
    public ParticipantAdapter(Context context, ArrayList<Users> usersArrayList, Events event) {
        this.context = context;
        this.usersArrayList = usersArrayList;
        this.event = event;

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]
    }


    @NonNull
    @Override
    public ParticipantAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.participant_item, parent, false);

        return new ParticipantAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantAdapter.ViewHolder holder, int position) {
        final Users user = usersArrayList.get(position);

        // [START config_firebase reference]
        DatabaseReference databaseParticipantRef, databaseModificationRef; // real-time db ref
        databaseParticipantRef = Rdb.getReference().child("eventParticipant").child(event.getEventID()).child(user.getUID());
        databaseModificationRef = Rdb.getReference().child("modification").child(user.getUID()).child(event.getEventID());
        // [END config_firebase reference]

        // [START config_layout]
        Picasso.get().load(user.getAvatarURL()).into(holder.avatar);
        holder.username.setText(user.getUsername());
        if (user.getUID().equals(auth.getUid())) {
            holder.btnRemove.setVisibility(View.GONE);
        }
        // [END config_layout]

        // [START layout component function]
        // Remove user
        holder.btnRemove.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Confirmation Notification")
                    .setMessage("Do you want to remove this participant from the list?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            databaseParticipantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Modifications modification = new Modifications();
                                    modification.setEventId(event.getEventID());
                                    modification.setContent("You have been removed from Event: "+ event.getEventName());
                                    modification.setRead(false);
                                    databaseModificationRef.setValue(modification).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "User is removed", Toast.LENGTH_SHORT).show();
                                            databaseParticipantRef.removeValue();
                                        } else {
                                            Toast.makeText(context, "Failed to unsaved event", Toast.LENGTH_SHORT).show();
                                        }
                                    });


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(context, "Failed to reload : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            // this line to show the dialog
            builder.create().show();
        });
        // [END layout component function]
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        TextView username, btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            username = itemView.findViewById(R.id.username);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}

