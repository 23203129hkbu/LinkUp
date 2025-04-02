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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

// ✅
public class RegistrationActivity extends AppCompatActivity {
    // layout object
    EditText email, pwd, conPwd;
    CheckBox cbxPwd;
    Button btnSignUp;
    TextView btnLogin;
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
    String userConPwd = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        // [START config_firebase]
        auth = FirebaseAuth.getInstance();
        // [END config_firebase]

        // [START gain layout objects]
        email = findViewById(R.id.email);
        pwd = findViewById(R.id.pwd);
        conPwd = findViewById(R.id.conPwd);
        cbxPwd = findViewById(R.id.cbxPwd);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnLogin = findViewById(R.id.btnLogin);
        progressbar = findViewById(R.id.progressbar);
        // [END gain]

        // [START config_dialog]
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating account, please wait...");
        progressDialog.setCancelable(false);
        // [END config_dialog]

        // [START layout component function]
        // Check whether shown password -- checkBox
        cbxPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // show password && confirm password
                    pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    conPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    // keep hiding the password && confirm password
                    pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    conPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        // Sign Up with email, password
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userEmail = email.getText().toString();
                userPwd = pwd.getText().toString();
                userConPwd = conPwd.getText().toString();
                progressDialog.show(); // Show the dialog first
                if (TextUtils.isEmpty(userEmail)) {
                    Toast.makeText(RegistrationActivity.this, "Email is required", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(userPwd)) {
                    Toast.makeText(RegistrationActivity.this, "Password is required", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(userConPwd)) {
                    Toast.makeText(RegistrationActivity.this, "Confirm Password is required", Toast.LENGTH_SHORT).show();
                } else if (!userEmail.matches(emailPattern)) {
                    email.setError("Please enter a valid email address");
                    Toast.makeText(RegistrationActivity.this, "Email address is invalid", Toast.LENGTH_SHORT).show();
                } else if (userPwd.length() < 6) {
                    pwd.setError("Passwords must be six or more characters");
                    Toast.makeText(RegistrationActivity.this, "Passwords are less than six characters", Toast.LENGTH_SHORT).show();
                } else if (!userPwd.equals(userConPwd)) {
                    conPwd.setError("Password and Confirm Password must be match");
                    Toast.makeText(RegistrationActivity.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                } else {
                    progressbar.setVisibility(View.VISIBLE); // Show progress bar
                    // createUserWithEmailAndPassword -> create the record -> user -> for sign in
                    auth.createUserWithEmailAndPassword(userEmail, userPwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                if (task.isSuccessful()) {
                                    updateUI("Main");
                                } else {
                                    // Handle the error
                                    Toast.makeText(RegistrationActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                                }
                                progressDialog.dismiss();
                            } else {
                                Toast.makeText(RegistrationActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        // Switch the screen - Login Activity
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressbar.setVisibility(View.VISIBLE);
                updateUI("Login");
            }
        });
        // [END layout component function]
    }

    // [START Method]
    // handling UI update
    private void updateUI(String screen) {
        Intent intent = null;
        if (screen.equals("Main")) {
            intent = new Intent(RegistrationActivity.this, MainActivity.class);
        } else if(screen.equals("Login")) {
            intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        }
        if (intent != null) {
            startActivity(intent);
            finish();
        }
    }
    // [END Method]
}