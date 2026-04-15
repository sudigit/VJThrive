package com.vjti.vjthrive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vjti.vjthrive.models.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<Message> messageList;
    private String currentUserId;
    private boolean isGroup;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MessageAdapter(List<Message> messageList, String currentUserId, boolean isGroup) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.isGroup = isGroup;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.getSenderId() != null && message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(message, timeFormat);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message, isGroup, timeFormat);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageBody, tvMessageTime;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            tvMessageBody = itemView.findViewById(R.id.tvMessageBody);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }

        void bind(Message message, SimpleDateFormat timeFormat) {
            tvMessageBody.setText(message.getText());
            if (message.getTimestamp() != null && message.getTimestamp() instanceof com.google.firebase.Timestamp) {
                Date date = ((com.google.firebase.Timestamp) message.getTimestamp()).toDate();
                tvMessageTime.setText(timeFormat.format(date));
            } else {
                tvMessageTime.setText("...");
            }
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageBody, tvSenderName, tvMessageTime;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            tvMessageBody = itemView.findViewById(R.id.tvMessageBody);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }

        void bind(Message message, boolean isGroup, SimpleDateFormat timeFormat) {
            tvMessageBody.setText(message.getText());
            
            if (isGroup && message.getSenderName() != null) {
                tvSenderName.setVisibility(View.VISIBLE);
                tvSenderName.setText(message.getSenderName());
            } else {
                tvSenderName.setVisibility(View.GONE);
            }

            if (message.getTimestamp() != null && message.getTimestamp() instanceof com.google.firebase.Timestamp) {
                Date date = ((com.google.firebase.Timestamp) message.getTimestamp()).toDate();
                tvMessageTime.setText(timeFormat.format(date));
            } else {
                tvMessageTime.setText("");
            }
        }
    }
}
