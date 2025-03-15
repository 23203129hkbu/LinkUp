package com.example.linkup;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import com.example.linkup.Object.Users;
import com.example.linkup.ProfileOperation.UpdateProfile;
import com.example.linkup.SocialLogin.FacebookSignInActivity;
import com.example.linkup.SocialLogin.GoogleSignInActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class PrivacyActivity extends AppCompatActivity {
    // layout object
    ImageView btnBack;
    Switch switchStatus;
    TextView status;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    FirebaseFirestore Fdb; // firestore db
    DatabaseReference databaseUserRef; // real-time db ref
    DocumentReference documentUserRef; // firestore db ref
    // default user info
    Users user = new Users();
    String userUsername, userAvatar, userStatus;
    private boolean isUserChange = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        switchStatus = findViewById(R.id.switchStatus);
        status = findViewById(R.id.status);
        // [END gain]

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Fdb = FirebaseFirestore.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user").child(auth.getUid());
        documentUserRef = Fdb.collection("user").document(auth.getUid());
        // [END config_firebase reference]

        //[Gain User state]
        documentUserRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()) {
                            userAvatar = task.getResult().getString("avatar");
                            userUsername = task.getResult().getString("username");
                            userStatus = task.getResult().getString("privacy");
                            status.setText(userStatus);

                            if (userStatus.equals("Private")) {
                                // Prevent message frames from popping up
                                isUserChange = false; // Do Not Remove
                                switchStatus.setChecked(true);
                                isUserChange = true; // Do Not Remove
                            }


                        } else {
                            Log.w(TAG, "Personal information cannot be obtained", task.getException());
                        }
                    }
                });
        // Switch the screen - SettingActivity
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI();
            }
        });

        switchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // Flag to differentiate user actions from programmatic changes

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isUserChange) {
                    return; // Ignore programmatic changes
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(PrivacyActivity.this);
                builder.setTitle("Confirmation Notification")
                        .setMessage("Toggle account privacy status?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                status.setText(isChecked ? "Private" : "Public");
                                savePrivacySetting(); // Save the new privacy setting
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Temporarily disable listener before reverting switch state
                                isUserChange = false;
                                switchStatus.setChecked(!isChecked); // Revert to previous state
                                isUserChange = true; // Re-enable listener after change
                            }
                        });

                // Show the dialog
                builder.create().show();
            }
        });
    }

    private void savePrivacySetting() {

        userStatus = status.getText().toString();
        Fdb.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(documentUserRef);
                        transaction.update(documentUserRef, "privacy", userStatus);
                        // Success
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(PrivacyActivity.this, "updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PrivacyActivity.this, "failed", Toast.LENGTH_SHORT).show();
                    }
                });

        user.setUID(auth.getUid());
        user.setUsername(userUsername);
        user.setImageURI(userAvatar);
        user.setPrivacy(userStatus);
        databaseUserRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(PrivacyActivity.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PrivacyActivity.this, "Failed to save data.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PrivacyActivity.this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // [START Method]
    // handling UI update
    private void updateUI() {
        Intent intent = new Intent(PrivacyActivity.this, SettingActivity.class);
        startActivity(intent);
        finish();
    }
    // [END Method]
}