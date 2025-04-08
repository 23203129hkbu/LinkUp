package com.example.linkup.EventOperation;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Adapter.EventAdapter;
import com.example.linkup.Adapter.UserAdapter;
import com.example.linkup.HomeOperation.SearchUser;
import com.example.linkup.Object.Events;
import com.example.linkup.Object.Users;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchEvent extends AppCompatActivity {
    // Layout objects
    ImageView btnBack;
    SearchView searchBar;
    RecyclerView eventRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb;
    DatabaseReference databaseEventRef;
    // convert event data into RecyclerView by Adapter
    ArrayList<Events> eventsArrayList = new ArrayList<>();
    ArrayList<Events> filteredEvents = new ArrayList<>();
    EventAdapter eventAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_event);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseEventRef = Rdb.getReference().child("event");
        // [END config_firebase reference]

        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        searchBar = findViewById(R.id.searchBar);
        eventRV = findViewById(R.id.eventRV);
        // [END gain]

        // [START config_layout]
        // Load users from Firebase (but do not display them initially)
        databaseEventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                eventsArrayList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Events event = dataSnapshot.getValue(Events.class);
                    if (event != null && !event.getUID().equals(auth.getUid())) {
                        eventsArrayList.add(event);
                    }
                }
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SearchEvent.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Initialize Adapter with an empty list
        eventAdapter = new EventAdapter(SearchEvent.this, filteredEvents,SearchEvent.this); // Only filteredUsers are shown
        eventRV.setLayoutManager(new LinearLayoutManager(this));
        eventRV.setHasFixedSize(true);
        eventRV.setAdapter(eventAdapter);
        // [END config_layout]

        // [START layout component function]
        // Back button action
        btnBack.setOnClickListener(v -> finish());
        // Search functionality
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });
        // [END layout component function]
    }

    // [START Method]
    // Filter events based on search input
    // essential for case-insensitive search
    private void filterUsers(String text) {
        filteredEvents.clear();
        if (!text.isEmpty()) {
            for (Events event : eventsArrayList) {
                // Search bu event name OR Description
                if (event.getEventName().toLowerCase().contains(text.toLowerCase())||event.getDescription().toLowerCase().contains(text.toLowerCase())) {
                    filteredEvents.add(event);
                }
            }
        }

        eventAdapter.notifyDataSetChanged(); // Refresh the adapter with the filtered list
    }
    // [END Method]
}