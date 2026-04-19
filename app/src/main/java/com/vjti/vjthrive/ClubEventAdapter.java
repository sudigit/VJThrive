package com.vjti.vjthrive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.vjti.vjthrive.models.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClubEventAdapter extends RecyclerView.Adapter<ClubEventAdapter.ClubEventViewHolder> {

    private List<Event> eventList;
    private String userRole;
    private OnClubEventClickListener clickListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public interface OnClubEventClickListener {
        void onDeleteClick(Event event, int position);
    }

    public ClubEventAdapter(List<Event> eventList, String userRole, OnClubEventClickListener clickListener) {
        this.eventList = eventList;
        this.userRole = userRole;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ClubEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_club_event, parent, false);
        return new ClubEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClubEventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.tvClubName.setText(event.getClubId());
        holder.tvTitle.setText(event.getTitle());

        if (event.getEventDate() != null) {
            holder.tvEventDate.setText("Event Date: " + dateFormat.format(event.getEventDate().toDate()));
        } else {
            holder.tvEventDate.setText("No date set");
        }

        holder.tvContent.setText(event.getDescription());

        // Use event date as display date if created timestamp is missing
        if (event.getEventDate() != null) {
            holder.tvDate.setText(dateFormat.format(event.getEventDate().toDate()));
        }

        // Handle Attachments
        String attachment = event.getAttachment();
        if (attachment != null && !attachment.isEmpty()) {
            holder.btnViewAttachment.setVisibility(View.VISIBLE);
            
            boolean isPdf = attachment.toLowerCase().contains(".pdf");
            holder.btnViewAttachment.setText(isPdf ? "View PDF" : "View Image");

            holder.btnViewAttachment.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(attachment));
                v.getContext().startActivity(intent);
            });

            // If it's an image or PDF, show preview
            if (isPdf || attachment.contains(".jpg") || attachment.contains(".jpeg") || attachment.contains(".png") || attachment.contains(".webp") || attachment.contains("image/upload")) {
                holder.ivAttachment.setVisibility(View.VISIBLE);
                
                String previewUrl = attachment;
                if (isPdf && attachment.contains("/image/upload/")) {
                    previewUrl = attachment.replaceAll("(?i)\\.pdf", ".jpg").replace("/image/upload/", "/image/upload/pg_1/");
                }

                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(previewUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(holder.ivAttachment);
            } else {
                holder.ivAttachment.setVisibility(View.GONE);
            }
        } else {
            holder.ivAttachment.setVisibility(View.GONE);
            holder.btnViewAttachment.setVisibility(View.GONE);
        }

        // Show options menu only for admin
        if ("admin".equalsIgnoreCase(userRole)) {
            holder.ivOptions.setVisibility(View.VISIBLE);
            holder.ivOptions.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), holder.ivOptions);
                popupMenu.inflate(R.menu.club_event_options_menu);
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_delete) {
                        if (clickListener != null) {
                            clickListener.onDeleteClick(event, position);
                        }
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
            });
        } else {
            holder.ivOptions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void updateData(List<Event> newEvents) {
        this.eventList = newEvents;
        notifyDataSetChanged();
    }

    static class ClubEventViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvClubName, tvTitle, tvEventDate, tvContent, tvDate;
        android.widget.ImageView ivOptions, ivAttachment;
        android.widget.Button btnViewAttachment;

        public ClubEventViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvClubName = itemView.findViewById(R.id.tvClubName);
            tvTitle = itemView.findViewById(R.id.tvClubEventTitle);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvContent = itemView.findViewById(R.id.tvClubEventContent);
            tvDate = itemView.findViewById(R.id.tvClubEventDate);
            ivOptions = itemView.findViewById(R.id.ivClubEventOptions);
            ivAttachment = itemView.findViewById(R.id.ivClubEventAttachment);
            btnViewAttachment = itemView.findViewById(R.id.btnViewClubEventAttachment);
        }
    }
}
