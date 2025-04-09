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

import com.example.linkup.EventOperation.EventActivity;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class InvitationNotificationAdapter extends RecyclerView.Adapter<InvitationNotificationAdapter.ViewHolder> {
    // layout object
    Context context;
    ArrayList<Events> eventsArrayList = new ArrayList<>();
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db

    // Constructor
    public InvitationNotificationAdapter(Context context, ArrayList<Events> eventsArrayList) {
        this.context = context;
        this.eventsArrayList = eventsArrayList;

        // [START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        // [END configuration]
    }
    @NonNull
    @Override
    public InvitationNotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.invitation_notification_item, parent, false);

        return new InvitationNotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvitationNotificationAdapter.ViewHolder holder, int position) {
        Events event = eventsArrayList.get(position);

        // [START config_firebase reference]
        DatabaseReference databaseEventRef,databaseInvitationRef, databaseIsJoinedRef;// real-time db ref
        databaseEventRef = Rdb.getReference().child("event").child(event.getEventID());
        databaseIsJoinedRef = Rdb.getReference().child("eventParticipant").child(event.getEventID()).child(auth.getUid());
        databaseInvitationRef = Rdb.getReference().child("invitation").child(auth.getUid()).child(event.getEventID());
        // [END config_firebase reference]

        // [START config_layout]
        databaseEventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Extract startDate and startTime from the event
                    String startDate = snapshot.child("startDate").getValue(String.class);
                    String startTime = snapshot.child("startTime").getValue(String.class);

                    if (startDate != null && startTime != null) {
                        try {
                            // Combine startDate and startTime into a Date object
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            Date eventStartDate = sdf.parse(startDate + " " + startTime);

                            // Get the current date and time
                            Date currentDate = new Date();

                            // Check if the event has started
                            if (eventStartDate != null && eventStartDate.before(currentDate)) {
                                // Event has started, remove the invitation
                                Toast.makeText(context, "Event has already started. Invitation removed.", Toast.LENGTH_SHORT).show();
                                databaseInvitationRef.removeValue();
                                return; // Exit early to avoid further processing
                            } else {
                                holder.eventName.setText("Event: "+snapshot.child("eventName").getValue(String.class));
                            }
                        } catch (ParseException e) {
                            Log.e(TAG, "Date parsing failed: " + e.getMessage());
                        }
                    } else {
                        Toast.makeText(context, "Invalid event start time or date.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Event is no longer valid.", Toast.LENGTH_SHORT).show();
                    databaseInvitationRef.removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Event cannot be obtained: " + error.getMessage());
            }
        });
        databaseIsJoinedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(context, "Event is joined.", Toast.LENGTH_SHORT).show();
                    databaseInvitationRef.removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Event cannot be obtained: " + error.getMessage());
            }
        });
        // [END config_layout]

        // [START layout component function]
        // reject
        holder.btnReject.setOnClickListener(view -> {
            databaseInvitationRef.removeValue();
        });
        // accept
        holder.btnAccept.setOnClickListener(view -> {
            databaseInvitationRef.removeValue();
            Users storedUser = new Users();
            storedUser.setUID(auth.getUid());
            databaseIsJoinedRef.setValue(storedUser).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Event joined", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to save event", Toast.LENGTH_SHORT).show();
                }
            });
        });
        // Open article details on item click
        holder.itemView.setOnClickListener(view -> {
            databaseEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Events event = snapshot.getValue(Events.class);
                        Intent intent = new Intent(context, EventActivity.class);
                        intent.putExtra("event", event);  // Pass the event object
                        context.startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Event cannot be obtained: " + error.getMessage());
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
        TextView eventName, btnReject, btnAccept;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.eventName);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnAccept = itemView.findViewById(R.id.btnAccept);
        }
    }

}
