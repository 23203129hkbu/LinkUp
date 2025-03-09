package com.example.linkup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
    // layout object
    Button btnLogin;
    EditText email, pwd;
    CheckBox cbxPwd;
    TextView btnSignUp;
    ImageView btnGoogleLogin, btnFacebookLogin;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // [START gain layout objects]
        btnLogin = findViewById(R.id.btnLogin);
        email = findViewById(R.id.email);
        pwd = findViewById(R.id.pwd);
        cbxPwd = findViewById(R.id.cbxPwd);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnFacebookLogin = findViewById(R.id.btnFacebookLogin);
        progressBar = findViewById(R.id.progressbar);
        // [END gain]
        // [START layout component function]
        // // Switch the screen - Registration Activity
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI("Registration");
            }
        });
        // [END layout component function]
    }
    // [START Method]
    // handling UI update
    private void updateUI(String screen) {
        Intent intent;
        if (screen.equals("Main")) {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, RegistrationActivity.class);
        }
        startActivity(intent);
        finish();
    }
    // [END Method]
}