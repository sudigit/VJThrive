package com.vjti.vjthrive;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vjti.vjthrive.models.Chat;
import com.vjti.vjthrive.utils.CollegeDataProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CreateFacultyGroupActivity extends AppCompatActivity {

    private EditText etGroupTitle, etSubjectFilter;
    private ChipGroup cgProgrammes, cgDepts, cgBranches, cgYears;
    private TextView tvMatchingCount;
    private ExtendedFloatingActionButton fabCreateGroup;

    private final List<String> selectedProgrammes = new ArrayList<>();
    private final List<String> selectedDepts = new ArrayList<>();
    private final List<String> selectedBranches = new ArrayList<>();
    private final List<String> selectedYears = new ArrayList<>();

    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_faculty_group);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        initViews();
        setupMultiSelectListeners();

        fabCreateGroup.setOnClickListener(v -> createGroup());
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbarFacultyGroup);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        etGroupTitle = findViewById(R.id.etGroupTitle);
        etSubjectFilter = findViewById(R.id.etSubjectFilter);
        cgProgrammes = findViewById(R.id.cgProgrammes);
        cgDepts = findViewById(R.id.cgDepts);
        cgBranches = findViewById(R.id.cgBranches);
        cgYears = findViewById(R.id.cgYears);
        tvMatchingCount = findViewById(R.id.tvMatchingCount);
        fabCreateGroup = findViewById(R.id.fabCreateGroup);
    }

    private void setupMultiSelectListeners() {
        // Programmes
        MaterialButton btnProgrammes = findViewById(R.id.btnSelectProgrammes);
        btnProgrammes.setOnClickListener(v -> {
            String[] options = CollegeDataProvider.getProgrammes();
            showMultiSelectDialog("Programmes", options, selectedProgrammes, cgProgrammes);
        });

        // Departments — show all departments regardless of programme selection
        MaterialButton btnDepts = findViewById(R.id.btnSelectDepts);
        btnDepts.setOnClickListener(v -> {
            List<String> allDepts = CollegeDataProvider.getAllDepartments();
            showMultiSelectDialog("Departments", allDepts.toArray(new String[0]), selectedDepts, cgDepts);
        });

        // Branches — collect from all departments
        MaterialButton btnBranches = findViewById(R.id.btnSelectBranches);
        btnBranches.setOnClickListener(v -> {
            Set<String> branchSet = new HashSet<>();
            for (String dept : CollegeDataProvider.getAllDepartments()) {
                branchSet.addAll(CollegeDataProvider.getBranches(dept));
            }
            List<String> sortedBranches = new ArrayList<>(branchSet);
            Collections.sort(sortedBranches);
            showMultiSelectDialog("Branches", sortedBranches.toArray(new String[0]), selectedBranches, cgBranches);
        });

        // Graduation Years — dynamic range
        MaterialButton btnYears = findViewById(R.id.btnSelectYears);
        btnYears.setOnClickListener(v -> {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            String[] yearOptions = new String[6];
            for (int i = 0; i < 6; i++) {
                yearOptions[i] = String.valueOf(currentYear + i);
            }
            showMultiSelectDialog("Graduation Years", yearOptions, selectedYears, cgYears);
        });
    }

    private void showMultiSelectDialog(String title, String[] options,
                                       List<String> selectionList, ChipGroup chipGroup) {
        boolean[] checkedItems = new boolean[options.length];
        for (int i = 0; i < options.length; i++) {
            checkedItems[i] = selectionList.contains(options[i]);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select " + title)
                .setMultiChoiceItems(options, checkedItems, (dialog, which, isChecked) ->
                        checkedItems[which] = isChecked)
                .setPositiveButton("Done", (dialog, which) -> {
                    selectionList.clear();
                    chipGroup.removeAllViews();
                    for (int i = 0; i < options.length; i++) {
                        if (checkedItems[i]) {
                            selectionList.add(options[i]);
                            addChip(options[i], chipGroup, selectionList);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addChip(String text, ChipGroup chipGroup, List<String> selectionList) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            selectionList.remove(text);
        });
        chipGroup.addView(chip);
    }

    private void createGroup() {
        String title = etGroupTitle.getText().toString().trim();
        String subjectFilter = etSubjectFilter.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etGroupTitle.setError("Title required");
            return;
        }

        fabCreateGroup.setEnabled(false);
        Toast.makeText(this, "Finding matching students...", Toast.LENGTH_SHORT).show();

        // Fetch ALL students — filter client-side to support multiple selections
        // (Firestore doesn't support multiple whereIn on different fields)
        db.collection("users").whereEqualTo("role", "student")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> memberIds = new ArrayList<>();
                    memberIds.add(currentUserId); // faculty is always a member

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Programme filter
                        if (!selectedProgrammes.isEmpty()) {
                            String prog = doc.getString("programme");
                            if (prog == null || !selectedProgrammes.contains(prog)) continue;
                        }
                        // Department filter
                        if (!selectedDepts.isEmpty()) {
                            String dept = doc.getString("department");
                            if (dept == null || !selectedDepts.contains(dept)) continue;
                        }
                        // Branch filter
                        if (!selectedBranches.isEmpty()) {
                            String branch = doc.getString("branch");
                            if (branch == null || !selectedBranches.contains(branch)) continue;
                        }
                        // Graduation Year filter
                        if (!selectedYears.isEmpty()) {
                            Long gradYear = doc.getLong("graduationYear");
                            String gradYearStr = gradYear != null ? String.valueOf(gradYear) : "";
                            if (!selectedYears.contains(gradYearStr)) continue;
                        }
                        // MDM Subject filter (substring match)
                        if (!subjectFilter.isEmpty()) {
                            String mdmSubject = doc.getString("mdmSubject");
                            if (mdmSubject == null || !mdmSubject.toLowerCase()
                                    .contains(subjectFilter.toLowerCase())) continue;
                        }

                        memberIds.add(doc.getId());
                    }

                    int studentCount = memberIds.size() - 1;
                    tvMatchingCount.setText("Found " + studentCount + " matching student(s)");

                    if (studentCount == 0) {
                        fabCreateGroup.setEnabled(true);
                        Toast.makeText(this, "No matching students found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    saveChatToFirestore(title, memberIds);
                })
                .addOnFailureListener(e -> {
                    fabCreateGroup.setEnabled(true);
                    Toast.makeText(this, "Query failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveChatToFirestore(String title, List<String> members) {
        String chatId = UUID.randomUUID().toString();
        Chat chat = new Chat(
                chatId, title,
                new ArrayList<>(selectedProgrammes),
                new ArrayList<>(selectedDepts),
                new ArrayList<>(selectedBranches),
                new ArrayList<>(selectedYears),
                currentUserId, members, true
        );

        db.collection("chats").document(chatId).set(chat)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Group created with " + (members.size() - 1) + " students",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    fabCreateGroup.setEnabled(true);
                    Toast.makeText(this, "Failed to save group", Toast.LENGTH_SHORT).show();
                });
    }
}
