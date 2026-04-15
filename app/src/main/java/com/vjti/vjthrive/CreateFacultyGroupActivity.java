package com.vjti.vjthrive;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vjti.vjthrive.models.Chat;
import com.vjti.vjthrive.utils.CollegeDataProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CreateFacultyGroupActivity extends AppCompatActivity {

    private EditText etGroupTitle, etSubjectFilter;
    private AutoCompleteTextView actvProgramme, actvDepartment, actvBranch, actvGradYear;
    private TextView tvMatchingCount;
    private ExtendedFloatingActionButton fabCreateGroup;

    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_faculty_group);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        initViews();
        setupDropdowns();

        fabCreateGroup.setOnClickListener(v -> createGroup());
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbarFacultyGroup);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        etGroupTitle = findViewById(R.id.etGroupTitle);
        etSubjectFilter = findViewById(R.id.etSubjectFilter);
        actvProgramme = findViewById(R.id.actvProgramme);
        actvDepartment = findViewById(R.id.actvDepartment);
        actvBranch = findViewById(R.id.actvBranch);
        actvGradYear = findViewById(R.id.actvGradYear);
        tvMatchingCount = findViewById(R.id.tvMatchingCount);
        fabCreateGroup = findViewById(R.id.fabCreateGroup);
    }

    private void setupDropdowns() {
        // Programme
        String[] programmes = CollegeDataProvider.getProgrammes();
        actvProgramme.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, programmes));

        actvProgramme.setOnItemClickListener((parent, view, position, id) -> {
            String selectedProg = programmes[position];
            
            // Depts
            List<String> depts = CollegeDataProvider.getDepartments(selectedProg);
            actvDepartment.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, depts));
            actvDepartment.setText("");

            // Grad Years
            List<String> years = CollegeDataProvider.getGraduationYears(selectedProg);
            actvGradYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, years));
            actvGradYear.setText("");
        });

        actvDepartment.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDept = actvDepartment.getText().toString();
            List<String> branches = CollegeDataProvider.getBranches(selectedDept);
            actvBranch.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, branches));
            actvBranch.setText("");
        });
    }

    private void createGroup() {
        String title = etGroupTitle.getText().toString().trim();
        String programme = actvProgramme.getText().toString().trim();
        String dept = actvDepartment.getText().toString().trim();
        String branch = actvBranch.getText().toString().trim();
        String gradYearStr = actvGradYear.getText().toString().trim();
        String subjectFilter = etSubjectFilter.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etGroupTitle.setError("Title required");
            return;
        }

        fabCreateGroup.setEnabled(false);
        Toast.makeText(this, "Finding matching students...", Toast.LENGTH_SHORT).show();

        // Build Query
        com.google.firebase.firestore.Query query = db.collection("users").whereEqualTo("role", "student");

        if (!programme.isEmpty()) query = query.whereEqualTo("programme", programme);
        if (!dept.isEmpty()) query = query.whereEqualTo("department", dept);
        if (!branch.isEmpty()) query = query.whereEqualTo("branch", branch);
        if (!gradYearStr.isEmpty()) {
            try {
                query = query.whereEqualTo("graduationYear", Integer.parseInt(gradYearStr));
            } catch (NumberFormatException ignored) {}
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> memberIds = new ArrayList<>();
            memberIds.add(currentUserId); // Add faculty as member

            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                // If subject filter is specified, check against user's subjects list
                if (!subjectFilter.isEmpty()) {
                    List<String> subjects = (List<String>) doc.get("subjects");
                    if (subjects != null) {
                        boolean match = false;
                        for (String s : subjects) {
                            if (s.toLowerCase().contains(subjectFilter.toLowerCase())) {
                                match = true;
                                break;
                            }
                        }
                        if (match) memberIds.add(doc.getId());
                    }
                } else {
                    memberIds.add(doc.getId());
                }
            }

            if (memberIds.size() <= 1) {
                fabCreateGroup.setEnabled(true);
                Toast.makeText(this, "No matching students found", Toast.LENGTH_SHORT).show();
                return;
            }

            saveChatToFirestore(title, programme, dept, branch, gradYearStr, memberIds);
        }).addOnFailureListener(e -> {
            fabCreateGroup.setEnabled(true);
            Toast.makeText(this, "Query failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveChatToFirestore(String title, String programme, String dept, String branch, String gradYear, List<String> members) {
        String chatId = UUID.randomUUID().toString();
        Chat chat = new Chat(chatId, title, programme, dept, branch, gradYear, currentUserId, members, true);

        db.collection("chats").document(chatId).set(chat)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Group created with " + (members.size() - 1) + " students", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    fabCreateGroup.setEnabled(true);
                    Toast.makeText(this, "Failed to save group", Toast.LENGTH_SHORT).show();
                });
    }
}
