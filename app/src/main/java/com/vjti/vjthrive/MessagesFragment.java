package com.vjti.vjthrive;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
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

public class MessagesFragment extends Fragment {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText etMessageInput;
    private ImageButton btnSendMessage;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String currentUserId;
    private CollectionReference messagesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        // Setup Toolbar
        Toolbar toolbarPath = view.findViewById(R.id.toolbarChat);
        toolbarPath.setTitle("Chat");

        recyclerView = view.findViewById(R.id.recyclerViewMessages);
        etMessageInput = view.findViewById(R.id.etMessageInput);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);
        progressBar = view.findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            currentUserId = ""; // fallback
        }

        messagesRef = db.collection("chats").document("chat1").collection("messages");

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // Start from bottom
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(messageAdapter);

        loadMessages();

        btnSendMessage.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void loadMessages() {
        progressBar.setVisibility(View.VISIBLE);
        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener((value, error) -> {
            progressBar.setVisibility(View.GONE);
            if (error != null) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading messages.", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (value != null) {
                for (DocumentChange dc : value.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            Message message = dc.getDocument().toObject(Message.class);
                            messageList.add(message);
                            messageAdapter.notifyItemInserted(messageList.size() - 1);
                            recyclerView.scrollToPosition(messageList.size() - 1);
                            break;
                        case MODIFIED:
                            // Handle modification if needed
                            break;
                        case REMOVED:
                            // Handle removal if needed
                            break;
                    }
                }
            }
        });
    }

    private void sendMessage() {
        String text = etMessageInput.getText().toString().trim();
        if (text.isEmpty() || currentUserId.isEmpty()) return;

        etMessageInput.setText("");

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("text", text);
        messageMap.put("senderId", currentUserId);
        messageMap.put("timestamp", FieldValue.serverTimestamp());

        messagesRef.add(messageMap).addOnFailureListener(e -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
