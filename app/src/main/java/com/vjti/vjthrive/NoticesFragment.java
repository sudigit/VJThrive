package com.vjti.vjthrive;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.vjti.vjthrive.models.Notice;
import com.vjti.vjthrive.models.User;

import java.util.ArrayList;
import java.util.List;

public class NoticesFragment extends Fragment implements NoticeAdapter.OnNoticeClickListener {

    private static final String TAG = "NoticesFragment";
    
    private RecyclerView rvNotices;
    private TextView tvEmptyNotices;
    private FloatingActionButton fabAddNotice;
    private EditText etSearchNotices;
    
    private NoticeAdapter adapter;
    private List<Notice> noticeList;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private User currentUserProfile;

    public NoticesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        rvNotices = view.findViewById(R.id.rvNotices);
        tvEmptyNotices = view.findViewById(R.id.tvEmptyNotices);
        fabAddNotice = view.findViewById(R.id.fabAddNotice);
        etSearchNotices = view.findViewById(R.id.etSearchNotices);
        
        etSearchNotices.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }
        });
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        noticeList = new ArrayList<>();
        
        rvNotices.setLayoutManager(new LinearLayoutManager(getContext()));
        
        fabAddNotice.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddNoticeActivity.class);
            startActivity(intent);
        });
        
        fetchUserRoleAndNotices();
    }
    
    private void fetchUserRoleAndNotices() {
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser == null) return;
        
        db.collection("users").document(fUser.getUid()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentUserProfile = documentSnapshot.toObject(User.class);
                    if (currentUserProfile != null) {
                        String role = currentUserProfile.getRole();
                        if (role == null) role = "student";
                        
                        // Setup adapter with role
                        adapter = new NoticeAdapter(new ArrayList<>(), role, this);
                        rvNotices.setAdapter(adapter);
                        
                        // Show/Hide FAB
                        if ("admin".equalsIgnoreCase(role) || "faculty".equalsIgnoreCase(role)) {
                            fabAddNotice.setVisibility(View.VISIBLE);
                        } else {
                            fabAddNotice.setVisibility(View.GONE);
                        }
                        
                        // Fetch Notices
                        fetchNotices(role);
                    }
                } else {
                    Log.e(TAG, "User document does not exist");
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error fetching user profile", e));
    }
    
    private void fetchNotices(String role) {
        db.collection("notices")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                noticeList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Notice notice = doc.toObject(Notice.class);
                    notice.setId(doc.getId());
                    
                    if (shouldShowNotice(notice, role)) {
                        noticeList.add(notice);
                    }
                }
                
                adapter.updateData(noticeList);
                
                if (noticeList.isEmpty()) {
                    tvEmptyNotices.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyNotices.setVisibility(View.GONE);
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error fetching notices", e));
    }
    
    private boolean shouldShowNotice(Notice notice, String role) {
        if ("admin".equalsIgnoreCase(role)) {
            return true; // Admins see everything
        }
        
        if ("faculty".equalsIgnoreCase(role)) {
            // Faculty see their department's notices or global notices
            if (notice.getTarget_dept() == null || notice.getTarget_dept().isEmpty()) {
                return true;
            }
            if (currentUserProfile.getDepartment() != null && 
                notice.getTarget_dept().contains(currentUserProfile.getDepartment())) {
                return true;
            }
            // Allow them to see notices they authored
            if (currentUserProfile.getName() != null && 
                currentUserProfile.getName().equals(notice.getAuthor())) {
                return true;
            }
            return false;
        }
        
        // Student logic
        // If target lists are completely empty/null, it's global
        boolean isGlobal = (notice.getTarget_programme() == null || notice.getTarget_programme().isEmpty()) &&
                           (notice.getTarget_dept() == null || notice.getTarget_dept().isEmpty()) &&
                           (notice.getTarget_branch() == null || notice.getTarget_branch().isEmpty()) &&
                           (notice.getTarget_year() == null || notice.getTarget_year().isEmpty());
                           
        if (isGlobal) return true;
        
        // Otherwise, check if student matches the criteria
        boolean matchesProgramme = isEmptyOrContains(notice.getTarget_programme(), currentUserProfile.getProgramme());
        boolean matchesDept = isEmptyOrContains(notice.getTarget_dept(), currentUserProfile.getDepartment());
        boolean matchesBranch = isEmptyOrContains(notice.getTarget_branch(), currentUserProfile.getBranch());
        
        String graduationYearStr = String.valueOf(currentUserProfile.getGraduationYear());
        boolean matchesYear = isEmptyOrContains(notice.getTarget_year(), graduationYearStr);
        
        return matchesProgramme && matchesDept && matchesBranch && matchesYear;
    }
    
    private boolean isEmptyOrContains(List<String> list, String value) {
        if (list == null || list.isEmpty()) return true;
        if (value == null) return false;
        return list.contains(value);
    }

    @Override
    public void onDeleteClick(Notice notice, int position) {
        if (notice.getId() == null) return;
        
        db.collection("notices").document(notice.getId()).delete()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Notice deleted", Toast.LENGTH_SHORT).show();
                fetchUserRoleAndNotices(); // Refresh list
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error deleting notice", e);
            });
    }
}
