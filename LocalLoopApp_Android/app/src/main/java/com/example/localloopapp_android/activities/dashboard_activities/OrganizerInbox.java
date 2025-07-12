package com.example.localloopapp_android.activities.dashboard_activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localloopapp_android.R;

public class OrganizerInbox extends AppCompatActivity {

    private ImageButton btnBack, btnHome, btnProfile, btnNotifications;
    private TextView toolbarTitle, tvPlaceholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_organizer);

        // Top bar
        btnBack = findViewById(R.id.btnBack);
        toolbarTitle = findViewById(R.id.toolbarTitle);

        btnBack.setOnClickListener(v -> finish()); // go back to previous screen

        // Placeholder text view
        tvPlaceholder = findViewById(R.id.tvPlaceholder);
        tvPlaceholder.setOnClickListener(v ->
                Toast.makeText(this, "Notifications placeholder clicked", Toast.LENGTH_SHORT).show()
        );

        // Bottom navigation
        btnHome = findViewById(R.id.btnHome);
        btnProfile = findViewById(R.id.btnProfile);
        btnNotifications = findViewById(R.id.btnNotifications);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrganizerDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageAccountOrganizer.class);
            startActivity(intent);
            finish();
        });

        btnNotifications.setOnClickListener(v -> {
            Toast.makeText(this, "Already in Notifications", Toast.LENGTH_SHORT).show();
        });
    }
}
