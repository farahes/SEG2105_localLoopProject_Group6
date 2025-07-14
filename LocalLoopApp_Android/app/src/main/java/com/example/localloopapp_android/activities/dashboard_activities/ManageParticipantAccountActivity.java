package com.example.localloopapp_android.activities.dashboard_activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.activities.LoginActivity;
import com.example.localloopapp_android.activities.dashboard_activities.ParticipantDashboardActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ManageParticipantAccountActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView profileName, profileUsername, profileEmail;
    private Button btnLogout;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_participant);

        profileImage = findViewById(R.id.profileImage);
        profileName = findViewById(R.id.profileName);
        profileUsername = findViewById(R.id.profileUsername);
        profileEmail = findViewById(R.id.profileEmail);
        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);

        // Load user info from Firebase Realtime Database
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        if (userId != null) {
            FirebaseDatabase.getInstance().getReference("users").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                profileName.setText("Name: (not set)");
                                profileUsername.setText("Username: (not set)");
                                profileEmail.setText("Email: (not set)");
                                return;
                            }
                            profileName.setText("Name: " + snapshot.child("name").getValue(String.class));
                            profileUsername.setText("Username: " + snapshot.child("username").getValue(String.class));
                            profileEmail.setText("Email: " + snapshot.child("email").getValue(String.class));
                        }
                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(ManageParticipantAccountActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            profileName.setText("Name: (not set)");
            profileUsername.setText("Username: (not set)");
            profileEmail.setText("Email: (not set)");
        }

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ManageParticipantAccountActivity.this, ParticipantDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("user_prefs", MODE_PRIVATE).edit().clear().apply();
            Toast.makeText(ManageParticipantAccountActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ManageParticipantAccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
