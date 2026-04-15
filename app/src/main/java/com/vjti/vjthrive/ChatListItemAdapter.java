package com.vjti.vjthrive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vjti.vjthrive.models.Chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatListItemAdapter extends RecyclerView.Adapter<ChatListItemAdapter.ChatViewHolder> {

    private List<Chat> chatList;
    private OnChatClickListener listener;
    private String currentUserId;
    private Map<String, String> userNames = new HashMap<>();

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatListItemAdapter(List<Chat> chatList, String currentUserId, OnChatClickListener listener) {
        this.chatList = chatList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_list, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        
        String displayName = chat.getName();
        if (!chat.isGroup() && chat.getMembers() != null) {
            // Find the other member
            for (String memberId : chat.getMembers()) {
                if (!memberId.equals(currentUserId)) {
                    String otherName = userNames.get(memberId);
                    if (otherName != null) {
                        displayName = otherName;
                    } else {
                        displayName = "Chat with " + memberId.substring(0, Math.min(memberId.length(), 5));
                    }
                    break;
                }
            }
        }

        holder.tvName.setText(displayName);
        holder.tvLastMessage.setText("Tap to view conversation"); 
        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void updateData(List<Chat> newList) {
        this.chatList = newList;
        notifyDataSetChanged();
    }

    public void updateUserNames(Map<String, String> newUserNames) {
        this.userNames.putAll(newUserNames);
        notifyDataSetChanged();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChatName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
        }
    }
}
