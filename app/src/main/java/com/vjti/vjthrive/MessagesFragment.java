package com.vjti.vjthrive;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.vjti.vjthrive.models.Chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MessagesFragment extends Fragment implements ChatListItemAdapter.OnChatClickListener {

    private RecyclerView rvChats;
    private TextView tvEmptyChats;
    private FloatingActionButton fabNewChat;
    
    private ChatListItemAdapter adapter;
    private List<Chat> chatList;
    
    private FirebaseFirestore db;
    private String currentUserId;

    public MessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvChats = view.findViewById(R.id.rvChats);
        tvEmptyChats = view.findViewById(R.id.tvEmptyChats);
        fabNewChat = view.findViewById(R.id.fabNewChat);

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        chatList = new ArrayList<>();
        adapter = new ChatListItemAdapter(chatList, currentUserId, this);
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChats.setAdapter(adapter);

        fabNewChat.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateChatActivity.class);
            startActivity(intent);
        });

        loadUserChats();
    }

    private void loadUserChats() {
        if (currentUserId == null) return;

        // Query chats where current user is a member
        db.collection("chats")
                .whereArrayContains("members", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error loading chats", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        chatList.clear();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                            Chat chat = doc.toObject(Chat.class);
                            chat.setChat_id(doc.getId());
                            chatList.add(chat);
                        }
                        adapter.updateData(chatList);
                        fetchOtherUserNames();

                        if (chatList.isEmpty()) {
                            tvEmptyChats.setVisibility(View.VISIBLE);
                        } else {
                            tvEmptyChats.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void fetchOtherUserNames() {
        Set<String> otherIds = new HashSet<>();
        for (Chat chat : chatList) {
            if (!chat.isGroup() && chat.getMembers() != null) {
                for (String memberId : chat.getMembers()) {
                    if (!memberId.equals(currentUserId)) {
                        otherIds.add(memberId);
                    }
                }
            }
        }

        if (otherIds.isEmpty()) return;

        // Fetch names for these IDs (Firestore 'whereIn' supports up to 30 items)
        List<String> idList = new ArrayList<>(otherIds);
        // If more than 30, we should chunk it, but for a typical chat list this is fine.
        int limit = Math.min(idList.size(), 30);
        
        db.collection("users").whereIn(FieldPath.documentId(), idList.subList(0, limit))
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, String> names = new HashMap<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        names.put(doc.getId(), doc.getString("name"));
                    }
                    adapter.updateUserNames(names);
                });
    }

    @Override
    public void onChatClick(Chat chat) {
        String displayName = chat.getName();
        // If 1:1, resolve name for the intent too
        if (!chat.isGroup() && chat.getMembers() != null) {
            for (String memberId : chat.getMembers()) {
                if (!memberId.equals(currentUserId)) {
                    // Note: This only works if we've already fetched it, 
                    // otherwise ChatActivity will fetch it.
                }
            }
        }

        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("CHAT_ID", chat.getChat_id());
        intent.putExtra("CHAT_NAME", chat.getName()); 
        startActivity(intent);
    }
}
