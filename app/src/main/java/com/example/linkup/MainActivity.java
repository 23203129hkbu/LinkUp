package com.example.linkup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    TextView logout;
    // Firebase features
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logout = findViewById(R.id.logout);
        //[START Firebase configuration - get a object]
        auth = FirebaseAuth.getInstance();
        //[END configuration]

        // Check if the user is logged in
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                // No need to call finish() here
            }
        });

    }
}