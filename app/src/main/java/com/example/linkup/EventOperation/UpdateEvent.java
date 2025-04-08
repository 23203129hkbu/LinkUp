package com.example.linkup.EventOperation;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.linkup.Object.Events;
import com.example.linkup.Object.Users;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UpdateEvent extends AppCompatActivity {
    // layout object
    ImageView btnBack, btnLocation;
    EditText eventName, description, location, participant;
    TextView startDate, startTime, endDate, endTime, state, btnUpdate, error;
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
    Boolean isPublic = false;
    int currentParticipant;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_event);
        // [START gain value from other activity]
        event = (Events) getIntent().getSerializableExtra("event");
        // [END gain]

        // [START config_firebase]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        // [END config_firebase]

        // [START config_firebase reference]
        databaseEventRef = Rdb.getReference().child("event").child(event.getEventID());
        databaseParticipantRef = Rdb.getReference().child("eventParticipant").child(event.getEventID());
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
        btnUpdate = findViewById(R.id.btnUpdate);
        error = findViewById(R.id.error);
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
        // [Gain Event Info]
        databaseEventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    event = snapshot.getValue(Events.class);
                    // Layout Control
                    eventName.setText(event.getEventName());
                    description.setText(event.getDescription());
                    startDate.setText(event.getStartDate());
                    startTime.setText(event.getStartTime());
                    endDate.setText(event.getEndDate());
                    endTime.setText(event.getEndTime());
                    location.setText(event.getLocation());
                    participant.setText(String.valueOf(event.getParticipantLimit()));
                    description.setText(event.getDescription());
                    if (event.isPublic()){
                        switchState.setChecked(false);
                    }else {
                        switchState.setChecked(true);
                    }
                } else {
                    Toast.makeText(UpdateEvent.this, "event data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateEvent.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Event cannot be obtained: " + error.getMessage());
            }
        });
        // [Gain Event Info]
        databaseParticipantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentParticipant = (int) snapshot.getChildrenCount(); // Count actual participants
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load participant data: " + error.getMessage());
            }
        });
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

            datePickerDialog = new DatePickerDialog(UpdateEvent.this, (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                startDate.setText(selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay);
            }, year, month, day);
            datePickerDialog.show();
        });
        endDate.setOnClickListener(view -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            datePickerDialog = new DatePickerDialog(UpdateEvent.this, (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                endDate.setText(selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay);
            }, year, month, day);
            datePickerDialog.show();
        });
        // TimePickerDialog - start Time / end time
        startTime.setOnClickListener(view -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            timePickerDialog = new TimePickerDialog(UpdateEvent.this, (timePicker, selectedHour, selectedMinute) -> {
                startTime.setText(selectedHour + ":" + String.format("%02d", selectedMinute));
            }, hour, minute, true);
            timePickerDialog.show();
        });
        endTime.setOnClickListener(view -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            timePickerDialog = new TimePickerDialog(UpdateEvent.this, (timePicker, selectedHour, selectedMinute) -> {
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
                    // Uri uri = Uri.parse("https://www.google.com.hk/maps/place/" + your location + "/"+ encodedLocation);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setPackage("com.google.android.apps.maps");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Toast.makeText(UpdateEvent.this, "Failed to encode location.", Toast.LENGTH_SHORT).show();
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
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectEventDataAndSave();
            }
        });

        // [END layout component function]
    }
    // Collect event data, validate, and save to Firebase
    private void collectEventDataAndSave() {
        // Create Event Object
        event.setEventName(eventName.getText().toString().trim());
        event.setDescription(description.getText().toString().trim());
        event.setStartDate(startDate.getText().toString().trim());
        event.setStartTime(startTime.getText().toString().trim());
        event.setEndDate(endDate.getText().toString().trim());
        event.setEndTime(endTime.getText().toString().trim());
        event.setLocation(location.getText().toString().trim());
        event.setParticipantLimit(Integer.parseInt(participant.getText().toString().trim()));
        event.setPublic(isPublic);

        // Validate required fields
        if (TextUtils.isEmpty(event.getEventName())) {
            eventName.setError("Event name is required!");
            return;
        }
        if (TextUtils.isEmpty(event.getDescription())) {
            description.setError("Description is required!");
            return;
        }
        if (TextUtils.isEmpty(event.getLocation())) {
            location.setError("Location is required!");
            return;
        }
        if (TextUtils.isEmpty(event.getStartDate())) {
            startDate.setError("Start date is required!");

        }
        if (TextUtils.isEmpty(event.getStartTime())) {
            startTime.setError("Start time is required!");
            return;
        }
        if (TextUtils.isEmpty(event.getEndDate())) {
            endDate.setError("End date is required!");
            return;
        }
        if (TextUtils.isEmpty(event.getEndTime())) {
            endTime.setError("End time is required!");
            return;
        }

        // Prepare event participant count
        try {
            if (event.getParticipantLimit() <= 1) {
                participant.setError("Participants must be greater than 1!");
                return;
            } else if (event.getParticipantLimit()<currentParticipant) {
                participant.setError("The maximum number of participants you set is smaller than the current number of participants, you need to remove some of the participants from the participant management.");
                return;
            }
        } catch (NumberFormatException e) {
            participant.setError("Invalid number of participants!");
            return;
        }

        // Validate start date/time is at least one day later than the current date/time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
        try {
            String startDateTime = event.getStartDate() + " " + event.getStartTime();
            String endDateTime = event.getEndDate() + " " + event.getEndTime();

            // Parse start and end date/time
            java.util.Date startDateObj = sdf.parse(startDateTime);
            java.util.Date endDateObj = sdf.parse(endDateTime);

            // Calculate the minimum valid start date (current date + 1 day)
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 1);
            java.util.Date minStartDate = cal.getTime();

            // Check if the start date/time is earlier than the allowed minimum
            if (startDateObj != null && startDateObj.before(minStartDate)) {
                error.setVisibility(View.VISIBLE);
                error.setText("Start date/time must be at least one day later than today!");
                return;
            }

            // Check if the start date/time is after the end date/time
            if (startDateObj != null && endDateObj != null && startDateObj.after(endDateObj)) {
                error.setVisibility(View.VISIBLE);
                error.setText("Start date/time cannot be later than end date/time!");
                return;
            }
        } catch (Exception e) {
            error.setVisibility(View.VISIBLE);
            error.setText("Please check the Start Date/Time and End Date/Time are selected!");
            return;
        }
        // Show progress dialog
        progressDialog.show();

        // Save event to Firebase
        databaseEventRef.setValue(event)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(UpdateEvent.this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    error.setVisibility(View.VISIBLE);
                    error.setText("Failed to create event: " + e.getMessage());
                });
    }
}