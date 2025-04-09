package com.example.linkup.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Adapter.EventAdapter;
import com.example.linkup.Adapter.PostAdapter;
import com.example.linkup.CommunityOperation.CommunityMenu;
import com.example.linkup.EventOperation.CreateEvent;
import com.example.linkup.EventOperation.EventMenu;
import com.example.linkup.EventOperation.SearchEvent;
import com.example.linkup.HomeOperation.CreatePost;
import com.example.linkup.HomeOperation.SearchUser;
import com.example.linkup.Object.Articles;
import com.example.linkup.Object.Events;
import com.example.linkup.Object.Posts;
import com.example.linkup.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EventFragment extends Fragment {
    View view;
    // layout object
    ImageView btnAdd, btnSearch, btnMenu;
    RecyclerView eventRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseEventRef; // real-time db ref
    // convert post data into RecyclerView by Adapter
    ArrayList<Events> eventsArrayList = new ArrayList<>();
    EventAdapter eventAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_event_fragment, container, false);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseEventRef = Rdb.getReference().child("event");
        // [END config_firebase reference]

        // [START gain layout objects]
        btnMenu = view.findViewById(R.id.btnMenu);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnAdd = view.findViewById(R.id.btnAdd);
        eventRV = view.findViewById(R.id.eventRV);
        // [END gain]

        databaseEventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventsArrayList.clear();
                // // Get the current date and time
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm", Locale.getDefault());
                String currentDateTimeString = sdf.format(new Date());

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Events event = dataSnapshot.getValue(Events.class);
                    // filter all private and expired
                    if (event != null && event.isPublic()) {
                        String startDateTime = event.getStartDate() + ", " + event.getStartTime();

                        try {
                            // Convert the string to a date object
                            Date eventStartDate = sdf.parse(startDateTime);
                            Date currentDate = sdf.parse(currentDateTimeString);

                            if (eventStartDate != null && currentDate != null && eventStartDate.after(currentDate)) {
                                // If the activity has not started yet, add it to the list
                                eventsArrayList.add(event);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error parsing date: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                eventAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load articles: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Grant value - which view, posts array list
        eventAdapter = new EventAdapter(getContext(), eventsArrayList, (AppCompatActivity) getActivity());
        // Set up the layout manager, adapter
        eventRV.setLayoutManager(new LinearLayoutManager(getContext()));
        eventRV.setHasFixedSize(true);
        eventRV.setAdapter(eventAdapter);
        // Determine if there are any follow requests

        // [START layout component function]
        // Switch the screen - Create Community Post
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI("Create");
            }
        });
        // Switch the screen - Search User
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI("Search");
            }
        });
        // Switch the screen - Community Menu
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventMenu em = new EventMenu();
                em.show(getParentFragmentManager(), "bottom");
            }
        });
        // [END layout component function]
        // this line must be finalized
        return view;
    }

    // [START Method]
    // handling UI update
    private void updateUI(String screen) {
        Intent intent = null;
        if (screen.equals("Create")) {
            intent = new Intent(getContext(), CreateEvent.class);
        } else if (screen.equals("Search")) {
            intent = new Intent(getContext(), SearchEvent.class);
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
    // [END Method]
}
