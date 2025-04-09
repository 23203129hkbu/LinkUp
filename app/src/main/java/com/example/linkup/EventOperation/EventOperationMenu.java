package com.example.linkup.EventOperation;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.example.linkup.Object.Events;
import com.example.linkup.Object.Modifications;
import com.example.linkup.Object.Users;
import com.example.linkup.Process.MainActivity;
import com.example.linkup.ProfileOperation.SettingActivity;
import com.example.linkup.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EventOperationMenu extends BottomSheetDialogFragment {
    // layout object
    CardView btnUpdate, btnCancel, btnManagement, btnInvite, btnGenQRCode;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseEventRef, databaseParticipantRef, databaseModificationRef; // real-time db ref
    // Event info
    Events event = new Events();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.event_operation_menu, null);
        // Retrieve the event object
        event = (Events) getArguments().getSerializable("event");

        // [START config_firebase]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        // [END config_firebase]

        // [START config_firebase reference]
        databaseEventRef = Rdb.getReference().child("event").child(event.getEventID());
        databaseParticipantRef = Rdb.getReference().child("eventParticipant").child(event.getEventID());
        databaseModificationRef = Rdb.getReference().child("modification");
        // [END config_firebase reference]

        // [START gain layout objects]
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnManagement = view.findViewById(R.id.btnManagement);
        btnInvite = view.findViewById(R.id.btnInvite);
        btnGenQRCode = view.findViewById(R.id.btnGenQRCode);
        // [END gain]

        // [START layout component function]
        // Switch the screen - User's articles
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUI("Update");
            }
        });
        // Switch the screen - saved articles
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Confirmation Notification")
                        .setMessage("Cancel Event" + event.getEventName() + "?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                builder.setTitle("Warning")
                                        .setMessage("It will be cancel and cannot be recovered.\n\nAre you sure you still want to perform this operation?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                databaseEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            // Article is already saved, remove it
                                                            databaseEventRef.removeValue().addOnCompleteListener(task -> {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(getContext(), "Event will be cancel", Toast.LENGTH_SHORT).show();
                                                                    databaseParticipantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                                                Users user = dataSnapshot.getValue(Users.class);
                                                                                if (user != null) {
                                                                                    Modifications modification = new Modifications();
                                                                                    modification.setEventId(event.getEventID());
                                                                                    modification.setContent("Please note that Event: "+ event.getEventName()+" has been cancel");
                                                                                    modification.setRead(false);
                                                                                    databaseModificationRef.child(user.getUID()).child(event.getEventID()).setValue(modification);
                                                                                }
                                                                            }
                                                                            databaseParticipantRef.removeValue();
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                                            Toast.makeText(getContext(), "Failed to reload : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                    updateUI("Event");
                                                                }
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                // this line to show the dialog
                                builder.create().show();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                // this line to show the dialog
                builder.create().show();
            }
        });
        // Switch the screen - saved articles
        btnManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUI("Management");
            }
        });
        btnInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUI("Invitation");
            }
        });
        btnGenQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        if (screen.equals("Update")) {
            intent = new Intent(getContext(), UpdateEvent.class);
            intent.putExtra("event", event);  // Pass the event object
        } else if (screen.equals("Event")) {
            intent = new Intent(getContext(), MainActivity.class);
        } else if (screen.equals("Management")) {
            intent = new Intent(getContext(), ParticipantManagementActivity.class);
            intent.putExtra("event", event);  // Pass the event object
        }else if (screen.equals("Invitation")) {
            intent = new Intent(getContext(), InvitationActivity.class);
            intent.putExtra("event", event);  // Pass the event object
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
    // [END Method]
}
