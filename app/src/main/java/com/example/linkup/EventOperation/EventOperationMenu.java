package com.example.linkup.EventOperation;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.linkup.Object.Events;
import com.example.linkup.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EventOperationMenu extends BottomSheetDialogFragment {
    // layout object
    CardView btnUpdate, btnCancel, btnManagement, btnInvite, btnGenQRCode;
    Events event;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.event_operation_menu,null);
        // Retrieve the event object
        event = (Events) getArguments().getSerializable("event");

        // [START gain layout objects]
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnManagement = view.findViewById(R.id.btnManagement);
        btnInvite = view.findViewById(R.id.btnInvite);
        btnGenQRCode = view.findViewById(R.id. btnGenQRCode);

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
            public void onClick(View view) {

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
                updateUI("joinedEvents");
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
        } else if (screen.equals("savedEvents")) {
            intent = new Intent(getContext(), SavedEventsActivity.class);
        }else if (screen.equals("joinedEvents")) {
            intent = new Intent(getContext(), JoinedEventsActivity.class);
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
    // [END Method]
}
