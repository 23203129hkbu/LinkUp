package com.example.linkup.ChatOperation;

import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.Adapter.ArticleCommentAdapter;
import com.example.linkup.Adapter.MessageAdapter;
import com.example.linkup.CommunityOperation.ArticleActivity;
import com.example.linkup.CommunityOperation.UpdateCommunityPost;
import com.example.linkup.Object.ArticleComments;
import com.example.linkup.Object.Articles;
import com.example.linkup.Object.Messages;
import com.example.linkup.Object.Users;
import com.example.linkup.Object.UsersStatus;
import com.example.linkup.R;
import com.google.android.exoplayer2.C;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

// ✅
public class ChatRoomActivity extends AppCompatActivity {
    // layout object
    ImageView btnBack, btnFavorites, btnVoice, btnImage, btnSend;
    CircleImageView receiverAvatar;
    TextView receiverUsername, userStatus;
    RecyclerView messageRV;
    EditText message;
    // Firebase features
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase Rdb; // real-time db
    StorageReference chatImageRef, chatAudioRef; // cloud storage ref
    DatabaseReference databaseMyStatusRef, databaseYourStatusRef, databaseSendUserRef, databaseReceiveUserRef, databaseSenderRef, databaseReceiverRef, databaseBestChatRoomRef; // real-time db ref ;
    // User (receiver) Info
    Users receiver = new Users();
    public static String senderImg;
    public static String receiverImg;
    // convert message data into RecyclerView by Adapter
    ArrayList<Messages> messagesArrayList = new ArrayList<>();
    MessageAdapter messageAdapter;
    // Message Info
    Messages msg = new Messages();
    String content;
    Date date = new Date();
    // typing
    private Handler typingHandler = new Handler();
    private Runnable typingTimeoutRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        // [START gain value from other activity]
        receiver = (Users) getIntent().getSerializableExtra("user");
        // [END gain]

        // [START config_firebase]
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        // [END config_firebase]

        // [START config_firebase reference - storage]
        chatImageRef = storage.getReference().child("chat_image/" + new Date().getTime() + ".jpg");
        // [END config_firebase reference]

        // [START config_firebase reference]
        databaseSendUserRef = Rdb.getReference().child("user").child(auth.getUid());
        databaseReceiveUserRef = Rdb.getReference().child("user").child(receiver.getUID());
        databaseSenderRef = Rdb.getReference().child("chatRoom").child(auth.getUid()).child(receiver.getUID());
        databaseReceiverRef = Rdb.getReference().child("chatRoom").child(receiver.getUID()).child(auth.getUid());
        databaseBestChatRoomRef = Rdb.getReference().child("likedChatRoom").child(auth.getUid()).child(receiver.getUID());
        databaseYourStatusRef = Rdb.getReference().child("status").child(auth.getUid()).child(receiver.getUID());
        databaseMyStatusRef = Rdb.getReference().child("status").child(receiver.getUID()).child(auth.getUid());
        // [END config_firebase reference]

        // [When I come in chatroom, update my status]
        UpdateUserStatus("Online");
        // [END config_firebase reference]


        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        btnFavorites = findViewById(R.id.btnFavorites);
        btnVoice = findViewById(R.id.btnVoice);
        btnImage = findViewById(R.id.btnImage);
        btnSend = findViewById(R.id.btnSend);
        receiverAvatar = findViewById(R.id.receiverAvatar);
        receiverUsername = findViewById(R.id.receiverUsername);
        userStatus = findViewById(R.id.userStatus);
        messageRV = findViewById(R.id.messageRV);
        message = findViewById(R.id.message);
        // [END gain]

