package com.example.linkup.HomeOperation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.linkup.CommunityOperation.CreateCommunityPost;
import com.example.linkup.Object.Posts;
import com.example.linkup.ProfileOperation.CreateProfile;
import com.example.linkup.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CreatePost extends AppCompatActivity {
    // Layout objects
    ImageView btnBack, image, btnUpload;
    TextView btnPost;
    VideoView video;
    EditText description;
    ProgressBar progressbar;
    // Firebase features
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase Rdb; // Real-time database
    StorageReference storageRef; // Cloud storage reference
    DatabaseReference databasePostRef; // Real-time database reference
    // Dialog
    ProgressDialog progressDialog;
    // Media URI
    Uri selectedMediaURI;
    // Calendar & DateFormat
    Calendar date, time;
    SimpleDateFormat currentDate, currentTime;
    // Post Info
    Posts post = new Posts();
    String postID,postURL, createdDate, createdTime, type, postDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        image = findViewById(R.id.image);
        btnUpload = findViewById(R.id.btnUpload);
        btnPost = findViewById(R.id.btnPost);
        video = findViewById(R.id.video);
        description = findViewById(R.id.description);
        progressbar = findViewById(R.id.progressbar);
        // [END gain]

        // [START config_firebase]
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        // [END config_firebase]

        // [START config_firebase reference]
        databasePostRef = Rdb.getReference().child("post");
        // [END config_firebase reference]

        //[START Calender / Date Format configuration]
        date = Calendar.getInstance();
        time = Calendar.getInstance();
        currentDate = new SimpleDateFormat("dd-MM-yy");
        currentTime = new SimpleDateFormat("HH:mm");
        //[END Calender / Date Format configuration]

        // [START config_dialog]
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);
        // [END config_dialog]

        // [START layout component function]
        // Switch the screen - Profile Fragment
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Handle Upload button click
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open gallery to select image or video
                Intent intent = new Intent();
                intent.setType("image/*"); // Allow image types
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"}); // Include video types
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select Picture or Video"), 1);
            }
        });

        // Create Profile
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postID = databasePostRef.push().getKey();
                postDescription = description.getText().toString();
                createdDate = currentDate.format(date.getTime());
                createdTime = currentTime.format(time.getTime());

                if (TextUtils.isEmpty(postDescription)) {
                    progressDialog.dismiss();
                    Toast.makeText(CreatePost.this, "Description is required", Toast.LENGTH_SHORT).show();
                } else if (selectedMediaURI == null) {
                    progressDialog.dismiss();
                    Toast.makeText(CreatePost.this, "Please upload photos or videos", Toast.LENGTH_SHORT).show();
                } else {
                    progressbar.setVisibility(View.VISIBLE);
                    // handle mediaURI To String -> Create post to database
                    handleMediaURI();

                }
            }
        });
        // [END layout component function]
    }

    // [START Method]
    // handling UI update
    private void updateUI() {
        Toast.makeText(CreatePost.this, "Post Created", Toast.LENGTH_SHORT).show();
        // Delay execution to allow enough time for data to be uploaded
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
    }

    //
    private void handleMediaURI() {

        storageRef = storage.getReference().child("posts/" + postID);
        // Upload the file to the specified path
        storageRef.putFile(selectedMediaURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            postURL = uri.toString(); // uri convert to string
                            // Save the download URL
                            // Now update the profile in the database after setting the avatarURL
                            handlePostToDatabase();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(CreatePost.this, "Failed to get media URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(CreatePost.this, "Media upload failed.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(CreatePost.this, "Media upload error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            selectedMediaURI = data.getData(); // Get the selected media URI
            String mediaType = getContentResolver().getType(selectedMediaURI); // Get the MIME type of the selected media
            if (mediaType != null) {
                if (mediaType.startsWith("image/")) {
                    // The selected media is an image
                    video.setVisibility(View.GONE); // Hide the VideoView
                    image.setVisibility(View.VISIBLE); // Show the ImageView
                    image.setImageURI(selectedMediaURI); // Display the selected image
                    type = "image";
                } else if (mediaType.startsWith("video/")) {
                    // The selected media is a video
                    image.setVisibility(View.GONE); // Hide the ImageView
                    video.setVisibility(View.VISIBLE); // Show the VideoView
                    video.setVideoURI(selectedMediaURI); // Set the video URI
                    video.start(); // Play the video
                    type = "video";
                }
            }
        }
    }

    // Ref - update realtime db
    private void handlePostToDatabase() {
        post.setUID(auth.getUid());
        post.setPostURL(postURL);
        post.setDate(createdDate);
        post.setTime(createdTime);
        post.setType(type);
        post.setDescription(postDescription);
        databasePostRef.child(postID).setValue(post);
        // Update UI
        updateUI();
    }
    // [END Method]
}