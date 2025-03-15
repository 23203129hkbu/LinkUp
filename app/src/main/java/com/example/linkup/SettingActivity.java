package com.example.linkup;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.linkup.Process.LoginActivity;
import com.example.linkup.Process.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingActivity extends AppCompatActivity {
    // layout object
    ImageView btnBack;
    TextView btnLogout, btnState, btnDeletePF;
    // Firebase features
    FirebaseAuth auth; // auth
    FirebaseFirestore Fdb; // firestore db
    DocumentReference documentUserRef; // firestore db ref

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);
        btnState = findViewById(R.id.btnState);
        btnDeletePF = findViewById(R.id.btnDeletePF);
        // [END gain]

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Fdb = FirebaseFirestore.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        documentUserRef = Fdb.collection("user").document(auth.getUid());
        // [END config_firebase reference]

        // [START layout component function]
        // Switch the screen - Profile
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI("Profile");
            }
        });
        // Logout with dialog message
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Confirmation Notification")
                        .setMessage("Log out of your account ?")
                        .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                auth.signOut();
                                updateUI("Login");
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

        // Switch the screen - Privacy Activity
        btnState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI("Privacy");
            }
        });

        // Switch the user state (public, privacy)
        btnDeletePF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Confirmation Notification")
                        .setMessage("Delete your profile ?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                builder.setTitle("Warning")
                                        .setMessage("Your profile will be deleted and cannot be recovered.\n\nAre you sure you still want to perform this operation?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                documentUserRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(SettingActivity.this, "Your profile has been deleted", Toast.LENGTH_SHORT).show();
                                                        Toast.makeText(SettingActivity.this, "You can recreate your profile", Toast.LENGTH_SHORT).show();
                                                        updateUI("Profile");
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

        // [END layout component function]
    }

    // [START Method]
    // handling UI update
    private void updateUI(String screen) {
        Intent intent = null;
        if (screen.equals("Profile")) {
            intent = new Intent(SettingActivity.this, MainActivity.class);
        } else if (screen.equals("Login")) {
            intent = new Intent(SettingActivity.this, LoginActivity.class);
        } else if (screen.equals("Privacy")) {
            intent = new Intent(SettingActivity.this, PrivacyActivity.class);
        }
        if (intent != null) {
            startActivity(intent);
            finish();
        }
    }
    // [END Method]
}