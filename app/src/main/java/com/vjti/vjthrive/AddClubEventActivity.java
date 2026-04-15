package com.vjti.vjthrive;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vjti.vjthrive.models.ClubEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AddClubEventActivity extends AppCompatActivity {

    private static final String TAG = "AddClubEventActivity";

    private TextInputEditText etClub, etTitle, etContent, etAttachment;
    private android.widget.TextView tvSelectedDate;
    private android.widget.Button btnPickDate;
    private ChipGroup cgColors;
    private ExtendedFloatingActionButton fabSend;

    private FirebaseFirestore db;
    private String selectedColor = "#6200EE"; // Default Material Purple
    private long selectedEventDate = 0;
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_club_event);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupColorSelection();

        fabSend.setOnClickListener(v -> postClubEvent());
    }

    private void initViews() {
        etClub = findViewById(R.id.etClub);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        etAttachment = findViewById(R.id.etAttachment);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnPickDate = findViewById(R.id.btnPickDate);
        cgColors = findViewById(R.id.cgColors);
        fabSend = findViewById(R.id.fabSend);

        btnPickDate.setOnClickListener(v -> showDatePicker());

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_send) {
                postClubEvent();
                return true;
            }
            return false;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupColorSelection() {
        cgColors.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipRed) selectedColor = "#F44336";
            else if (checkedId == R.id.chipBlue) selectedColor = "#2196F3";
            else if (checkedId == R.id.chipGreen) selectedColor = "#4CAF50";
            else if (checkedId == R.id.chipYellow) selectedColor = "#FFEB3B";
            else if (checkedId == R.id.chipPurple) selectedColor = "#9C27B0";
        });
        
        // Default selection
        cgColors.check(R.id.chipPurple);
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Event Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedEventDate = selection;
            tvSelectedDate.setText(displayDateFormat.format(new Date(selection)));
        });

        datePicker.show(getSupportFragmentManager(), "EVENT_DATE_PICKER");
    }

    private void postClubEvent() {
        String club = etClub.getText().toString().trim();
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String attachment = etAttachment.getText().toString().trim();

        if (TextUtils.isEmpty(club)) {
            etClub.setError("Club name is required");
            return;
        }
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            return;
        }
        if (TextUtils.isEmpty(content)) {
            etContent.setError("Content is required");
            return;
        }
        if (selectedEventDate == 0) {
            Toast.makeText(this, "Please select an event date", Toast.LENGTH_SHORT).show();
            return;
        }

        fabSend.setEnabled(false);
        Toast.makeText(this, "Posting update...", Toast.LENGTH_SHORT).show();

        String eventId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        ClubEvent event = new ClubEvent(
                eventId,
                club,
                title,
                content,
                attachment,
                selectedColor,
                selectedEventDate,
                timestamp
        );

        db.collection("club_events").document(eventId)
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddClubEventActivity.this, "Update posted successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    fabSend.setEnabled(true);
                    Toast.makeText(AddClubEventActivity.this, "Failed to post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
