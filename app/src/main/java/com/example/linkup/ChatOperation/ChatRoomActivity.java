package com.example.linkup.ChatOperation;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.linkup.CommunityOperation.ArticleActivity;
import com.example.linkup.CommunityOperation.UpdateCommunityPost;
import com.example.linkup.Object.Messages;
import com.example.linkup.Object.Users;
import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomActivity extends AppCompatActivity {
    // layout object
    ImageView btnBack, btnFavorites, btnVoice, btnImage, btnSend;
    CircleImageView receiverAvatar;
    TextView receiverUsername, typingStatus;
    RecyclerView messageRV;
    EditText message;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb; // real-time db
    DatabaseReference databaseUserRef, databaseSenderRef, databaseReceiverRef; // real-time db ref ;
    // User (receiver) Info
    Users receiver = new Users();
    // Message Info
    Messages msg = new Messages();
    String content;
    Date date = new Date();
    // Sender Room / Receiver Room
    String senderRoom, receiverRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        // [START gain value from other activity]
        receiver = (Users) getIntent().getSerializableExtra("user");
        // [END gain]

        // [START config_firebase]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        // [END config_firebase]

        // [Create SenderRoom / ReceiverRoom]
        senderRoom = auth.getUid() + receiver.getUID();
        receiverRoom = receiver.getUID() + auth.getUid();
        // [Created SenderRoom / ReceiverRoom]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user").child(receiver.getUID());
        databaseSenderRef = Rdb.getReference().child("chatRoom").child(auth.getUid()).child(senderRoom);
        databaseReceiverRef = Rdb.getReference().child("chatRoom").child(receiver.getUID()).child(receiverRoom);
        // [END config_firebase reference]

        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        btnFavorites = findViewById(R.id.btnFavorites);
        btnVoice = findViewById(R.id.btnVoice);
        btnImage = findViewById(R.id.btnImage);
        btnSend = findViewById(R.id.btnSend);
        receiverAvatar = findViewById(R.id.receiverAvatar);
        receiverUsername = findViewById(R.id.receiverUsername);
        typingStatus = findViewById(R.id.typingStatus);
        messageRV = findViewById(R.id.messageRV);
        message = findViewById(R.id.message);
        // [END gain]

        // [START config_layout]
        // [Gain User Profile]
        databaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    receiver = snapshot.getValue(Users.class);
                    // Layout Control
                    receiverUsername.setText(receiver.getUsername());
                    Picasso.get().load(receiver.getAvatarURL()).into(receiverAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatRoomActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // [END config_layout]

        // [START layout component function]
        // Back button action
        btnBack.setOnClickListener(v -> finish());
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
        // [END layout component function]
    }

    // [START Method]
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