package com.vjti.vjthrive;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.vjti.vjthrive.models.User;
import com.vjti.vjthrive.utils.CloudinaryHelper;
import com.vjti.vjthrive.utils.NotificationHelper;

import com.cloudinary.android.MediaManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        startTime = System.currentTimeMillis();

        // 2. Session Check: If not logged in, boot them to Login
        if (currentUser == null) {
            goToLogin();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 3. Handle System Bars (Status bar/Navigation bar padding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 4. Logout Logic
        ImageView btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                goToLogin();
            });
        }
        
        // 5. Setup Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_notices) {
                selectedFragment = new NoticesFragment();
            } else if (itemId == R.id.nav_clubs) {
                selectedFragment = new ClubEventsFragment();
            } else if (itemId == R.id.nav_calendar) {
                selectedFragment = new CalendarFragment();
            } else if (itemId == R.id.nav_messages) {
                selectedFragment = new MessagesFragment();
            }
            
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        });

        // 6. Load default fragment on start
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NoticesFragment()).commit();
            bottomNav.setSelectedItemId(R.id.nav_notices);
        }

        // 7. FCM Setup
        askNotificationPermission();
        setupFCM();
        
        // 8. Real-time Notification Listeners (Simulating FCM for Dev)
        setupRealtimeNotifications();

        // Initialize Cloudinary (Only once)
        CloudinaryHelper.init(this);
    }

    private void setupRealtimeNotifications() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Listen for new Notices
        db.collection("notices")
            .whereGreaterThan("timestamp", startTime)
            .addSnapshotListener((value, error) -> {
                if (value != null) {
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String title = dc.getDocument().getString("title");
                            String author = dc.getDocument().getString("author");
                            if (author != null && !author.equals(user.getDisplayName())) {
                                NotificationHelper.showNotification(this, "New Notice", title, "notice");
                            }
                        }
                    }
                }
            });

        // Listen for new Club Events
        db.collection("events")
            .addSnapshotListener((value, error) -> {
                if (value != null) {
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            // Events use Firestore Timestamp or long?
                            // Let's check the model
                            Object ts = dc.getDocument().get("eventDate");
                            // We only want extremely recent ones
                            // For simplicity, we'll check if it's a new document in the last few seconds
                            String title = dc.getDocument().getString("title");
                            String createdBy = dc.getDocument().getString("createdBy");
                            if (createdBy != null && !createdBy.equals(user.getUid())) {
                                // Since 'events' don't have a 'createdAt' timestamp in the model yet, 
                                // we'll just check if it's not by us.
                                // In production, Cloud Functions handle this better.
                                NotificationHelper.showNotification(this, "New Club Update", title, "event");
                            }
                        }
                    }
                }
            });

        // Listen for new Messages (Global)
        // Note: In a real app, you'd only listen to your chats
        db.collection("chats").whereArrayContains("members", user.getUid())
            .addSnapshotListener((value, error) -> {
                if (value != null) {
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        // If a chat document changes, it might be a new message in a subcollection
                        // But SnapshotListener on a collection doesn't trigger for subcollections.
                        // For a real notification system, we'd need a "lastMessage" field in the chat doc.
                    }
                }
            });
    }

    private void setupFCM() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Subscribe to global topics for global notifications
        FirebaseMessaging.getInstance().subscribeToTopic("notices")
                .addOnCompleteListener(task -> Log.d(TAG, "Subscribed to notices topic"));
        
        FirebaseMessaging.getInstance().subscribeToTopic("club_updates")
                .addOnCompleteListener(task -> Log.d(TAG, "Subscribed to club_updates topic"));

        // Update individual token for private messages
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);
                    
                    // Save to Firestore
                    FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                            .update("fcmToken", token)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token saved to Firestore"));
                });
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show();
                }
            });

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}