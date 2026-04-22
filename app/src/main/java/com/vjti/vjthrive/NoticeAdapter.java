package com.vjti.vjthrive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vjti.vjthrive.models.Notice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {

    private List<Notice> noticeList;
    private List<Notice> noticeListFull;
    private String userRole;
    private OnNoticeClickListener clickListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public interface OnNoticeClickListener {
        void onDeleteClick(Notice notice, int position);
    }

    public NoticeAdapter(List<Notice> noticeList, String userRole, OnNoticeClickListener clickListener) {
        this.noticeList = noticeList;
        this.noticeListFull = new java.util.ArrayList<>(noticeList);
        this.userRole = userRole;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        Notice notice = noticeList.get(position);
        holder.tvTitle.setText(notice.getTitle());
        holder.tvContent.setText(notice.getContent());
        holder.tvAuthor.setText("Posted by: " + notice.getAuthor());
        holder.tvDate.setText(dateFormat.format(new Date(notice.getTimestamp())));

        // Handle Attachments
        String attachment = notice.getAttachment();
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
                    // Cloudinary trick: Use pg_1 (page 1) and change extension to .jpg for a high-quality preview
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
                popupMenu.inflate(R.menu.notice_options_menu); // We need to create this menu
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_delete) {
                        if (clickListener != null) {
                            clickListener.onDeleteClick(notice, position);
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
        return noticeList.size();
    }

    public void updateData(List<Notice> newNotices) {
        this.noticeList.clear();
        this.noticeList.addAll(newNotices);
        this.noticeListFull = new java.util.ArrayList<>(newNotices);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        noticeList.clear();
        if (query == null || query.trim().isEmpty()) {
            noticeList.addAll(noticeListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
            for (Notice notice : noticeListFull) {
                boolean matchesTitle = notice.getTitle() != null && notice.getTitle().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);
                boolean matchesContent = notice.getContent() != null && notice.getContent().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);
                boolean matchesAuthor = notice.getAuthor() != null && notice.getAuthor().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);
                
                if (matchesTitle || matchesContent || matchesAuthor) {
                    noticeList.add(notice);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class NoticeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvContent, tvDate;
        android.widget.ImageView ivOptions, ivAttachment;
        android.widget.Button btnViewAttachment;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoticeTitle);
            tvAuthor = itemView.findViewById(R.id.tvNoticeAuthor);
            tvContent = itemView.findViewById(R.id.tvNoticeContent);
            tvDate = itemView.findViewById(R.id.tvNoticeDate);
            ivOptions = itemView.findViewById(R.id.ivNoticeOptions);
            ivAttachment = itemView.findViewById(R.id.ivNoticeAttachment);
            btnViewAttachment = itemView.findViewById(R.id.btnViewAttachment);
        }
    }
}
