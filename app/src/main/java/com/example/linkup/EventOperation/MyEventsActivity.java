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

import com.example.linkup.Adapter.ArticleAdapter;
import com.example.linkup.Adapter.EventAdapter;
import com.example.linkup.CommunityOperation.MyArticlesActivity;
import com.example.linkup.Object.Articles;
import com.example.linkup.Object.Events;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
// ✅
public class MyEventsActivity extends AppCompatActivity {
    // layout object
    ImageView btnBack;
    RecyclerView eventRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseEventRef; // real-time db ref ; SavedArticleSortByUser -> SASBU
    // convert event data into RecyclerView by Adapter
    ArrayList<Events> eventsArrayList = new ArrayList<>();
    EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseEventRef = Rdb.getReference().child("event");
        // [END config_firebase reference]

        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        eventRV = findViewById(R.id.eventRV);
        // [END gain]

        // [START config_layout]
        // Pass data into arrayList , then article adapter received
        databaseEventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventsArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Events event = dataSnapshot.getValue(Events.class);
                    // Ensure article is not null before proceeding
                    if (event != null && event.getUID().equals(auth.getUid())) {
                        eventsArrayList.add(event);
                    }
                }
                // Notify adapter after sorting
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyEventsActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Grant value - which view, posts array list
        eventAdapter = new EventAdapter(MyEventsActivity.this, eventsArrayList, MyEventsActivity.this);
        // Set up the layout manager, adapter
        eventRV.setLayoutManager(new LinearLayoutManager(MyEventsActivity.this));
        eventRV.setHasFixedSize(true);
        eventRV.setAdapter(eventAdapter);
        // Determine if there are any follow requests
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