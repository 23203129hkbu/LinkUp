package com.example.linkup.ProfileOperation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.linkup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeGeneratorActivity extends AppCompatActivity {
    // layout object
    ImageView btnBack, qrCode;
    TextView username;
    // Firebase features
    FirebaseAuth auth;
    FirebaseDatabase Rdb;
    DatabaseReference databaseUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_generator);

        // [START gain layout objects]
        btnBack = findViewById(R.id.btnBack);
        qrCode = findViewById(R.id.qrCode);
        username = findViewById(R.id.username);
        // [END gain]

        // [START config_firebase]
        auth = FirebaseAuth.getInstance();
        Rdb = FirebaseDatabase.getInstance();
        // [END config_firebase]

        // [START config_firebase reference]
        databaseUserRef = Rdb.getReference().child("user").child(auth.getUid());
        // [END config_firebase reference]

        // [START config_firebase reference]
        databaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    username.setText(snapshot.child("username").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QRCodeGeneratorActivity.this, "Load failed：" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Generate QR Code
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(auth.getUid(), BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate QR Code", Toast.LENGTH_SHORT).show();
        }

        // [START layout component function]
        // Switch the screen - Setting Activity
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // [END layout component function]
    }

}