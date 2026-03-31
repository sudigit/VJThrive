package com.vjti.vjthrive;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword;
    private MaterialAutoCompleteTextView actvDepartment, actvBranch, actvGraduationYear;
    private TextInputLayout tilDepartment, tilBranch, tilGraduationYear;
    private Button btnRegister;

    private String role = "";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        role = getIntent().getStringExtra("ROLE");
        if (role == null) role = "student"; // Default fallback

        initViews();
        setupDropdowns();
        adjustUIForRole();

        btnRegister.setOnClickListener(v -> handleRegistration());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        
        actvDepartment = findViewById(R.id.actvDepartment);
        actvBranch = findViewById(R.id.actvBranch);
        actvGraduationYear = findViewById(R.id.actvGraduationYear);
        
        tilDepartment = findViewById(R.id.tilDepartment);
        tilBranch = findViewById(R.id.tilBranch);
        tilGraduationYear = findViewById(R.id.tilGraduationYear);
        
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void setupDropdowns() {
        String[] departments = {"CE", "IT", "ME", "EE", "EX", "Prod", "Textile"};
        String[] branches = {"Computer Engineering", "Information Technology", "Mechanical Engineering", "Electrical Engineering"};
        String[] years = {"FY", "SY", "TY", "BTech"};

        actvDepartment.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, departments));
        actvBranch.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, branches));
        actvGraduationYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, years));
    }

    private void adjustUIForRole() {
        if ("student".equals(role)) {
            tilBranch.setVisibility(View.VISIBLE);
            tilGraduationYear.setVisibility(View.VISIBLE);
            tilDepartment.setVisibility(View.GONE);
        } else if ("faculty".equals(role)) {
            tilBranch.setVisibility(View.GONE);
            tilGraduationYear.setVisibility(View.GONE);
            tilDepartment.setVisibility(View.VISIBLE);
        } else {
            // Admin or others
            tilBranch.setVisibility(View.GONE);
            tilGraduationYear.setVisibility(View.GONE);
            tilDepartment.setVisibility(View.GONE);
        }
    }

    private void handleRegistration() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all basic fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.contains("@") || !email.endsWith("vjti.ac.in")) {
            Toast.makeText(this, "Please use a valid vjti.ac.in email", Toast.LENGTH_SHORT).show();
            return;
        }

        String department = actvDepartment.getText().toString();
        String branch = actvBranch.getText().toString();
        String yearStr = actvGraduationYear.getText().toString();

        if ("student".equals(role) && (branch.isEmpty() || yearStr.isEmpty())) {
            Toast.makeText(this, "Please select Branch and Year", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("faculty".equals(role) && department.isEmpty()) {
            Toast.makeText(this, "Please select Department", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserToFirestore(name, email, department, branch, yearStr);
                    } else {
                        btnRegister.setEnabled(true);
                        Toast.makeText(SignupActivity.this, "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String name, String email, String department, String branch, String year) {
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("role", role);
        
        if ("student".equals(role)) {
            user.put("branch", branch);
            user.put("year", year);
        } else if ("faculty".equals(role)) {
            user.put("department", department);
        }

        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignupActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                    finishAffinity(); // Clear stack
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    Toast.makeText(SignupActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                });
    }
}
