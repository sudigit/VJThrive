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
    private List<Event> eventListFull;
    private String userRole;
    private OnClubEventClickListener clickListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    private SimpleDateFormat eventDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timestampFormat = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());

    public interface OnClubEventClickListener {
        void onDeleteClick(Event event, int position);
    }

    public ClubEventAdapter(List<Event> eventList, String userRole, OnClubEventClickListener clickListener) {
        this.eventList = eventList;
        this.eventListFull = new java.util.ArrayList<>(eventList);
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
            holder.tvEventDate.setText("Event Date: " + eventDateFormat.format(event.getEventDate().toDate()));
        } else {
            holder.tvEventDate.setText("No date set");
        }

        holder.tvContent.setText(event.getDescription());

        // Show posted-at timestamp at the bottom (createdAt preferred, fallback to eventDate for old records)
        if (event.getCreatedAt() != null) {
            holder.tvDate.setText("Posted: " + timestampFormat.format(event.getCreatedAt().toDate()));
        } else if (event.getEventDate() != null) {
            holder.tvDate.setText("Posted: " + timestampFormat.format(event.getEventDate().toDate()));
        } else {
            holder.tvDate.setVisibility(View.GONE);
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
        this.eventList = new java.util.ArrayList<>(newEvents);
        this.eventListFull = new java.util.ArrayList<>(newEvents);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        eventList.clear();
        if (query == null || query.trim().isEmpty()) {
            eventList.addAll(eventListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
            for (Event event : eventListFull) {
                boolean matchesTitle = event.getTitle() != null && event.getTitle().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);
                boolean matchesDescription = event.getDescription() != null && event.getDescription().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);
                boolean matchesClub = event.getClubId() != null && event.getClubId().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);
                
                if (matchesTitle || matchesDescription || matchesClub) {
                    eventList.add(event);
                }
            }
        }
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
