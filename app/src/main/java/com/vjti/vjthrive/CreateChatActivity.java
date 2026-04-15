package com.vjti.vjthrive;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vjti.vjthrive.models.Chat;
import com.vjti.vjthrive.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CreateChatActivity extends AppCompatActivity implements UserSelectionAdapter.OnUserSelectedListener {

    private EditText etSearchUser, etGroupName;
    private TextInputLayout tilGroupName;
    private Button btnFacultyGroup;
    private RecyclerView rvUserSelection;
    private ExtendedFloatingActionButton fabCreate;

    private UserSelectionAdapter adapter;
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();

    private FirebaseFirestore db;
    private String currentUserId;
    private String currentUserRole;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        initViews();
        fetchCurrentUserProfile();
        loadAllUsers();

        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnFacultyGroup.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateFacultyGroupActivity.class));
        });

        fabCreate.setOnClickListener(v -> createChat());
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbarCreateChat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        etSearchUser = findViewById(R.id.etSearchUser);
        etGroupName = findViewById(R.id.etGroupName);
        tilGroupName = findViewById(R.id.tilGroupName);
        btnFacultyGroup = findViewById(R.id.btnFacultyGroup);
        rvUserSelection = findViewById(R.id.rvUserSelection);
        fabCreate = findViewById(R.id.fabCreate);

        adapter = new UserSelectionAdapter(filteredUsers, this);
        rvUserSelection.setLayoutManager(new LinearLayoutManager(this));
        rvUserSelection.setAdapter(adapter);
    }

    private void fetchCurrentUserProfile() {
        if (currentUserId == null) return;
        db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                currentUserRole = doc.getString("role");
                currentUserName = doc.getString("name");
                if ("faculty".equalsIgnoreCase(currentUserRole)) {
                    btnFacultyGroup.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadAllUsers() {
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            allUsers.clear();
            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                User user = doc.toObject(User.class);
                user.setUid(doc.getId());
                if (!user.getUid().equals(currentUserId)) {
                    allUsers.add(user);
                }
            }
            filterUsers(""); // Initial load
        });
    }

    private void filterUsers(String query) {
        filteredUsers.clear();
        if (TextUtils.isEmpty(query)) {
            filteredUsers.addAll(allUsers);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User u : allUsers) {
                if (u.getName() != null && u.getName().toLowerCase().contains(lowerQuery)) {
                    filteredUsers.add(u);
                }
            }
        }
        adapter.updateData(filteredUsers);
    }

    @Override
    public void onUserSelectionChanged(Set<String> selectedIds) {
        if (selectedIds.size() > 1) {
            tilGroupName.setVisibility(View.VISIBLE);
        } else {
            tilGroupName.setVisibility(View.GONE);
        }
    }

    private void createChat() {
        Set<String> selectedIds = adapter.getSelectedUserIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Please select at least one person", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isGroup = selectedIds.size() > 1;
        String name = etGroupName.getText().toString().trim();

        if (isGroup && TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> members = new ArrayList<>(selectedIds);
        members.add(currentUserId);

        // For 1:1, use the other person's name
        if (!isGroup) {
            String otherUserId = members.get(0).equals(currentUserId) ? members.get(1) : members.get(0);
            for (User u : allUsers) {
                if (u.getUid().equals(otherUserId)) {
                    name = u.getName();
                    break;
                }
            }
        }

        final String finalChatId = UUID.randomUUID().toString();
        final String finalChatName = name;

        Chat chat = new Chat(finalChatId, finalChatName, null, null, null, null, currentUserId, members, isGroup);

        db.collection("chats").document(finalChatId).set(chat).addOnSuccessListener(aVoid -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("CHAT_ID", finalChatId);
            intent.putExtra("CHAT_NAME", finalChatName);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to create chat", Toast.LENGTH_SHORT).show();
        });
    }
}
