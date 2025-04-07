package com.example.linkup.EventOperation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.linkup.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EventItemMenu extends BottomSheetDialogFragment {
    // layout object
    CardView btnMyEvents, btnSavedEvents, btnJoinedEvents ;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.event_bottom_menu,null);
        // [START gain layout objects]
        btnMyEvents = view.findViewById(R.id.btnMyEvents);
        btnSavedEvents = view.findViewById(R.id.btnSavedEvents);
        btnJoinedEvents = view.findViewById(R.id.btnJoinedEvents);
        // [END gain]

        // [START layout component function]
        // Switch the screen - User's articles
        btnMyEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUI("MyCreated");
            }
        });
        // Switch the screen - saved articles
        btnSavedEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUI("savedEvents");
            }
        });
        // Switch the screen - saved articles
        btnJoinedEvents.setOnClickListener(new View.OnClickListener() {
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
        if (screen.equals("MyCreated")) {
            intent = new Intent(getContext(), MyEventsActivity.class);
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

