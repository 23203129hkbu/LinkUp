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

import com.example.linkup.Process.MainActivity;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class UpdateProfile extends AppCompatActivity {
    // layout object
    ImageView avatar, btnBack;
    EditText username, website, introduction;
    Button btnSave, btnUpload;
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
    String userUsername, userIntroduction, userWebsite, userAvatar, userStatus;
    // Prevent user status from disappearing -> real DB -> userStatus


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
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

        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        Fdb = FirebaseFirestore.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        //[END configuration]

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

        //[Gain User Profile]
        documentUserRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()) {
                            userAvatar = task.getResult().getString("avatar");
                            userUsername = task.getResult().getString("username");
                            userIntroduction = task.getResult().getString("introduction");
                            userWebsite = task.getResult().getString("website");
                            userStatus = task.getResult().getString("privacy");

                            Picasso.get().load(userAvatar).into(avatar);
                            username.setText(userUsername);
                            introduction.setText(userIntroduction);
                            website.setText(userWebsite);
                        } else {
                            Log.w(TAG, "Personal information cannot be obtained", task.getException());
                        }
                    }
                });

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
                userUsername = username.getText().toString();
                userWebsite = website.getText().toString();
                userIntroduction = introduction.getText().toString();
                if (TextUtils.isEmpty(userUsername)) {
                    progressDialog.dismiss();
                    Toast.makeText(UpdateProfile.this, "Username is required", Toast.LENGTH_SHORT).show();
                } else {
                    progressbar.setVisibility(View.VISIBLE);
                    Toast.makeText(UpdateProfile.this, "Updating...", Toast.LENGTH_SHORT).show();
                    // handle imageURI To String
                    if (imageURI != null)
                        handleImageURI();
                    // Update profile to database
                    updateProfileToDatabase();
                    // Update profile to Firestore
                    updateProfileToFirestore();
                    // update UI
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateUI();
                        }
                    }, 4000);
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
        if (requestCode == 10 && data != null) {
            imageURI = data.getData();
            avatar.setImageURI(imageURI);

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
                            userAvatar = uri.toString(); // uri convert to string
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UpdateProfile.this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(UpdateProfile.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(UpdateProfile.this, "Image upload error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfileToDatabase() {
        user.setUID(auth.getUid());
        user.setUsername(userUsername);
        user.setImageURI(userAvatar);
        user.setPrivacy(userStatus);
        databaseUserRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(UpdateProfile.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UpdateProfile.this, "Failed to save data.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(UpdateProfile.this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfileToFirestore() {
        Fdb.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(documentUserRef);
                        transaction.update(documentUserRef, "avatar", userAvatar);
                        transaction.update(documentUserRef, "username", userUsername);
                        transaction.update(documentUserRef, "website", userWebsite);
                        transaction.update(documentUserRef, "introduction", userIntroduction);
                        // Success
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UpdateProfile.this, "updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UpdateProfile.this, "failed", Toast.LENGTH_SHORT).show();
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