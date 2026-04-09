package com.vjti.vjthrive;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

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
                // selectedFragment = new ClubsFragment(); 
                return true; // placeholder
            } else if (itemId == R.id.nav_calendar) {
                // selectedFragment = new CalendarFragment();
                return true; // placeholder
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
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}