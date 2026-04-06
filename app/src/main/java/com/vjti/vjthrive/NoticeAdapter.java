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
    private String userRole;
    private OnNoticeClickListener clickListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public interface OnNoticeClickListener {
        void onDeleteClick(Notice notice, int position);
    }

    public NoticeAdapter(List<Notice> noticeList, String userRole, OnNoticeClickListener clickListener) {
        this.noticeList = noticeList;
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
        notifyDataSetChanged();
    }

    static class NoticeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvAuthor, tvDate;
        ImageView ivOptions;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoticeTitle);
            tvContent = itemView.findViewById(R.id.tvNoticeContent);
            tvAuthor = itemView.findViewById(R.id.tvNoticeAuthor);
            tvDate = itemView.findViewById(R.id.tvNoticeDate);
            ivOptions = itemView.findViewById(R.id.ivNoticeOptions);
        }
    }
}
