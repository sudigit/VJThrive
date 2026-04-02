package com.vjti.vjthrive;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vjti.vjthrive.utils.CollegeDataProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    // ── Views ───────────────────────────────────────────────────────────
    private TextInputEditText etName, etEmail, etPassword, etRollNo;
    private MaterialAutoCompleteTextView actvProgramme, actvDepartment, actvBranch, actvGraduationYear;
    private TextInputLayout tilRollNo, tilProgramme, tilDepartment, tilBranch, tilGraduationYear;
    private TextView tvRoleLabel;
    private Button btnRegister;

    // ── State ───────────────────────────────────────────────────────────
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
        if (role == null) role = "student";

        initViews();
        adjustUIForRole();
        setupDropdowns();

        btnRegister.setOnClickListener(v -> handleRegistration());
    }

    // ── Init Views ──────────────────────────────────────────────────────

    private void initViews() {
        tvRoleLabel = findViewById(R.id.tvRoleLabel);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRollNo = findViewById(R.id.etRollNo);

        actvProgramme = findViewById(R.id.actvProgramme);
        actvDepartment = findViewById(R.id.actvDepartment);
        actvBranch = findViewById(R.id.actvBranch);
        actvGraduationYear = findViewById(R.id.actvGraduationYear);

        tilRollNo = findViewById(R.id.tilRollNo);
        tilProgramme = findViewById(R.id.tilProgramme);
        tilDepartment = findViewById(R.id.tilDepartment);
        tilBranch = findViewById(R.id.tilBranch);
        tilGraduationYear = findViewById(R.id.tilGraduationYear);

        btnRegister = findViewById(R.id.btnRegister);
    }

    // ── Role-based visibility ───────────────────────────────────────────

    private void adjustUIForRole() {
        // Set role label
        String label;
        switch (role) {
            case "faculty": label = "Signing up as Faculty"; break;
            case "admin":   label = "Signing up as Admin";   break;
            default:        label = "Signing up as Student"; break;
        }
        tvRoleLabel.setText(label);

        if ("student".equals(role)) {
            tilRollNo.setVisibility(View.VISIBLE);
            tilProgramme.setVisibility(View.VISIBLE);
            // Department, Branch, GradYear start hidden — shown after Programme is selected
            tilDepartment.setVisibility(View.GONE);
            tilBranch.setVisibility(View.GONE);
            tilGraduationYear.setVisibility(View.GONE);

        } else if ("faculty".equals(role)) {
            tilRollNo.setVisibility(View.GONE);
            tilProgramme.setVisibility(View.GONE);
            tilDepartment.setVisibility(View.VISIBLE);
            tilBranch.setVisibility(View.GONE);
            tilGraduationYear.setVisibility(View.GONE);

        } else {
            // Admin — only name, email, password
            tilRollNo.setVisibility(View.GONE);
            tilProgramme.setVisibility(View.GONE);
            tilDepartment.setVisibility(View.GONE);
            tilBranch.setVisibility(View.GONE);
            tilGraduationYear.setVisibility(View.GONE);
        }
    }

    // ── Dropdown setup ──────────────────────────────────────────────────

    private void setupDropdowns() {
        if ("student".equals(role)) {
            setupStudentDropdowns();
        } else if ("faculty".equals(role)) {
            setupFacultyDropdowns();
        }
        // Admin has no dropdowns
    }

    private void setupStudentDropdowns() {
        // 1. Programme dropdown (static list)
        String[] programmes = CollegeDataProvider.getProgrammes();
        actvProgramme.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, programmes));

        // 2. When Programme is selected → populate Department + Graduation Year
        actvProgramme.setOnItemClickListener((parent, view, position, id) -> {
            String selectedProgramme = programmes[position];

            // Clear dependent selections
            actvDepartment.setText("", false);
            actvBranch.setText("", false);
            actvGraduationYear.setText("", false);
            tilBranch.setVisibility(View.GONE);

            // Show and populate Department
            List<String> departments = CollegeDataProvider.getDepartments(selectedProgramme);
            actvDepartment.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, departments));
            tilDepartment.setVisibility(View.VISIBLE);

            // Show and populate Graduation Year
            List<String> years = CollegeDataProvider.getGraduationYears(selectedProgramme);
            actvGraduationYear.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, years));
            tilGraduationYear.setVisibility(View.VISIBLE);
        });

        // 3. When Department is selected → populate Branch
        actvDepartment.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDepartment = actvDepartment.getText().toString();

            // Clear dependent selection
            actvBranch.setText("", false);

            // Show and populate Branch
            List<String> branches = CollegeDataProvider.getBranches(selectedDepartment);
            actvBranch.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, branches));
            tilBranch.setVisibility(View.VISIBLE);
        });
    }

    private void setupFacultyDropdowns() {
        // Faculty uses the combined department list
        List<String> departments = CollegeDataProvider.getAllDepartments();
        actvDepartment.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, departments));
    }

    // ── Registration ────────────────────────────────────────────────────

    private void handleRegistration() {
        String name     = getText(etName);
        String email    = getText(etEmail);
        String password = getText(etPassword);

        // ── Basic validation ────────────────────────────────────────────
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.endsWith("vjti.ac.in")) {
            Toast.makeText(this, "Please use a valid vjti.ac.in email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // ── Role-specific validation ────────────────────────────────────
        String rollNo      = getText(etRollNo);
        String programme   = actvProgramme.getText().toString().trim();
        String department  = actvDepartment.getText().toString().trim();
        String branch      = actvBranch.getText().toString().trim();
        String gradYearStr = actvGraduationYear.getText().toString().trim();

        if ("student".equals(role)) {
            if (rollNo.isEmpty()) {
                Toast.makeText(this, "Please enter your Roll Number", Toast.LENGTH_SHORT).show();
                return;
            }
            if (programme.isEmpty()) {
                Toast.makeText(this, "Please select a Programme", Toast.LENGTH_SHORT).show();
                return;
            }
            if (department.isEmpty()) {
                Toast.makeText(this, "Please select a Department", Toast.LENGTH_SHORT).show();
                return;
            }
            if (branch.isEmpty()) {
                Toast.makeText(this, "Please select a Branch", Toast.LENGTH_SHORT).show();
                return;
            }
            if (gradYearStr.isEmpty()) {
                Toast.makeText(this, "Please select a Graduation Year", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if ("faculty".equals(role) && department.isEmpty()) {
            Toast.makeText(this, "Please select a Department", Toast.LENGTH_SHORT).show();
            return;
        }

        // ── Create Firebase Auth user ───────────────────────────────────
        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Send email verification
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        // Save user data to Firestore regardless of verification email status
                                        int gradYear = 0;
                                        try { gradYear = Integer.parseInt(gradYearStr); }
                                        catch (NumberFormatException ignored) {}

                                        saveUserToFirestore(name, email, rollNo, programme,
                                                department, branch, gradYear);
                                    });
                        }
                    } else {
                        btnRegister.setEnabled(true);
                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed";
                        Toast.makeText(SignupActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ── Save to Firestore ───────────────────────────────────────────────

    private void saveUserToFirestore(String name, String email, String rollNo,
                                     String programme, String department,
                                     String branch, int graduationYear) {
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("role", role);

        if ("student".equals(role)) {
            userData.put("rollNo", rollNo);
            userData.put("programme", programme);
            userData.put("department", department);
            userData.put("branch", branch);
            userData.put("graduationYear", graduationYear);

            // Placeholder subjects (Sem 1 for now — will be updated later)
            List<String> subjects = CollegeDataProvider.getSubjects(programme, branch, 1);
            userData.put("subjects", subjects);

        } else if ("faculty".equals(role)) {
            userData.put("department", department);
        }
        // Admin: only name, email, role — already added above

        db.collection("users").document(uid).set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Sign out so user must verify email before logging in
                    mAuth.signOut();

                    Toast.makeText(SignupActivity.this,
                            "Registration successful! Please check your email to verify your account.",
                            Toast.LENGTH_LONG).show();

                    // Go back to LoginActivity
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    Toast.makeText(SignupActivity.this,
                            "Failed to save user data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
