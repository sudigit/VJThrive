package com.vjti.vjthrive;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vjti.vjthrive.models.Notice;
import com.vjti.vjthrive.models.User;
import com.vjti.vjthrive.utils.CollegeDataProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AddNoticeActivity extends AppCompatActivity {

    private static final String TAG = "AddNoticeActivity";

    private TextInputEditText etTitle, etContent;
    private android.widget.Button btnPickFile;
    private android.widget.TextView tvAttachmentStatus;
    private ChipGroup cgProgrammes, cgDepts, cgBranches, cgYears;
    private ExtendedFloatingActionButton fabSend;

    private String attachmentUrl = "";
    private boolean isUploading = false;

    private List<String> selectedProgrammes = new ArrayList<>();
    private List<String> selectedDepts = new ArrayList<>();
    private List<String> selectedBranches = new ArrayList<>();
    private List<String> selectedYears = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String authorName = "Anonymous";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_notice);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        fetchAuthorName();
        setupMultiSelectListeners();

        fabSend.setOnClickListener(v -> postNotice());
        btnPickFile.setOnClickListener(v -> pickFile());
    }

    private final androidx.activity.result.ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    uploadFileToCloudinary(uri);
                }
            });

    private void pickFile() {
        filePickerLauncher.launch("*/*");
    }

    private void uploadFileToCloudinary(android.net.Uri uri) {
        isUploading = true;
        tvAttachmentStatus.setText("Uploading...");
        btnPickFile.setEnabled(false);
        fabSend.setEnabled(false);

        com.vjti.vjthrive.utils.CloudinaryHelper.uploadFile(uri, "notice",
                new com.vjti.vjthrive.utils.CloudinaryHelper.UploadListener() {
                    @Override
                    public void onSuccess(String url) {
                        isUploading = false;
                        attachmentUrl = url;
                        tvAttachmentStatus.setText("File attached!");
                        btnPickFile.setEnabled(true);
                        fabSend.setEnabled(true);
                    }

                    @Override
                    public void onError(String error) {
                        isUploading = false;
                        tvAttachmentStatus.setText("Upload failed: " + error);
                        btnPickFile.setEnabled(true);
                        fabSend.setEnabled(true);
                        Toast.makeText(AddNoticeActivity.this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnPickFile = findViewById(R.id.btnPickFile);
        tvAttachmentStatus = findViewById(R.id.tvAttachmentStatus);
        cgProgrammes = findViewById(R.id.cgProgrammes);
        cgDepts = findViewById(R.id.cgDepts);
        cgBranches = findViewById(R.id.cgBranches);
        cgYears = findViewById(R.id.cgYears);
        fabSend = findViewById(R.id.fabSend);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom); // Apply top inset padding
            return insets;
        });
    }

    private void fetchAuthorName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User profile = documentSnapshot.toObject(User.class);
                            if (profile != null && profile.getName() != null) {
                                authorName = profile.getName();
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching user name", e));
        }
    }

    private void setupMultiSelectListeners() {
        findViewById(R.id.btnSelectProgrammes).setOnClickListener(v -> {
            String[] commonProgrammes = CollegeDataProvider.getProgrammes();
            showMultiSelectDialog("Programmes", commonProgrammes, selectedProgrammes, cgProgrammes);
        });

        findViewById(R.id.btnSelectDepts).setOnClickListener(v -> {
            List<String> allDepts = CollegeDataProvider.getAllDepartments();
            showMultiSelectDialog("Departments", allDepts.toArray(new String[0]), selectedDepts, cgDepts);
        });

        findViewById(R.id.btnSelectBranches).setOnClickListener(v -> {
            Set<String> branchSet = new HashSet<>();
            for (String dept : CollegeDataProvider.getAllDepartments()) {
                branchSet.addAll(CollegeDataProvider.getBranches(dept));
            }
            List<String> sortedBranches = new ArrayList<>(branchSet);
            Collections.sort(sortedBranches);
            showMultiSelectDialog("Branches", sortedBranches.toArray(new String[0]), selectedBranches, cgBranches);
        });

        findViewById(R.id.btnSelectYears).setOnClickListener(v -> {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            String[] yearOptions = new String[6];
            for (int i = 0; i < 6; i++) {
                yearOptions[i] = String.valueOf(currentYear + i);
            }
            showMultiSelectDialog("Graduation Years", yearOptions, selectedYears, cgYears);
        });
    }

    private void showMultiSelectDialog(String title, String[] options, List<String> selectionList,
            ChipGroup chipGroup) {
        boolean[] checkedItems = new boolean[options.length];
        for (int i = 0; i < options.length; i++) {
            checkedItems[i] = selectionList.contains(options[i]);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select " + title)
                .setMultiChoiceItems(options, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
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

    private void postNotice() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (isUploading) {
            Toast.makeText(this, "Please wait for upload to complete", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            return;
        }
        if (TextUtils.isEmpty(content)) {
            etContent.setError("Content is required");
            return;
        }

        fabSend.setEnabled(false);
        Toast.makeText(this, "Posting notice...", Toast.LENGTH_SHORT).show();

        String noticeId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        Notice notice = new Notice(
                noticeId,
                authorName,
                title,
                content,
                attachmentUrl,
                timestamp,
                new ArrayList<>(selectedProgrammes),
                new ArrayList<>(selectedDepts),
                new ArrayList<>(selectedBranches),
                new ArrayList<>(selectedYears));

        db.collection("notices").document(noticeId)
                .set(notice)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddNoticeActivity.this, "Notice posted successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    fabSend.setEnabled(true);
                    Toast.makeText(AddNoticeActivity.this, "Failed to post: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
    }
}
