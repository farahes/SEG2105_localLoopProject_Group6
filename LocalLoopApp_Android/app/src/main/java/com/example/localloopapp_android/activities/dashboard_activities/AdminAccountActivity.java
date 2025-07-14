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
import com.example.localloopapp_android.activities.dashboard_activities.AdminDashboardActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminAccountActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView profileName, profileUsername, profileEmail;
    private Button btnLogout;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_admin);

        // Bind views
        profileImage = findViewById(R.id.profileImage);
        profileName = findViewById(R.id.profileName);
        profileUsername = findViewById(R.id.profileUsername);
        profileEmail = findViewById(R.id.profileEmail);
        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);

        // Back button logic - back to admin dashboard
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // Load user info from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        if (userId != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(AdminAccountActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(AdminAccountActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
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

        // Logout logic
        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("user_prefs", MODE_PRIVATE).edit().clear().apply();

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}

