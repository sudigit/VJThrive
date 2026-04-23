package com.vjti.vjthrive;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.vjti.vjthrive.utils.CollegeDataProvider;

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
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
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
        AutoCompleteTextView actvProgramme = dialogView.findViewById(R.id.actvProgramme);
        AutoCompleteTextView actvDepartment = dialogView.findViewById(R.id.actvDepartment);
        AutoCompleteTextView actvBranch = dialogView.findViewById(R.id.actvBranch);
        AutoCompleteTextView actvGradYear = dialogView.findViewById(R.id.actvGradYear);
        EditText etMdm = dialogView.findViewById(R.id.etMdmSubject);
        MaterialSwitch swSecretary = dialogView.findViewById(R.id.swSecretary);

        // Visibility containers
        View tilRollNo = dialogView.findViewById(R.id.tilRollNo);
        View tilProgramme = dialogView.findViewById(R.id.tilProgramme);
        View tilDepartment = dialogView.findViewById(R.id.tilDepartment);
        View tilBranch = dialogView.findViewById(R.id.tilBranch);
        View tilGradYear = dialogView.findViewById(R.id.tilGradYear);
        View tilMdmSubject = dialogView.findViewById(R.id.tilMdmSubject);

        String role = user.getRole() != null ? user.getRole() : "student";

        // ── Department adapter — always starts with all departments ───────
        List<String> allDepts = CollegeDataProvider.getAllDepartments();
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, new ArrayList<>(allDepts));
        actvDepartment.setAdapter(deptAdapter);

        // ── Branch adapter ────────────────────────────────────────────────
        ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        actvBranch.setAdapter(branchAdapter);

        // ── Grad Year adapter ─────────────────────────────────────────────
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        actvGradYear.setAdapter(yearAdapter);

        if ("student".equals(role)) {
            // ── Student: Programme dropdown with cascading ────────────────
            String[] programmes = CollegeDataProvider.getProgrammes();
            actvProgramme.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, programmes));

            actvProgramme.setOnItemClickListener((parent, v, position, id) -> {
                String selectedProg = programmes[position];
                List<String> depts = CollegeDataProvider.getDepartments(selectedProg);
                deptAdapter.clear();
                deptAdapter.addAll(depts);
                deptAdapter.notifyDataSetChanged();
                actvDepartment.setText("", false);
                branchAdapter.clear();
                branchAdapter.notifyDataSetChanged();
                actvBranch.setText("", false);
                List<String> years = CollegeDataProvider.getGraduationYears(selectedProg);
                yearAdapter.clear();
                yearAdapter.addAll(years);
                yearAdapter.notifyDataSetChanged();
                actvGradYear.setText("", false);
            });

            // Pre-fill programme and prime cascaded lists
            if (user.getProgramme() != null && !user.getProgramme().isEmpty()) {
                actvProgramme.setText(user.getProgramme(), false);
                List<String> depts = CollegeDataProvider.getDepartments(user.getProgramme());
                deptAdapter.clear();
                deptAdapter.addAll(depts);
                deptAdapter.notifyDataSetChanged();
                List<String> years = CollegeDataProvider.getGraduationYears(user.getProgramme());
                yearAdapter.clear();
                yearAdapter.addAll(years);
                yearAdapter.notifyDataSetChanged();
            }
        }

        // ── Department → Branch cascade (applies to both roles) ───────────
        actvDepartment.setOnItemClickListener((parent, v, position, id) -> {
            String selectedDept = actvDepartment.getText().toString();
            List<String> branches = CollegeDataProvider.getBranches(selectedDept);
            branchAdapter.clear();
            branchAdapter.addAll(branches);
            branchAdapter.notifyDataSetChanged();
            actvBranch.setText("", false);
        });

        // Pre-fill department (works for both student and faculty)
        if (user.getDepartment() != null && !user.getDepartment().isEmpty()) {
            actvDepartment.setText(user.getDepartment(), false);
            List<String> branches = CollegeDataProvider.getBranches(user.getDepartment());
            branchAdapter.clear();
            branchAdapter.addAll(branches);
            branchAdapter.notifyDataSetChanged();
        }
        if (user.getBranch() != null && !user.getBranch().isEmpty())
            actvBranch.setText(user.getBranch(), false);
        if (user.getGraduationYear() > 0)
            actvGradYear.setText(String.valueOf(user.getGraduationYear()), false);

        // ── Pre-fill simple fields ────────────────────────────────────────
        tvEmail.setText(user.getEmail());
        etName.setText(user.getName());
        etRole.setText(role);
        etRollNo.setText(user.getRollNo());
        etMdm.setText(user.getMdmSubject());
        swSecretary.setChecked(user.isSecretary());

        // ── Adjust visibility per role ────────────────────────────────────
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
                    String newProg = actvProgramme.getText().toString().trim();
                    String newDept = actvDepartment.getText().toString().trim();
                    String newBranch = actvBranch.getText().toString().trim();
                    String gradYearStr = actvGradYear.getText().toString().trim();
                    int newGradYear = gradYearStr.isEmpty() ? 0 : Integer.parseInt(gradYearStr);
                    String newMdm = etMdm.getText().toString().trim();
                    boolean newSecretary = swSecretary.isChecked();

                    updateUser(user, newName, newRole, newRollNo, newProg, newDept,
                            newBranch, newGradYear, newMdm, newSecretary);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUser(User user, String name, String role, String rollNo,
                            String prog, String dept, String branch,
                            int gradYear, String mdm, boolean secretary) {
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
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
    }
}
