package com.example.linkup.ProfileOperation;

import static android.app.ProgressDialog.show;
import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.linkup.Object.Articles;
import com.example.linkup.Process.MainActivity;
import com.example.linkup.Object.Users;
import com.example.linkup.Process.RegistrationActivity;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

// ✅
public class UpdateProfile extends AppCompatActivity {
    // layout object
    CircleImageView avatar;
    ImageView btnBack;
    EditText username, website, introduction;
    Button btnSave, btnUpload;
    ProgressBar progressbar;
    // Firebase features
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase Rdb; // real-time db
    StorageReference storageRef; // cloud storage ref
    DatabaseReference databaseUserRef; // real-time db ref
    // Dialog
    ProgressDialog progressDialog;
    // Upload Photo
    Uri imageURI;
    // default user info
    Users user = new Users();
    // Prevent user status from disappearing -> real DB -> userStatus


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

        // [START config_firebase reference]
        storageRef = storage.getReference().child("avatars/" + auth.getUid() + ".jpg");
        databaseUserRef = Rdb.getReference().child("user").child(auth.getUid());
        // [END config_firebase reference]

        // [START gain layout objects]
        avatar = findViewById(R.id.avatar);
        username = findViewById(R.id.username);
        website = findViewById(R.id.website);
        introduction = findViewById(R.id.introduction);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        btnUpload = findViewById(R.id.btnUpload);
        progressbar = findViewById(R.id.progressbar);
        // [END gain]

        // [START config_dialog]
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);
        // [END config_dialog]

        // [START config_layout]
        // [Gain User Profile]
        databaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    user = snapshot.getValue(Users.class);
                    // Layout Control
                    username.setText(user.getUsername());
                    Picasso.get().load(user.getAvatarURL()).into(avatar);
                    website.setText(user.getWebsite());
                    introduction.setText(user.getIntroduction());
                } else {
                    Toast.makeText(UpdateProfile.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfile.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Personal information cannot be obtained: " + error.getMessage());
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
        // Update Profile
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setUsername(username.getText().toString());
                user.setWebsite(website.getText().toString());
                user.setIntroduction(introduction.getText().toString());

                progressDialog.show(); // Show the dialog before dismissing it
                if (TextUtils.isEmpty(user.getUsername())) {
                    progressDialog.dismiss();
                    Toast.makeText(UpdateProfile.this, "Username is required", Toast.LENGTH_SHORT).show();
                } else {
                    progressbar.setVisibility(View.VISIBLE);
                    Toast.makeText(UpdateProfile.this, "Updating...", Toast.LENGTH_SHORT).show();
                    // handle imageURI To String -> Update profile to database
                    handleImageURI();

                }
            }
        });
        // Profile image click listener / Upload the new avatar
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });
        // [END layout component function]
    }


    // [START Method]
    // Handle image selection
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageURI = data.getData();
            avatar.setImageURI(imageURI);
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImageURI() {
        if (imageURI != null) {
            storageRef.putFile(imageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                user.setAvatarURL(uri.toString()); // Update avatar URL
                                updateProfileToDatabase();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(UpdateProfile.this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                updateProfileToDatabase(); // Use existing avatar URL
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(UpdateProfile.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                        updateProfileToDatabase(); // Use existing avatar URL
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(UpdateProfile.this, "Image upload error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateProfileToDatabase(); // Use existing avatar URL
                }
            });
        } else {
            updateProfileToDatabase(); // No new image selected, use existing avatar URL
        }
    }

    // Update Profile to real time db
    private void updateProfileToDatabase() {
        databaseUserRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                progressbar.setVisibility(View.GONE); // Hide progress bar
                if (task.isSuccessful()) {
                    Toast.makeText(UpdateProfile.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    updateUI();
                } else {
                    Toast.makeText(UpdateProfile.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                progressbar.setVisibility(View.GONE); // Hide progress bar
                Toast.makeText(UpdateProfile.this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // handling UI update
    private void updateUI() {
        Intent intent = new Intent(UpdateProfile.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    // [END Method]
}