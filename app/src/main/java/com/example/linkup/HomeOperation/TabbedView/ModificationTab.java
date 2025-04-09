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

import com.example.linkup.Adapter.InvitationNotificationAdapter;
import com.example.linkup.Adapter.ModificationAdapter;
import com.example.linkup.Object.Events;
import com.example.linkup.Object.Modifications;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ModificationTab extends Fragment {
    View view;
    // layout object
    RecyclerView modificationRV;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseModificationRef; // real-time db ref
    // convert invitation data into RecyclerView by Adapter
    ArrayList<Modifications> modificationsArrayList = new ArrayList<>();
    ModificationAdapter modificationAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.activity_modification_tab, container, false);

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseModificationRef = Rdb.getReference().child("modification").child(auth.getUid());
        // [END config_firebase reference]

        // [START gain layout objects]
        modificationRV = view.findViewById(R.id.modificationRV);
        // [END gain]


        // Load users from Firebase
        databaseModificationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                modificationsArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Modifications modification = dataSnapshot.getValue(Modifications.class);
                    if (modification != null) {
                        modificationsArrayList.add(modification);
                        modification.setRead(true);
                        databaseModificationRef.child(modification.getEventId()).setValue(modification);
                    }
                }
                modificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize Adapter with an empty list
        modificationAdapter = new ModificationAdapter(getContext(), modificationsArrayList); // Only filteredUsers are shown
        modificationRV.setLayoutManager(new LinearLayoutManager(getContext()));
        modificationRV.setHasFixedSize(true);
        modificationRV.setAdapter(modificationAdapter);
        // this line must be finalized
        return view;
    }
}