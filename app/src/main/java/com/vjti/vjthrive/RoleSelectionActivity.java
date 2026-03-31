package com.vjti.vjthrive;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        findViewById(R.id.btnRoleStudent).setOnClickListener(v -> navigateToSignup("student"));
        findViewById(R.id.btnRoleFaculty).setOnClickListener(v -> navigateToSignup("faculty"));
        findViewById(R.id.btnRoleAdmin).setOnClickListener(v -> navigateToSignup("admin"));
    }

    private void navigateToSignup(String role) {
        Intent intent = new Intent(this, SignupActivity.class);
        intent.putExtra("ROLE", role);
        startActivity(intent);
    }
}
