package com.vjti.vjthrive;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.vjti.vjthrive.models.ClubEvent;
import com.vjti.vjthrive.models.User;

import java.util.ArrayList;
import java.util.List;

public class ClubEventsFragment extends Fragment implements ClubEventAdapter.OnClubEventClickListener {

    private static final String TAG = "ClubEventsFragment";

    private RecyclerView rvClubEvents;
    private TextView tvEmptyClubEvents;
    private FloatingActionButton fabAddClubEvent;

    private ClubEventAdapter adapter;
    private List<ClubEvent> clubEventList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private User currentUserProfile;

    public ClubEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_club_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvClubEvents = view.findViewById(R.id.rvClubEvents);
        tvEmptyClubEvents = view.findViewById(R.id.tvEmptyClubEvents);
        fabAddClubEvent = view.findViewById(R.id.fabAddClubEvent);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        clubEventList = new ArrayList<>();

        rvClubEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        fabAddClubEvent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddClubEventActivity.class);
            startActivity(intent);
        });

        fetchUserRoleAndEvents();
    }

    private void fetchUserRoleAndEvents() {
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
                        adapter = new ClubEventAdapter(new ArrayList<>(), role, this);
                        rvClubEvents.setAdapter(adapter);

                        // Show/Hide FAB - Secretaties and Admin can add
                        boolean isSecretary = currentUserProfile.isSecretary();
                        if ("admin".equalsIgnoreCase(role) || isSecretary) {
                            fabAddClubEvent.setVisibility(View.VISIBLE);
                        } else {
                            fabAddClubEvent.setVisibility(View.GONE);
                        }

                        // Fetch Events
                        fetchClubEvents();
                    }
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error fetching user profile", e));
    }

    private void fetchClubEvents() {
        db.collection("club_events")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                clubEventList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    ClubEvent event = doc.toObject(ClubEvent.class);
                    event.setId(doc.getId());
                    clubEventList.add(event);
                }

                adapter.updateData(clubEventList);

                if (clubEventList.isEmpty()) {
                    tvEmptyClubEvents.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyClubEvents.setVisibility(View.GONE);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching club events", e);
                Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onDeleteClick(ClubEvent clubEvent, int position) {
        if (clubEvent.getId() == null) return;

        db.collection("club_events").document(clubEvent.getId()).delete()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Update deleted", Toast.LENGTH_SHORT).show();
                fetchClubEvents(); // Refresh list
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error deleting club event", e);
            });
    }
}
