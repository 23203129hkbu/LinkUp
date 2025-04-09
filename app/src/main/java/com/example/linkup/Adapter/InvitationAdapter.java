package com.example.linkup.Adapter;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.HomeOperation.UserProfile;
import com.example.linkup.Object.Events;
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

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Users> usersArrayList;
    Events event;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db

    // Constructor
    public InvitationAdapter(Context context, ArrayList<Users> usersArrayList, Events event) {
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
    public InvitationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.invitation_item, parent, false);

        return new InvitationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvitationAdapter.ViewHolder holder, int position) {
        final Users user = usersArrayList.get(position);
        // [START config_firebase reference]
        DatabaseReference databaseInvitationRef, databaseIsJoinedRef; // real-time db ref
        databaseIsJoinedRef = Rdb.getReference().child("eventParticipant").child(event.getEventID()).child(user.getUID());
        databaseInvitationRef = Rdb.getReference().child("invitation").child(user.getUID()).child(event.getEventID());
        // [END config_firebase reference]

        // [START config_layout]
        Picasso.get().load(user.getAvatarURL()).into(holder.avatar);
        holder.username.setText(user.getUsername());
        // [Gain Event IsJoined Info]
        databaseIsJoinedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Layout Control
                    holder.btnInvite.setBackgroundTintList(null);
                    holder.btnInvite.setText("Joined");
                } else {
                    databaseInvitationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                holder.btnInvite.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red_3));
                                holder.btnInvite.setText("Requested");
                            } else {
                                holder.btnInvite.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.blue_3));
                                holder.btnInvite.setText("Invite");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Personal information cannot be obtained: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Personal information cannot be obtained: " + error.getMessage());
            }
        });

        // [Gain Event Waiting for his approval.]
        databaseInvitationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.btnInvite.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red_3));
                    holder.btnInvite.setText("Requested");
                } else {
                    databaseIsJoinedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                holder.btnInvite.setBackgroundTintList(null);
                                holder.btnInvite.setText("Joined");
                            } else {
                                holder.btnInvite.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.blue_3));
                                holder.btnInvite.setText("Invite");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Personal information cannot be obtained: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Personal information cannot be obtained: " + error.getMessage());
            }
        });
        // [END config_layout]

        holder.btnInvite.setOnClickListener(view -> {
            if (!holder.btnInvite.getText().toString().equals("Joined")) {
                databaseInvitationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && holder.btnInvite.getText().toString().equals("Requested")) {
                            // event is already saved, unsaved it
                            databaseInvitationRef.removeValue().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "cancel invitation", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Failed to unsaved event", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Events storedEvent = new Events();
                            storedEvent.setEventID(event.getEventID());
                            // Article is not saved, save it
                            databaseInvitationRef.setValue(storedEvent).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "send invitation", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Failed to save event", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Save/unsave operation failed: " + error.getMessage());
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        TextView username, btnInvite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            username = itemView.findViewById(R.id.username);
            btnInvite = itemView.findViewById(R.id.btnInvite);
        }
    }
}


