package com.example.linkup.Process;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.linkup.R;
import com.example.linkup.SocialLogin.FacebookSignInActivity;
import com.example.linkup.SocialLogin.GoogleSignInActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

// ✅
public class LoginActivity extends AppCompatActivity {
    // layout object
    Button btnLogin, btnGoogleLogin, btnFacebookLogin;
    EditText email, pwd;
    CheckBox cbxPwd;
    TextView btnSignUp;
    ProgressBar progressbar;
    // Limitation / Case handling
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    // Dialog
    ProgressDialog progressDialog;
    // Firebase features
    FirebaseAuth auth; // auth
    // User default registration info
    String userEmail = "";
    String userPwd = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // [START config_firebase]
        auth = FirebaseAuth.getInstance();
        // [END config_firebase]

        // [START gain layout objects]
        btnLogin = findViewById(R.id.btnLogin);
        email = findViewById(R.id.email);
        pwd = findViewById(R.id.pwd);
        cbxPwd = findViewById(R.id.cbxPwd);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnFacebookLogin = findViewById(R.id.btnFacebookLogin);
        progressbar = findViewById(R.id.progressbar);
        // [END gain]

        // [START config_dialog]
        progressDialog = new ProgressDialog(this); // create new dialog
        progressDialog.setMessage("Login account, please wait..."); // input msg
        progressDialog.setCancelable(false); // Can the dialog be canceled?
        // [END config_dialog]

        // [START layout component function]
        // Check whether shown password
        cbxPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    // show password / confirm password
                    pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    // keep hiding the password
                    pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        // SignIn with email, password
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the value from the input
                userEmail = email.getText().toString();
                userPwd = pwd.getText().toString();


                // error handling : empty, invalid format, length limitation
                if ((TextUtils.isEmpty(userEmail))) {
                    Toast.makeText(LoginActivity.this, "Email is required", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(userPwd)) {
                    Toast.makeText(LoginActivity.this, "Password is required", Toast.LENGTH_SHORT).show();
                } else if (!userEmail.matches(emailPattern)) {
                    email.setError("Please enter a valid email address");
                    Toast.makeText(LoginActivity.this, "Email address is invalid", Toast.LENGTH_SHORT).show();
                } else if (pwd.length() < 6) {
                    pwd.setError("Passwords must be six or more characters");
                    Toast.makeText(LoginActivity.this, "Passwords are less than six characters", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.show(); // Show the dialog first
                    progressbar.setVisibility(View.VISIBLE);
                    // When a user signs in to your app, pass the user's email address and password
                    auth.signInWithEmailAndPassword(userEmail, userPwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                updateUI("Main");
                            } else {
                                Toast.makeText(LoginActivity.this, "Incorrect email or password", Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        });
        // Switch the screen - Registration Activity
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressbar.setVisibility(View.VISIBLE);
                updateUI("Registration");
            }
        });
        // Google Sign In
        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressbar.setVisibility(View.VISIBLE);
                updateUI("Google");
            }
        });
        // Facebook Sign In
        btnFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressbar.setVisibility(View.VISIBLE);
                updateUI("Facebook");
            }
        });
        // [END layout component function]
    }

    // [START Method]
    // handling UI update
    private void updateUI(String screen) {
        Intent intent = null;
        if (screen.equals("Main")) {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        } else if (screen.equals("Registration")) {
            intent = new Intent(LoginActivity.this, RegistrationActivity.class);
        } else if (screen.equals("Google")) {
            intent = new Intent(LoginActivity.this, GoogleSignInActivity.class);
        } else if (screen.equals("Facebook")) {
            intent = new Intent(LoginActivity.this, FacebookSignInActivity.class);
        }
        if (intent != null) {
            startActivity(intent);
            finish();
        }
    }
    // [END Method]
}