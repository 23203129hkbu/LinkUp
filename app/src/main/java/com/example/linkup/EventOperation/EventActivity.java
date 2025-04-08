package com.example.linkup.EventOperation;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.linkup.CommunityOperation.ArticleActivity;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventActivity extends AppCompatActivity {
    CircleImageView avatar;
    TextView username, eventName, startDateTime, endDateTime, location, participantLimit, description, eventStatusAndQuota, btnJoin;
    ImageView btnLocation, btnMenu, btnBack;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef, databaseEventRef, databaseParticipantRef, databaseIsJoinedRef; // real-time db ref
    // Event Info
    Events event = new Events();
    Boolean isStart = false;
    Boolean isEnd = false;
    Boolean isJoined = false;
    Boolean isFull = false;
    int maxParticipants = 0;
    int participantCount = 0;
    //
    SimpleDateFormat newDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        // [START gain value from other activity]
        event = (Events) getIntent().getSerializableExtra("event");
        // [END gain]

        // [START config_firebase]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        // [END config_firebase]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user").child(event.getUID());
        databaseEventRef = Rdb.getReference().child("event").child(event.getEventID());
        databaseParticipantRef = Rdb.getReference().child("eventParticipant").child(event.getEventID());
        // join / cancel
        databaseIsJoinedRef = Rdb.getReference().child("eventParticipant").child(event.getEventID()).child(auth.getUid());
        // [END config_firebase reference]

        // [START gain layout objects]
        avatar = findViewById(R.id.avatar);
        username = findViewById(R.id.username);
        eventName = findViewById(R.id.eventName);
        startDateTime = findViewById(R.id.startDateTime);
        endDateTime = findViewById(R.id.endDateTime);
        location = findViewById(R.id.location);
        participantLimit = findViewById(R.id.participantLimit);
        description = findViewById(R.id.description);
        eventStatusAndQuota = findViewById(R.id.eventStatusAndQuota);
        btnJoin = findViewById(R.id.btnJoin);
        btnLocation = findViewById(R.id.btnLocation);
        btnMenu = findViewById(R.id.btnMenu);
        btnBack = findViewById(R.id.btnBack);
        // [END gain]

        // [START config_layout]
        // [Gain Post Creator Info]
        databaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Layout Control
                    Picasso.get().load(snapshot.child("avatarURL").getValue(String.class)).into(avatar);
                    username.setText(snapshot.child("username").getValue(String.class));
                } else {
                    Toast.makeText(EventActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EventActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Personal information cannot be obtained: " + error.getMessage());
            }
        });
        // [Gain Event Info]
        databaseEventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    event = snapshot.getValue(Events.class);
                    String eventStartDateTime = event.getStartDate() + " " + event.getStartTime(); // Combine start date and time
                    String eventEndDateTime = event.getEndDate() + " " + event.getEndTime(); // Combine end date and time
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // Adjust format as needed
                    try {
                        Date eventStart = dateFormat.parse(eventStartDateTime); // Event start time
                        Date eventEnd = dateFormat.parse(eventEndDateTime); // Event end time
                        Date currentTime = new Date(); // Current system time
                        if (eventStart != null && currentTime.after(eventStart)) {
                            isStart = true;
                            if (eventEnd != null && currentTime.after(eventEnd)) {
                                isEnd = true;
                            } else {
                                isEnd = false;
                            }
                        } else {
                            isStart = false;
                            isEnd = false;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error parsing event start date/time: " + e.getMessage());
                    }
                    maxParticipants = event.getParticipantLimit();
                    // Layout Control
                    eventName.setText(event.getEventName());
                    location.setText(event.getLocation());
                    startDateTime.setText("Start: " + event.getStartDate() + ", " + event.getStartTime());
                    endDateTime.setText("End: " + event.getEndDate() + ", " + event.getEndTime());
                    description.setText("Description: " + event.getDescription());
                    participantLimit.setText("Participants: " + event.getParticipantLimit());
                    loadEvent();
                } else {
                    Toast.makeText(EventActivity.this, "event data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EventActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Event cannot be obtained: " + error.getMessage());
            }
        });
        // [Count Join number of people]
        databaseParticipantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                participantCount = (int) snapshot.getChildrenCount(); // Count actual participants
                loadEvent();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load participant data: " + error.getMessage());
            }
        });
        // Check IsJoin
        databaseIsJoinedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isJoined = true;
                } else {
                    isJoined = false;
                }
                loadEvent();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EventActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Personal information cannot be obtained: " + error.getMessage());
            }
        });
        // [END config_layout]

        // [START layout component function]
        // Switch the screen -  Community Fragment
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Join the event
        btnJoin.setOnClickListener(view -> {
            databaseIsJoinedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Cancel
                        databaseIsJoinedRef.removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(EventActivity.this, "Event cancel", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EventActivity.this, "Failed to unsaved event", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Join
                        Users storedUser = new Users();
                        storedUser.setUID(auth.getUid());
                        databaseIsJoinedRef.setValue(storedUser).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(EventActivity.this, "Event joined", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EventActivity.this, "Failed to save event", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Save/unsave operation failed: " + error.getMessage());
                }
            });
        });
        // handle find the location
        btnLocation.setOnClickListener(view -> {
            try {
                String encodedLocation = URLEncoder.encode(event.getLocation().toString(), "UTF-8");
                Uri uri = Uri.parse("https://www.google.com.hk/maps/place/" + encodedLocation);
                // Uri uri = Uri.parse("https://www.google.com.hk/maps/place/" + your location + "/"+ encodedLocation);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent); // Use context to start the activity
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Toast.makeText(EventActivity.this, "Failed to encode location.", Toast.LENGTH_SHORT).show();
            }
        });
        // Open Menu to manage event
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventOperationMenu eom = new EventOperationMenu();

                // Pass the `event` object to the bottom sheet
                Bundle bundle = new Bundle();
                bundle.putSerializable("event", event);
                eom.setArguments(bundle);

                // Show the bottom sheet
                eom.show(getSupportFragmentManager(), "eventMenu");
            }
        });
        // [END layout component function]
    }

    // [START Method]
    private void loadEvent() {
        eventStatusAndQuota.setTextColor(ContextCompat.getColor(EventActivity.this, R.color.red_3));
        int remainingQuota = maxParticipants - participantCount;
        if (remainingQuota <= 0) {
            isFull = true;
        } else {
            isFull = false;
        }
        // Only creator can manage the event
        if (!event.getUID().equals(auth.getUid())||isStart||isEnd) {
            // Hide save join button if the user is the creator
            btnMenu.setVisibility(View.GONE);
        }
        // Control Quota message
        if (isEnd) {
            eventStatusAndQuota.setText("The event has ended");
        } else if (isStart) {
            eventStatusAndQuota.setText("The event has started");
        } else if (isFull) {
            eventStatusAndQuota.setText("Remaining quota: Full");
        } else {
            eventStatusAndQuota.setText("Remaining quota: " + remainingQuota);
            eventStatusAndQuota.setTextColor(ContextCompat.getColor(EventActivity.this, R.color.green_1));
        }
        // Control Button Follow
        if (isStart || isEnd || event.getUID().equals(auth.getUid())) {
            btnJoin.setVisibility(View.GONE);
        } else {
            if (isFull && !isJoined) {
                btnJoin.setVisibility(View.GONE);
            } else {
                btnJoin.setVisibility(View.VISIBLE);
                if (isJoined) {
                    btnJoin.setBackgroundTintList(ContextCompat.getColorStateList(EventActivity.this, R.color.red_3));
                    btnJoin.setText("CANCEL");
                } else {
                    btnJoin.setBackgroundTintList(ContextCompat.getColorStateList(EventActivity.this, R.color.green_1));
                    btnJoin.setText("JOIN");
                }
            }
        }
    }
    // [END Method]
}


