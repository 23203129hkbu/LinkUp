package com.example.linkup.ProfileOperation;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.linkup.Object.Users;
import com.example.linkup.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.squareup.picasso.Picasso;

// ✅
public class PrivacyActivity extends AppCompatActivity {
    // layout object
    ImageView btnBack;
    Switch switchState;
    TextView state;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef; // real-time db ref
    // default user info
    Users user = new Users();
    Boolean cancel = false;
    String userState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user").child(auth.getUid());
        // [END config_firebase reference]

        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        switchState = findViewById(R.id.switchState);
        state = findViewById(R.id.state);
        // [END gain]

        // [START config_layout]
        // [Gain User Data]
        databaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Layout Control
                    user = snapshot.getValue(Users.class);
                    state.setText(user.getPrivacy());
                    // State initialization
                    if (user.getPrivacy().equals("Private")) {
                        switchState.setChecked(true);
                        userState = "Public";
                    }else{
                        switchState.setChecked(false);
                        userState = "Private";
                    }

                } else {
                    Toast.makeText(PrivacyActivity.this, "Modification failed", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PrivacyActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Personal data cannot be obtained: "+error.getMessage());
            }
        });
        // [END config_layout]

        // [START layout component function]
        // Switch the screen - SettingActivity
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI();
            }
        });
        // Switch user privacy setting
        switchState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // Flag to differentiate user actions from programmatic changes
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cancel) {
                    cancel = false; // Reset the flag
                    return; // Exit early to avoid showing the dialog
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(PrivacyActivity.this);
                builder.setTitle("Confirmation Notification")
                        .setMessage("Toggle account privacy status?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                savePrivacySetting(); // Save the new privacy setting
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cancel = true; // Set the flag to prevent triggering listener
                                switchState.setChecked(user.getPrivacy().equals("Private"));
                            }
                        });
                // Show the dialog
                builder.create().show();
            }
        });
        // [END layout component function]
    }
    // [START Method]
    // handling UI update
    private void updateUI() {
        Intent intent = new Intent(PrivacyActivity.this, SettingActivity.class);
        startActivity(intent);
        finish();
    }
    // handling privacy setting update
    private void savePrivacySetting() {
        user.setPrivacy(userState);
        databaseUserRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(PrivacyActivity.this, "Privacy setting saved successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PrivacyActivity.this, "Failed to save.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PrivacyActivity.this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // [END Method]
}