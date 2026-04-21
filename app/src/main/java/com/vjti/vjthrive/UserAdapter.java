package com.vjti.vjthrive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.vjti.vjthrive.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    public void updateList(List<User> newList) {
        this.users = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvDetails;
        Chip chipRole;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvDetails = itemView.findViewById(R.id.tvUserDetails);
            chipRole = itemView.findViewById(R.id.chipRole);
        }

        public void bind(User user, OnUserClickListener listener) {
            tvName.setText(user.getName());
            tvEmail.setText(user.getEmail());
            
            StringBuilder details = new StringBuilder();
            if (user.getProgramme() != null) details.append(user.getProgramme()).append(" | ");
            if (user.getDepartment() != null) details.append(user.getDepartment());
            if (user.isSecretary()) details.append(" | Secretary");
            if (user.getMdmSubject() != null && !user.getMdmSubject().isEmpty()) {
                details.append("\nMDM: ").append(user.getMdmSubject());
            }
            tvDetails.setText(details.toString());

            String role = user.getRole();
            chipRole.setText(role != null ? role.toUpperCase() : "STUDENT");

            itemView.setOnClickListener(v -> listener.onUserClick(user));
        }
    }
}
