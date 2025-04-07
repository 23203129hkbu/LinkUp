package com.example.linkup.Adapter;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.HomeOperation.PostMenu;
import com.example.linkup.HomeOperation.UserProfile;
import com.example.linkup.Object.Articles;
import com.example.linkup.Object.Events;
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

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Events> eventsArrayList;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    // Boolean checker
    String startDateAndTime, endDateAndTime;


    // Constructor
    public EventAdapter(Context context, ArrayList<Events> eventsArrayList) {
        this.context = context;
        this.eventsArrayList = eventsArrayList;

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]
    }


    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);

        return new EventAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventAdapter.ViewHolder holder, int position) {
        final Events event = eventsArrayList.get(position);
        // event.getUID and event.getEventID never change

        // [START config_firebase reference]
        DatabaseReference databaseUserRef,databaseEventRef, databaseParticipantRef, databaseSavedEventRef,databaseIsJoinedRef; // real-time db ref
        databaseUserRef = Rdb.getReference().child("user").child(event.getUID());
        databaseParticipantRef = Rdb.getReference().child("eventParticipant").child(event.getEventID());
        // join / cancel
        databaseIsJoinedRef = Rdb.getReference().child("eventParticipant").child(event.getEventID()).child(auth.getUid());
        databaseSavedEventRef = Rdb.getReference().child("savedEvent").child(auth.getUid()).child(event.getEventID());
        // [END config_firebase reference]

        // [START config_layout]
        if (event.getUID().equals(auth.getUid())){
            // Hide save join button if the user is the creator
            holder.btnJoin.setVisibility(View.GONE);
            holder.btnSave.setVisibility(View.GONE);
            holder.btnMenu.setVisibility(View.VISIBLE);
        }else{
            // Show save button for other users
            holder.btnSave.setVisibility(View.VISIBLE);
            if (!event.isPublic()){
                holder.btnMenu.setVisibility(View.GONE);
            }else {
                holder.btnMenu.setVisibility(View.VISIBLE);
            }
        }

        // [Gain Post Creator Info]
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
        // [Gain Event Info]
        holder.eventName.setText(event.getEventName());
        holder.location.setText(event.getLocation());
        holder.startDateTime.setText("Start: " + event.getStartDate() + ", " + event.getStartTime());
        holder.endDateTime.setText("End: " + event.getEndDate() + ", " + event.getEndTime());
        holder.description.setText(event.getDescription());
        holder.participantLimit.setText("Participants: " + event.getParticipantLimit());
        // [Gain Event Quota / Count Join number of people]
        databaseIsJoinedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.btnJoin.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red_3));
                    holder.btnJoin.setText("CANCEL");
                } else {
                    holder.btnJoin.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.green_1));
                    holder.btnJoin.setText("JOIN");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Personal information cannot be obtained: "+error.getMessage());
            }
        });
        // Calculate remaining quota
        databaseParticipantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int participantCount = (int) snapshot.getChildrenCount(); // Count actual participants
                int maxParticipants = event.getParticipantLimit(); // Retrieve max participants
                int remainingQuota = maxParticipants - participantCount;

                // Update UI based on remaining quota
                if (remainingQuota <= 0) {
                    holder.eventStatusAndQuota.setText("Remaining quota: Full");
                    holder.eventStatusAndQuota.setTextColor(ContextCompat.getColor(context, R.color.red_3));
                    holder.btnJoin.setVisibility(View.GONE); // Hide join button if full
                } else {
                    holder.eventStatusAndQuota.setText("Remaining quota: " + remainingQuota);
                    holder.eventStatusAndQuota.setTextColor(ContextCompat.getColor(context, R.color.green_1));
                    if (event.getUID().equals(auth.getUid())){
                        // Hide save join button if the user is the creator
                        holder.btnJoin.setVisibility(View.GONE);
                    }else{
                        holder.btnJoin.setVisibility(View.VISIBLE);
                    }
                     // Show join button if quota available
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load participant data: " + error.getMessage());
            }
        });

        // Check saved state of the event
        databaseSavedEventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Saved
                    holder.btnSave.setImageResource(R.drawable.baseline_turned_in_24);
                } else {
                    // Unsaved
                    holder.btnSave.setImageResource(R.drawable.baseline_turned_in_not_24);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // [END config_layout]

        // [START layout component function]
        // Handle save button click (toggle save/remove event)
        holder.btnSave.setOnClickListener(view -> {
            databaseSavedEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // event is already saved, unsaved it
                        databaseSavedEventRef.removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Event unsaved", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to unsaved event", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Events storedEvent = new Events();
                        storedEvent.setEventID(event.getEventID());
                        // Article is not saved, save it
                        databaseSavedEventRef.setValue(storedEvent).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Event saved", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to save event", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    // * When every save changes -> Notify of changes to data set
                    notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Save/unsave operation failed: " + error.getMessage());
                }
            });
        });
        // Join the event
        holder.btnJoin.setOnClickListener(view -> {
            databaseIsJoinedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Cancel
                        databaseIsJoinedRef.removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Event unsaved", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to unsaved event", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Join
                        Users storedUser = new Users();
                        storedUser.setUID(auth.getUid());
                        databaseIsJoinedRef.setValue(storedUser).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Event saved", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to save event", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    // * When every save changes -> Notify of changes to data set
                    notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Save/unsave operation failed: " + error.getMessage());
                }
            });
        });
        // Handle save button click (toggle save/remove event)
        holder.btnMenu.setOnClickListener(view -> {
            databaseSavedEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // event is already saved, unsaved it
                        databaseSavedEventRef.removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Event unsaved", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to unsaved event", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Events storedEvent = new Events();
                        storedEvent.setEventID(event.getEventID());
                        // Article is not saved, save it
                        databaseSavedEventRef.setValue(storedEvent).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Event saved", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to save event", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    // * When every save changes -> Notify of changes to data set
                    notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Save/unsave operation failed: " + error.getMessage());
                }
            });
        });
        // [END layout component function]
    }

    @Override
    public int getItemCount() {
        return eventsArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        TextView username, eventName, startDateTime, endDateTime, location, participantLimit, description, eventStatusAndQuota, btnJoin;
        ImageView btnMenu, btnLocation, btnSave;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            username = itemView.findViewById(R.id.username);
            eventName = itemView.findViewById(R.id.eventName);
            startDateTime = itemView.findViewById(R.id.startDateTime);
            endDateTime = itemView.findViewById(R.id.endDateTime);
            location= itemView.findViewById(R.id.location);
            participantLimit = itemView.findViewById(R.id.participantLimit);
            description = itemView.findViewById(R.id.description);
            eventStatusAndQuota = itemView.findViewById(R.id.eventStatusAndQuota);
            btnMenu = itemView.findViewById(R.id.btnMenu);
            btnJoin = itemView.findViewById(R.id.btnJoin);
            btnLocation = itemView.findViewById(R.id.btnLocation);
            btnSave = itemView.findViewById(R.id.btnSave);
        }
    }
}
