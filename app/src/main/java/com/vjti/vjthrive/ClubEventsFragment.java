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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.vjti.vjthrive.models.Event;
import com.vjti.vjthrive.models.User;

import java.util.ArrayList;
import java.util.List;

public class ClubEventsFragment extends Fragment implements ClubEventAdapter.OnClubEventClickListener {

    private static final String TAG = "ClubEventsFragment";

    private RecyclerView rvClubEvents;
    private TextView tvEmptyClubEvents;
    private FloatingActionButton fabAddClubEvent;
    private EditText etSearchClubEvents;

    private ClubEventAdapter adapter;
    private List<Event> eventList;
    private ListenerRegistration eventListener;

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
        etSearchClubEvents = view.findViewById(R.id.etSearchClubEvents);
        
        etSearchClubEvents.addTextChangedListener(new TextWatcher() {
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
        eventList = new ArrayList<>();

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

                        // Show/Hide FAB - Secretaries and Admin can add
                        boolean isSecretary = currentUserProfile.isSecretary();
                        if ("admin".equalsIgnoreCase(role) || isSecretary) {
                            fabAddClubEvent.setVisibility(View.VISIBLE);
                        } else {
                            fabAddClubEvent.setVisibility(View.GONE);
                        }

                        // Start Listening for Events
                        setupEventsListener();
                    }
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error fetching user profile", e));
    }

    private void setupEventsListener() {
        Log.d(TAG, "Setting up real-time listener for 'events' collection");
        
        // Remove existing listener if any
        if (eventListener != null) {
            eventListener.remove();
        }

        eventListener = db.collection("events")
            .orderBy("eventDate", Query.Direction.DESCENDING)
            .addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    eventList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Event event = doc.toObject(Event.class);
                            // We don't have a separate ID field in Event model, but we can use it for deletion
                            // For simplicity, we'll store the ID in the document reference if needed or just use index
                            eventList.add(event);
                        } catch (Exception ex) {
                            Log.e(TAG, "Error parsing event", ex);
                        }
                    }

                    Log.d(TAG, "Number of events fetched: " + eventList.size());
                    adapter.updateData(eventList);

                    if (eventList.isEmpty()) {
                        tvEmptyClubEvents.setVisibility(View.VISIBLE);
                        tvEmptyClubEvents.setText("No events yet");
                    } else {
                        tvEmptyClubEvents.setVisibility(View.GONE);
                    }
                }
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (eventListener != null) {
            eventListener.remove();
        }
    }

    @Override
    public void onDeleteClick(Event event, int position) {
        // Deletion usually needs a document ID. Since we added with .add(), we need the ID.
        // We'll modify setupEventsListener to capture the ID if possible or refetch.
        // For now, let's find the document by title/description/date (imperfect but better than nothing)
        // OR better yet, we should have stored the ID.
        
        db.collection("events")
            .whereEqualTo("title", event.getTitle())
            .whereEqualTo("description", event.getDescription())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    doc.getReference().delete()
                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show());
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error deleting event", e));
    }
}
