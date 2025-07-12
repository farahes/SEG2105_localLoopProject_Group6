package com.example.localloopapp_android.activities.dashboard_activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.activities.LoginActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ManageAccountOrganizer extends AppCompatActivity {

    private ImageView profileImage;
    private TextView profileName, profileUsername, profileEmail;
    private Button btnEditProfile, btnDeleteAccount;
    private ImageButton btnProfile, btnHome, btnNotifications;

    private Button btnLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_organizer);

        // Initialize views
        profileImage = findViewById(R.id.profileImage);
        profileName = findViewById(R.id.profileName);
        profileUsername = findViewById(R.id.profileUsername);
        profileEmail = findViewById(R.id.profileEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        // Fetch userId from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        if (userId != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(ManageAccountOrganizer.this, "User data not found.", Toast.LENGTH_SHORT).show();
                        profileName.setText("Name: (not set)");
                        profileUsername.setText("Username: (not set)");
                        profileEmail.setText("Email: (not set)");
                        return;
                    }
                    String name = snapshot.child("name").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    profileName.setText("Name: " + (name != null ? name : "(not set)"));
                    profileUsername.setText("Username: " + (username != null ? username : "(not set)"));
                    profileEmail.setText("Email: " + (email != null ? email : "(not set)"));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ManageAccountOrganizer.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                    profileName.setText("Name: (not set)");
                    profileUsername.setText("Username: (not set)");
                    profileEmail.setText("Email: (not set)");
                }
            });
        } else {
            Toast.makeText(this, "User ID not found in SharedPreferences.", Toast.LENGTH_SHORT).show();
            profileName.setText("Name: (not set)");
            profileUsername.setText("Username: (not set)");
            profileEmail.setText("Email: (not set)");
        }

        // Edit Profile Button
        btnEditProfile.setOnClickListener(view -> {
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show();
            // TODO: Launch edit profile activity or show a dialog
        });

        // Delete Account Button
        btnDeleteAccount.setOnClickListener(view -> {
            Toast.makeText(this, "Delete Account clicked", Toast.LENGTH_LONG).show();
            // TODO: Add confirmation dialog and delete logic
        });

        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("user_prefs", MODE_PRIVATE).edit().clear().apply();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });


        // Bottom Navigation buttons
        btnProfile = findViewById(R.id.btnProfile);
        btnHome = findViewById(R.id.btnHome);
        btnNotifications = findViewById(R.id.btnNotifications);

        btnProfile.setOnClickListener(v -> {
            // Already in profile
            Toast.makeText(this, "Already on Profile page", Toast.LENGTH_SHORT).show();
        });

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrganizerDashboardActivity.class);
            startActivity(intent);
            finish();
        });


    }
}
