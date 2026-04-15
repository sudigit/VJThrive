package com.vjti.vjthrive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vjti.vjthrive.models.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserSelectionAdapter extends RecyclerView.Adapter<UserSelectionAdapter.UserViewHolder> {

    private List<User> userList;
    private Set<String> selectedUserIds = new HashSet<>();
    private OnUserSelectedListener listener;

    public interface OnUserSelectedListener {
        void onUserSelectionChanged(Set<String> selectedIds);
    }

    public UserSelectionAdapter(List<User> userList, OnUserSelectedListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_selection, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvName.setText(user.getName());
        
        String info = (user.getBranch() != null ? user.getBranch() : "") + 
                     (user.getGraduationYear() > 0 ? " | " + user.getGraduationYear() : "");
        holder.tvInfo.setText(info);

        holder.cbSelected.setChecked(selectedUserIds.contains(user.getUid()));

        View.OnClickListener toggleSelection = v -> {
            String uid = user.getUid();
            if (uid == null) return;

            if (selectedUserIds.contains(uid)) {
                selectedUserIds.remove(uid);
            } else {
                selectedUserIds.add(uid);
            }
            holder.cbSelected.setChecked(selectedUserIds.contains(uid));
            if (listener != null) {
                listener.onUserSelectionChanged(selectedUserIds);
            }
        };

        holder.itemView.setOnClickListener(toggleSelection);
        holder.cbSelected.setOnClickListener(toggleSelection);
    }

    public Set<String> getSelectedUserIds() {
        return selectedUserIds;
    }
    
    // I should probably update the User model to have an ID field.
    // Let me check User model again.
    
    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateData(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvInfo;
        CheckBox cbSelected;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvInfo = itemView.findViewById(R.id.tvUserInfo);
            cbSelected = itemView.findViewById(R.id.cbSelected);
        }
    }
}
