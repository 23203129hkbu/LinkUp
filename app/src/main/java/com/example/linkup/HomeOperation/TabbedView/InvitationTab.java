package com.example.linkup.HomeOperation.TabbedView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.example.linkup.Adapter.FollowRequestAdapter;
import com.example.linkup.Adapter.InvitationAdapter;
import com.example.linkup.Adapter.InvitationNotificationAdapter;
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

public class InvitationTab extends Fragment {
    View view;
    // layout object
    RecyclerView invitationRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseInvitationRef; // real-time db ref
    // convert invitation data into RecyclerView by Adapter
    ArrayList<Events> eventsArrayList = new ArrayList<>();
    InvitationNotificationAdapter invitationNotificationAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_invitation_tab, container, false);

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseInvitationRef = Rdb.getReference().child("invitation").child(auth.getUid());
        // [END config_firebase reference]

        // [START gain layout objects]
        invitationRV = view.findViewById(R.id.invitationRV);
        // [END gain]


        // Load users from Firebase
        databaseInvitationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                eventsArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Events event = dataSnapshot.getValue(Events.class);
                    if (event != null) {
                        eventsArrayList.add(event);
                    }
                }
                invitationNotificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize Adapter with an empty list
        invitationNotificationAdapter = new InvitationNotificationAdapter(getContext(),eventsArrayList); // Only filteredUsers are shown
        invitationRV.setLayoutManager(new LinearLayoutManager(getContext()));
        invitationRV.setHasFixedSize(true);
        invitationRV.setAdapter(invitationNotificationAdapter);
        // this line must be finalized
        return view;
    }
}