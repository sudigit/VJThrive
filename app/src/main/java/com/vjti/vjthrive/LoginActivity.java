package com.vjti.vjthrive;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return; // Important: Return so layout doesn't inflate
        } else if (currentUser != null) {
            // User exists but email not verified — sign them out
            mAuth.signOut();
        }

        setContentView(R.layout.activity_login);

        // Initialize Views
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterNow = findViewById(R.id.tvRegisterNow);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegisterNow.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RoleSelectionActivity.class));
        });
    }

    private void loginUser() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // Email verified — proceed to main app
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            // Email NOT verified — block login
                            mAuth.signOut();
                            Toast.makeText(LoginActivity.this,
                                    "Please verify your email first. Check your inbox.",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Authentication failed.";
                        Toast.makeText(LoginActivity.this, errorMsg,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
