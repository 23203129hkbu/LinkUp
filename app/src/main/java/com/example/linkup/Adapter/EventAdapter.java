package com.example.linkup.Adapter;

import static android.content.ContentValues.TAG;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import com.example.linkup.ChatOperation.ChatRoomActivity;
import com.example.linkup.CommunityOperation.UpdateCommunityPost;
import com.example.linkup.EventOperation.CreateEvent;
import com.example.linkup.EventOperation.EventActivity;
import com.example.linkup.EventOperation.FindRoute;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Events> eventsArrayList;
    AppCompatActivity activity;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db


    // Constructor
    public EventAdapter(Context context, ArrayList<Events> eventsArrayList, AppCompatActivity activity) {
        this.context = context;
        this.eventsArrayList = eventsArrayList;
        this.activity = activity; // Assign activity

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
        DatabaseReference databaseUserRef, databaseParticipantRef, databaseSavedEventRef, databaseIsJoinedRef; // real-time db ref
        databaseUserRef = Rdb.getReference().child("user").child(event.getUID());
        databaseIsJoinedRef = Rdb.getReference().child("eventParticipant").child(event.getEventID()).child(auth.getUid());
        databaseParticipantRef = Rdb.getReference().child("eventParticipant").child(event.getEventID());
        databaseSavedEventRef = Rdb.getReference().child("savedEvent").child(auth.getUid()).child(event.getEventID());
        // [END config_firebase reference]

        // [START config_layout]
        if (event.getUID().equals(auth.getUid())){
            holder.btnSave.setVisibility(View.GONE);
        }

        try {
            // Parse the event's start and end date/time
            String eventStartDateTime = event.getStartDate() + " " + event.getStartTime(); // Combine start date and time
            String eventEndDateTime = event.getEndDate() + " " + event.getEndTime(); // Combine end date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // Adjust format as needed

            Date eventStart = dateFormat.parse(eventStartDateTime); // Event start time
            Date eventEnd = dateFormat.parse(eventEndDateTime); // Event end time
            Date currentTime = new Date(); // Current system time
            // Check if the event has started
            // If the event is start or end, don't need count the quota
            if (!currentTime.after(eventStart)) {
                // [Gain Event Quota / Count Join number of people]
                databaseParticipantRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isJoined = false;
                        boolean isFull = false;
                        int participantCount = (int) snapshot.getChildrenCount(); // Count actual participants
                        int maxParticipants = event.getParticipantLimit(); // Retrieve max participants
                        int remainingQuota = maxParticipants - participantCount;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Users user = dataSnapshot.getValue(Users.class);
                            // Check isJoined
                            if (user.getUID().equals(auth.getUid())) {
                                isJoined = true;
                            }
                        }
                        // Update UI based on remaining quota
                        if (remainingQuota <= 0) {
                            holder.eventStatusAndQuota.setText("Remaining quota: Full");
                            holder.eventStatusAndQuota.setTextColor(ContextCompat.getColor(context, R.color.red_3));
                            // Check isFull
                            isFull = true;
                        } else {
                            holder.eventStatusAndQuota.setText("Remaining quota: " + remainingQuota);
                            holder.eventStatusAndQuota.setTextColor(ContextCompat.getColor(context, R.color.green_1));
                        }
                        // creator can't quit
                        if (!event.getUID().equals(auth.getUid())) {
                            // isFull and is participant
                            if (isFull && !isJoined) {
                                // isFull -> can't join and is participant
                                holder.btnJoin.setVisibility(View.GONE);
                            } else {
                                // is participant or isn't full
                                holder.btnJoin.setVisibility(View.VISIBLE);
                                if (isJoined) {
                                    holder.btnJoin.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red_3));
                                    holder.btnJoin.setText("CANCEL");
                                } else {
                                    holder.btnJoin.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.green_1));
                                    holder.btnJoin.setText("JOIN");
                                }
                            }
                        } else {
                            holder.btnJoin.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load participant data: " + error.getMessage());
                    }
                });

            } else {
                holder.btnJoin.setVisibility(View.GONE);
                holder.eventStatusAndQuota.setTextColor(ContextCompat.getColor(context, R.color.red_3));
                if (currentTime.after(eventEnd)) {
                    holder.eventStatusAndQuota.setText("The event has ended");
                } else if (currentTime.after(eventStart)) {
                    holder.eventStatusAndQuota.setText("The event has started");
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(TAG, "Error parsing event start date/time: " + e.getMessage());
        }
        // [Gain Event Creator Info]
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
                Log.w(TAG, "Personal information cannot be obtained: " + error.getMessage());
            }
        });
        // [Gain Event Info]
        holder.eventName.setText(event.getEventName());
        holder.location.setText(event.getLocation());
        holder.startDateTime.setText("Start: " + event.getStartDate() + ", " + event.getStartTime());
        holder.endDateTime.setText("End: " + event.getEndDate() + ", " + event.getEndTime());
        holder.description.setText(event.getDescription());
        holder.participantLimit.setText("Participants: " + event.getParticipantLimit());
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
        // Point to Point Google Map
        holder.btnPointToPoint.setOnClickListener(view -> {
            FindRoute findRoute = new FindRoute();
            // Pass data to the bottom sheet if needed, for example:
            Bundle bundle = new Bundle();
            bundle.putSerializable("destination", event.getLocation());
            findRoute.setArguments(bundle);
            // Use the activity's fragment manager to show the bottom sheet
            findRoute.show(activity.getSupportFragmentManager(), "bottom");
        });
        // handle find the location
        holder.btnLocation.setOnClickListener(view -> {
            try {
                String encodedLocation = URLEncoder.encode(event.getLocation().toString(), "UTF-8");
                Uri uri = Uri.parse("https://www.google.com.hk/maps/place/" + encodedLocation);
                // Uri uri = Uri.parse("https://www.google.com.hk/maps/place/" + your location + "/"+ encodedLocation);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent); // Use context to start the activity
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Toast.makeText(context, "Failed to encode location.", Toast.LENGTH_SHORT).show();
            }
        });
        // Open event on item click
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, EventActivity.class);
            intent.putExtra("event", event);  // Pass the event object
            context.startActivity(intent);
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
        ImageView btnPointToPoint, btnLocation, btnSave;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            username = itemView.findViewById(R.id.username);
            eventName = itemView.findViewById(R.id.eventName);
            startDateTime = itemView.findViewById(R.id.startDateTime);
            endDateTime = itemView.findViewById(R.id.endDateTime);
            location = itemView.findViewById(R.id.location);
            participantLimit = itemView.findViewById(R.id.participantLimit);
            description = itemView.findViewById(R.id.description);
            eventStatusAndQuota = itemView.findViewById(R.id.eventStatusAndQuota);
            btnPointToPoint = itemView.findViewById(R.id.btnPointToPoint);
            btnJoin = itemView.findViewById(R.id.btnJoin);
            btnLocation = itemView.findViewById(R.id.btnLocation);
            btnSave = itemView.findViewById(R.id.btnSave);
        }
    }
}
