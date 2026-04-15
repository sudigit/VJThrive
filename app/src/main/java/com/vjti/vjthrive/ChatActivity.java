package com.vjti.vjthrive;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.vjti.vjthrive.models.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText etMessageInput;
    private ImageButton btnSendMessage;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String currentUserId;
    private String chatId;
    private String chatName;
    private CollectionReference messagesRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId = getIntent().getStringExtra("CHAT_ID");
        chatName = getIntent().getStringExtra("CHAT_NAME");

        if (chatId == null) {
            Toast.makeText(this, "Error: Chat ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(chatName != null ? chatName : "Chat");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewMessages);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            finish(); // Should not happen
            return;
        }

        messagesRef = db.collection("chats").document(chatId).collection("messages");

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(messageAdapter);

        loadMessages();

        btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        progressBar.setVisibility(View.VISIBLE);
        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener((value, error) -> {
            progressBar.setVisibility(View.GONE);
            if (error != null) {
                Toast.makeText(this, "Error loading messages.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                for (DocumentChange dc : value.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        Message message = dc.getDocument().toObject(Message.class);
                        messageList.add(message);
                        messageAdapter.notifyItemInserted(messageList.size() - 1);
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    }
                }
            }
        });
    }

    private void sendMessage() {
        String text = etMessageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        etMessageInput.setText("");

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("text", text);
        messageMap.put("senderId", currentUserId);
        messageMap.put("timestamp", FieldValue.serverTimestamp());

        messagesRef.add(messageMap).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
        });
    }
}
