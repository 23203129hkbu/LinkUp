package com.example.linkup.EventOperation;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Adapter.EventAdapter;
import com.example.linkup.Object.Events;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;

public class JoinedEventsActivity extends AppCompatActivity {
    // layout object
    ImageView btnBack;
    RecyclerView eventRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseEventRef, databaseParticipantRef; // real-time db ref ; SavedArticleSortByUser -> SASBU
    // convert event data into RecyclerView by Adapter
    ArrayList<Events> eventsArrayList = new ArrayList<>();
    EventAdapter eventAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joined_events);

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseEventRef = Rdb.getReference().child("event");
        databaseParticipantRef = Rdb.getReference().child("eventParticipant");
        // [END config_firebase reference]

        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        eventRV = findViewById(R.id.eventRV);
        // [END gain]

        // [START config_layout]
        // My joined events show all
        // Query the eventParticipant node for current user's joined events
        databaseParticipantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashSet<String> joinedEventIds = new HashSet<>();

                // Find events where the current user is a participant
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    if (eventSnapshot.hasChild(auth.getUid())) {
                        joinedEventIds.add(eventSnapshot.getKey());
                    }
                }

                // Retrieve event details for joined events
                fetchEvents(joinedEventIds,auth.getUid());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(JoinedEventsActivity.this, "Failed to load joined events: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchEvents(HashSet<String> joinedEventIds, String currentUserId) {
        databaseEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventsArrayList.clear();

                // Match the event IDs with the joined events and exclude self-created activities
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    Events event = eventSnapshot.getValue(Events.class);
                    if (event != null && joinedEventIds.contains(event.getEventID()) && !event.getUID().equals(currentUserId)) {
                        eventsArrayList.add(event);
                    }
                }

                // Notify adapter of data changes
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(JoinedEventsActivity.this, "Failed to load events: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Grant value - which view, posts array list
        eventAdapter = new EventAdapter(JoinedEventsActivity.this, eventsArrayList);
        // Set up the layout manager, adapter
        eventRV.setLayoutManager(new LinearLayoutManager(JoinedEventsActivity.this));
        eventRV.setHasFixedSize(true);
        eventRV.setAdapter(eventAdapter);
        // [END config_layout]

        // [START layout component function]
        // Switch the screen - Main Screen
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // [END layout component function]
    }
}