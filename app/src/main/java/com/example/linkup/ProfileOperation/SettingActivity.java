package com.example.linkup.ProfileOperation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.linkup.CommunityOperation.ArticleActivity;
import com.example.linkup.HomeOperation.UserProfile;
import com.example.linkup.Object.Users;
import com.example.linkup.Process.LoginActivity;
import com.example.linkup.Process.MainActivity;
import com.example.linkup.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

// ✅
public class SettingActivity extends AppCompatActivity {
    // layout object
    ImageView btnBack;
    TextView btnLogout, btnState, btnDeleteAC, btnScanQRCode, btnGenQRCode;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef; // real-time db ref

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user").child(auth.getUid());
        // [END config_firebase reference]

        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);
        btnState = findViewById(R.id.btnState);
        btnDeleteAC = findViewById(R.id.btnDeleteAC);
        btnScanQRCode = findViewById(R.id.btnScanQRCode);
        btnGenQRCode = findViewById(R.id.btnGenQRCode);
        // [END gain]


        // [START layout component function]
        // Switch the screen - Profile
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
        // Switch the screen - QR Code Scanner Activity
        btnScanQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(SettingActivity.this);
                intentIntegrator.setOrientationLocked(true)// Lock the screen in portrait mode
                        .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                        .setPrompt("")
                        .setCameraId(0)
                        .initiateScan();
            }
        });

        // Switch the screen - QR Code Generator Activity
        btnGenQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI("Generator");
            }
        });
        // Switch the user state (public, privacy)
        btnDeleteAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Confirmation Notification")
                        .setMessage("Delete your account ?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                builder.setTitle("Warning")
                                        .setMessage("Your account will be deleted and cannot be recovered.\n\nAre you sure you still want to perform this operation?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                databaseUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            // Article is already saved, remove it
                                                            databaseUserRef.removeValue()
                                                                    .addOnCompleteListener(task -> {
                                                                        if (task.isSuccessful()) {
                                                                            Toast.makeText(SettingActivity.this, "User account deleted", Toast.LENGTH_SHORT).show();
                                                                            updateUI("Login");
                                                                        } else {
                                                                            Toast.makeText(SettingActivity.this, "Failed to remove user AC", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Toast.makeText(SettingActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
        if (screen.equals("Login")) {
            intent = new Intent(SettingActivity.this, LoginActivity.class);
        } else if (screen.equals("Privacy")) {
            intent = new Intent(SettingActivity.this, PrivacyActivity.class);
        } else if (screen.equals("Generator")) {
            intent = new Intent(SettingActivity.this, QRCodeGeneratorActivity.class);
        }
        if (intent != null) {
            startActivity(intent);
            finish();
        }
    }
    //
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (intentResult !=null){
            String uid = intentResult.getContents();
            if (uid!=null){
                Users storedUser = new Users();
                // Be follower
                storedUser.setUID(auth.getUid());
                DatabaseReference databaseFollowerRef = Rdb.getReference().child("follower").child(uid);
                databaseFollowerRef.child(auth.getUid()).setValue(storedUser);
                // Insert a following
                storedUser.setUID(uid);
                DatabaseReference databaseYourFollowingRef = Rdb.getReference().child("following").child(auth.getUid());
                databaseYourFollowingRef.child(uid).setValue(storedUser);
                Toast.makeText(SettingActivity.this, "followed this user", Toast.LENGTH_SHORT).show();
            }
        }else{
            super.onActivityResult(requestCode,resultCode,data);
        }
    }
    // [END Method]
}