        // [START config_layout]
        // check receiver is typing;
        databaseYourStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userStatus.setText(snapshot.child("status").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatRoomActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Check if the chat room is already saved
        databaseBestChatRoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    btnFavorites.setImageResource(R.drawable.baseline_star_24_blue);
                } else {
                    btnFavorites.setImageResource(R.drawable.baseline_star_outline_24_gray);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatRoomActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // [Gain User Profile]
        // Gain Sender Image
        databaseSendUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // store sender Image
                    senderImg = snapshot.child("avatarURL").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatRoomActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Gain Receiver Image / Info
        databaseReceiveUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    receiver = snapshot.getValue(Users.class);
                    // Layout Control
                    receiverUsername.setText(receiver.getUsername());
                    Picasso.get().load(receiver.getAvatarURL()).into(receiverAvatar);
                    // store receiver Image
                    receiverImg = receiver.getAvatarURL();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatRoomActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Load messages
        databaseSenderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Messages message = dataSnapshot.getValue(Messages.class);
                    // Ensure message is not null before proceeding
                    if (message != null) {
                        messagesArrayList.add(message);
                    }
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
        // Grant value - which view, comment array list
        messageAdapter = new MessageAdapter(ChatRoomActivity.this, messagesArrayList);
        // Set up the layout manager, adapter
        messageRV.setLayoutManager(new LinearLayoutManager(ChatRoomActivity.this));
        messageRV.setHasFixedSize(true);
        messageRV.setAdapter(messageAdapter);
        // [END config_layout]

        // [START layout component function]
        // Typing
        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                UpdateUserStatus("Typing...");

                // Remove previous callbacks
                if (typingTimeoutRunnable != null) {
                    typingHandler.removeCallbacks(typingTimeoutRunnable);
                }

                // Set new timeout to go back to "Online" after 2 seconds of no typing
                typingTimeoutRunnable = () -> UpdateUserStatus("Online");
                typingHandler.postDelayed(typingTimeoutRunnable, 2000); // 2 second
            }
        });
        // Back button action
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                UpdateUserStatus("Left");
            }
        });
        // Send Message (Image)
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });
        // Send Message (Text)
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content = message.getText().toString();
                if ((TextUtils.isEmpty(content))) {
                    Toast.makeText(ChatRoomActivity.this, "Message cannot be empty ", Toast.LENGTH_SHORT).show();
                } else {
                    // Empty message box
                    message.setText("");
                    // Create Comment
                    SendMessage(content,"text");
                }
            }
        });
        // Store to be favourite chat room
        btnFavorites.setOnClickListener(view -> {
            databaseBestChatRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Chat Room is already saved, unsaved it
                        databaseBestChatRoomRef.removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ChatRoomActivity.this, "chat room unsaved", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ChatRoomActivity.this, "Failed to unsaved room", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Users storedUser = new Users();
                        storedUser.setUID(receiver.getUID());
                        // Chat Room is not saved, save it
                        databaseBestChatRoomRef.setValue(storedUser).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ChatRoomActivity.this, "Room saved", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ChatRoomActivity.this, "Failed to save room", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ChatRoomActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        // [END layout component function]
    }

    private void UpdateUserStatus(String status) {
        UsersStatus storedStatus = new UsersStatus(auth.getUid(),status);
        databaseMyStatusRef.setValue(storedStatus);
    }

    // [START Method]
    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateUserStatus("Left");
    }
    // Handle image selection
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            chatImageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                chatImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    SendMessage(uri.toString(), "image"); // directly send image
                });
            });
        } else{
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
    // Send Message Text, Image,
    private void SendMessage(String content, String type) {
        // Create Message
        msg.setContent(content);
        msg.setType(type);
        msg.setUID(auth.getUid());
        msg.setTimestamp(date.getTime());
        // Save message
        UpdateToFirebase(msg);
    }

    private void UpdateToFirebase(Messages msg) {
        databaseSenderRef.push().setValue(msg).addOnCompleteListener(senderTask -> {
            if (senderTask.isSuccessful()) {
                databaseReceiverRef.push().setValue(msg).addOnCompleteListener(receiverTask -> {
                    if (receiverTask.isSuccessful()) {
                        Toast.makeText(ChatRoomActivity.this, "send a message", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatRoomActivity.this, "Failed to store receiver msg", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ChatRoomActivity.this, "Failed to store sender msg", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // [END Method]
}