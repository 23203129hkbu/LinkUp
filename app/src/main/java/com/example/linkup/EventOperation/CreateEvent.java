package com.example.linkup.EventOperation;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.metrics.Event;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.linkup.HomeOperation.CreatePost;
import com.example.linkup.Object.Events;
import com.example.linkup.Object.Posts;
import com.example.linkup.Object.Users;
import com.example.linkup.ProfileOperation.PrivacyActivity;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateEvent extends AppCompatActivity {
    // layout object
    ImageView btnBack, btnLocation;
    EditText eventName, description, location, participant;
    TextView startDate, startTime, endDate, endTime, state, btnCreate;
    Switch switchState;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseEventRef, databaseParticipantRef; // real-time db ref
    // Dialog
    ProgressDialog progressDialog;
    // Calendar & DateFormat
    Calendar calendar;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    // Event Info
    Events event = new Events();
    String eventId;
    String name, eventDescription, eventLocation, eventParticipant, eventStartDate, eventStartTime, eventEndDate, eventEndTime;
    Boolean isPublic = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // [START config_firebase]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        // [END config_firebase]

        // [START config_firebase reference]
        databaseEventRef = Rdb.getReference().child("event");
        databaseParticipantRef = Rdb.getReference().child("eventParticipant");
        // [END config_firebase reference]

        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        btnLocation = findViewById(R.id.btnLocation);
        eventName = findViewById(R.id.eventName);
        description = findViewById(R.id.description);
        location = findViewById(R.id.location);
        participant = findViewById(R.id.participant);
        startDate = findViewById(R.id.startDate);
        startTime = findViewById(R.id.startTime);
        endDate = findViewById(R.id.endDate);
        endTime = findViewById(R.id.endTime);
        switchState = findViewById(R.id.switchState);
        state = findViewById(R.id.state);
        btnCreate = findViewById(R.id.btnCreate);
        // [END gain]

        // [START Calender / Date Format configuration]
        calendar = Calendar.getInstance();
        // [END Calender / Date Format configuration]

        // [START config_dialog]
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);
        // [END config_dialog]

        // [START config_layout]
        // [START config_layout]
        // Monitor changes in the location EditText
        location.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Update the location button icon based on whether there is input
                if (s.toString().isEmpty()) {
                    btnLocation.setImageResource(R.drawable.baseline_location_on_24_black);
                } else {
                    btnLocation.setImageResource(R.drawable.baseline_location_on_24_red);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed after text changes
            }
        });
        // [END config_layout]


        // [START layout component function]
        // Switch the screen - Profile Fragment
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // DatePickerDialog - start Date / end Date
        startDate.setOnClickListener(view -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            datePickerDialog = new DatePickerDialog(this, (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                startDate.setText(selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay);
            }, year, month, day);
            datePickerDialog.show();
        });
        endDate.setOnClickListener(view -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            datePickerDialog = new DatePickerDialog(this, (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                endDate.setText(selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay);
            }, year, month, day);
            datePickerDialog.show();
        });
        // TimePickerDialog - start Time / end time
        startTime.setOnClickListener(view -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            timePickerDialog = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
                startTime.setText(selectedHour + ":" + String.format("%02d", selectedMinute));
            }, hour, minute, true);
            timePickerDialog.show();
        });
        endTime.setOnClickListener(view -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            timePickerDialog = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
                endTime.setText(selectedHour + ":" + String.format("%02d", selectedMinute));
            }, hour, minute, true);
            timePickerDialog.show();
        });
        // google url view
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationText = location.getText().toString().trim();
                try {
                    String encodedLocation = URLEncoder.encode(locationText, "UTF-8");
                    Uri uri = Uri.parse("https://www.google.com.hk/maps/place/" + encodedLocation);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setPackage("com.google.android.apps.maps");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Toast.makeText(CreateEvent.this, "Failed to encode location.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Switch event state
        switchState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // Flag to differentiate user actions from programmatic changes
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    state.setText("Private");
                    isPublic = false;
                } else {
                    state.setText("Public");
                    isPublic = true;
                }
            }
        });
        // Create Event
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectEventDataAndSave();
            }
        });

        // [END layout component function]
    }

    // Collect event data, validate, and save to Firebase
    private void collectEventDataAndSave() {
        // Collect data from form fields
        name = eventName.getText().toString().trim();
        eventDescription = description.getText().toString().trim();
        eventLocation = location.getText().toString().trim();
        eventParticipant = participant.getText().toString().trim();
        eventStartDate = startDate.getText().toString().trim();
        eventStartTime = startTime.getText().toString().trim();
        eventEndDate = endDate.getText().toString().trim();
        eventEndTime = endTime.getText().toString().trim();

        // Validate required fields
        if (TextUtils.isEmpty(name)) {
            eventName.setError("Event name is required!");
            return;
        }
        if (TextUtils.isEmpty(eventDescription)) {
            description.setError("Description is required!");
            return;
        }
        if (TextUtils.isEmpty(eventLocation)) {
            location.setError("Location is required!");
            return;
        }
        if (TextUtils.isEmpty(eventStartDate)) {
            Toast.makeText(this, "Start date is required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(eventStartTime)) {
            Toast.makeText(this, "Start time is required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(eventEndDate)) {
            Toast.makeText(this, "End date is required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(eventEndTime)) {
            Toast.makeText(this, "End time is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare event participant count
        int maxParticipants;
        try {
            maxParticipants = Integer.parseInt(eventParticipant);
            if (maxParticipants <= 2) {
                participant.setError("Participants must be greater than 2!");
                return;
            }
        } catch (NumberFormatException e) {
            participant.setError("Invalid number of participants!");
            return;
        }


        // Create Event Object
        eventId = databaseEventRef.push().getKey(); // Generate unique ID for the event
        event.setEventID(eventId);
        event.setUID(auth.getUid());
        event.setEventName(name);
        event.setDescription(eventDescription);
        event.setStartDate(eventStartDate);
        event.setStartTime(eventStartTime);
        event.setEndDate(eventEndDate);
        event.setEndTime(eventEndTime);
        event.setLocation(eventLocation);
        event.setParticipantLimit(maxParticipants);
        event.setPublic(isPublic);

        // Show progress dialog
        progressDialog.show();

        // Save event to Firebase
        databaseEventRef.child(eventId).setValue(event)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Users storedUser = new Users();
                    storedUser.setUID(auth.getUid());
                    databaseParticipantRef.child(eventId).child(auth.getUid()).setValue(storedUser);
                    Toast.makeText(CreateEvent.this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity after successful creation
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(CreateEvent.this, "Failed to create event: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}