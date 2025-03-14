package com.example.linkup.ProfileOperation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.linkup.Fragment.ProfileFragment;
import com.example.linkup.MainActivity;
import com.example.linkup.Object.Users;
import com.example.linkup.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;


public class CreateProfile extends AppCompatActivity {
    // layout object
    ImageView avatarUpload;
    EditText username, website, introduction;
    Button btnSave;
    ProgressBar progressbar;
    // Firebase features
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase Rdb; // real-time db
    FirebaseFirestore Fdb; // firestore db
    StorageReference storageRef; // cloud storage ref
    DatabaseReference databaseUserRef; // real-time db ref
    DocumentReference documentUserRef; // firestore db ref
    // Dialog
    ProgressDialog progressDialog;
    // Upload Photo
    Uri imageURI;
    // default user info
    Users user = new Users();
    String userUsername = "";
    String userWebsite = "";
    String userIntroduction = "";
    // default img is saved in firebase cloud storage
    String imageURIString = "https://firebasestorage.googleapis.com/v0/b/link-up-17148.firebasestorage.app/o/defaulticon.png?alt=media&token=249ae5c9-6d08-4f66-beb7-5297fd864738";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        // [START gain layout objects]
        avatarUpload = findViewById(R.id.avatar);
        username = findViewById(R.id.username);
        website = findViewById(R.id.website);
        introduction = findViewById(R.id.introduction);
        btnSave = findViewById(R.id.btnSave);
        progressbar = findViewById(R.id.progressbar);
        // [END gain]

        // [START config_firebase]
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        Fdb = FirebaseFirestore.getInstance();
        // [END config_firebase]

        // [START config_firebase reference]
        storageRef = storage.getReference();
        databaseUserRef = Rdb.getReference().child("user").child(auth.getUid());
        documentUserRef = Fdb.collection("user").document(auth.getUid());
        // [END config_firebase reference]

        // [START config_dialog]
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);
        // [END config_dialog]

        // [START layout component function]
        // Profile image click listener / Upload the new avatar
        avatarUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });

        // Create Profile
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userUsername = username.getText().toString();
                userWebsite = website.getText().toString();
                userIntroduction = introduction.getText().toString();
                if (TextUtils.isEmpty(userUsername)) {
                    progressDialog.dismiss();
                    Toast.makeText(CreateProfile.this, "Username is required", Toast.LENGTH_SHORT).show();
                } else {
                    progressbar.setVisibility(View.VISIBLE);
                    // handle imageURI To String
                    if (imageURI != null)
                        handleImageURI();
                    // Save profile to Database
                    handleProfileToDatabase();
                    // Save profile (full) to Firestore
                    handleProfileToFirestore();
                    // update UI
                    updateUI();
                }
            }
        });


    }

    // [START Method]
    // Handle image selection
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && data != null) {
            imageURI = data.getData();
            avatarUpload.setImageURI(imageURI);

        }
    }

    private void handleImageURI() {
        storageRef.putFile(imageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageURIString = uri.toString(); // uri convert to string
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(CreateProfile.this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(CreateProfile.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(CreateProfile.this, "Image upload error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleProfileToDatabase() {
        user.setUID(auth.getUid());
        user.setUsername(userUsername);
        user.setImageURI(imageURIString);
        databaseUserRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(CreateProfile.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CreateProfile.this, "Failed to save data.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(CreateProfile.this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleProfileToFirestore() {
        // Prepare the profile data
        Map<String, String> user_firestore = new HashMap<>();
        user_firestore.put("uid", auth.getUid());
        user_firestore.put("username", userUsername);
        user_firestore.put("avatar", imageURIString); // Use empty string if no image
        user_firestore.put("website", userWebsite);
        user_firestore.put("introduction", userIntroduction);
        user_firestore.put("privacy", "Public");
        documentUserRef.set(user_firestore)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(CreateProfile.this, "Profile Created", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // handling UI update
    private void updateUI() {
        Toast.makeText(CreateProfile.this, "Profile Created", Toast.LENGTH_SHORT).show();
        // Delay execution to allow enough time for data to be uploaded
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(CreateProfile.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },4000);
    }
    // [END Method]
}