package com.vjti.vjthrive;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.vjti.vjthrive.models.User;

import java.util.ArrayList;
import java.util.List;

public class AdminPanelActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {

    private RecyclerView rvUsers;
    private UserAdapter adapter;
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        db = FirebaseFirestore.getInstance();
        initViews();
        fetchUsers();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(filteredUsers, this);
        rvUsers.setAdapter(adapter);

        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchUsers() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allUsers.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    user.setUid(document.getId());
                    allUsers.add(user);
                }
                filteredUsers.clear();
                filteredUsers.addAll(allUsers);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filter(String text) {
        filteredUsers.clear();
        for (User user : allUsers) {
            if (user.getName().toLowerCase().contains(text.toLowerCase()) ||
                user.getEmail().toLowerCase().contains(text.toLowerCase())) {
                filteredUsers.add(user);
            }
        }
        adapter.updateList(filteredUsers);
    }

    @Override
    public void onUserClick(User user) {
        showEditDialog(user);
    }

    private void showEditDialog(User user) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_user, null);
        
        android.widget.TextView tvEmail = dialogView.findViewById(R.id.tvEmail);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etRole = dialogView.findViewById(R.id.etRole);
        EditText etRollNo = dialogView.findViewById(R.id.etRollNo);
        EditText etProgramme = dialogView.findViewById(R.id.etProgramme);
        EditText etDepartment = dialogView.findViewById(R.id.etDepartment);
        EditText etBranch = dialogView.findViewById(R.id.etBranch);
        EditText etGradYear = dialogView.findViewById(R.id.etGradYear);
        EditText etMdm = dialogView.findViewById(R.id.etMdmSubject);
        MaterialSwitch swSecretary = dialogView.findViewById(R.id.swSecretary);

        // References to TextInputLayouts for visibility toggling
        View tilRollNo = dialogView.findViewById(R.id.tilRollNo);
        View tilProgramme = dialogView.findViewById(R.id.tilProgramme);
        View tilBranch = dialogView.findViewById(R.id.tilBranch);
        View tilGradYear = dialogView.findViewById(R.id.tilGradYear);
        View tilMdmSubject = dialogView.findViewById(R.id.tilMdmSubject);
        View tilDepartment = dialogView.findViewById(R.id.tilDepartment);

        // Pre-fill data
        tvEmail.setText(user.getEmail());
        etName.setText(user.getName());
        etRole.setText(user.getRole() != null ? user.getRole() : "student");
        etRollNo.setText(user.getRollNo());
        etProgramme.setText(user.getProgramme());
        etDepartment.setText(user.getDepartment());
        etBranch.setText(user.getBranch());
        etGradYear.setText(user.getGraduationYear() > 0 ? String.valueOf(user.getGraduationYear()) : "");
        etMdm.setText(user.getMdmSubject());
        swSecretary.setChecked(user.isSecretary());

        String role = user.getRole() != null ? user.getRole() : "student";
        
        // Adjust visibility based on role
        if ("faculty".equals(role)) {
            tilRollNo.setVisibility(View.GONE);
            tilProgramme.setVisibility(View.GONE);
            tilBranch.setVisibility(View.GONE);
            tilGradYear.setVisibility(View.GONE);
            tilMdmSubject.setVisibility(View.GONE);
            swSecretary.setVisibility(View.GONE);
        } else if ("admin".equals(role)) {
            tilRollNo.setVisibility(View.GONE);
            tilProgramme.setVisibility(View.GONE);
            tilDepartment.setVisibility(View.GONE);
            tilBranch.setVisibility(View.GONE);
            tilGradYear.setVisibility(View.GONE);
            tilMdmSubject.setVisibility(View.GONE);
            swSecretary.setVisibility(View.GONE);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit User")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newRole = etRole.getText().toString().trim();
                    String newRollNo = etRollNo.getText().toString().trim();
                    String newProg = etProgramme.getText().toString().trim();
                    String newDept = etDepartment.getText().toString().trim();
                    String newBranch = etBranch.getText().toString().trim();
                    String gradYearStr = etGradYear.getText().toString().trim();
                    int newGradYear = gradYearStr.isEmpty() ? 0 : Integer.parseInt(gradYearStr);
                    String newMdm = etMdm.getText().toString().trim();
                    boolean newSecretary = swSecretary.isChecked();
                    
                    updateUser(user, newName, newRole, newRollNo, newProg, newDept, newBranch, newGradYear, newMdm, newSecretary);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUser(User user, String name, String role, String rollNo, String prog, String dept, String branch, int gradYear, String mdm, boolean secretary) {
        db.collection("users").document(user.getUid())
                .update(
                    "name", name,
                    "role", role,
                    "rollNo", rollNo,
                    "programme", prog,
                    "department", dept,
                    "branch", branch,
                    "graduationYear", gradYear,
                    "mdmSubject", mdm, 
                    "isSecretary", secretary
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show();
                    user.setName(name);
                    user.setRole(role);
                    user.setRollNo(rollNo);
                    user.setProgramme(prog);
                    user.setDepartment(dept);
                    user.setBranch(branch);
                    user.setGraduationYear(gradYear);
                    user.setMdmSubject(mdm);
                    user.setSecretary(secretary);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
    }
}
