package com.example.localloopapp_android.activities.dashboard_activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboardActivity";
    private TextView tvWelcomeMessage;
    private LinearLayout userListContainer;

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate reached!");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard); // admin-specific layout

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        userListContainer = findViewById(R.id.userListContainer);
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // ADMIN-ONLY LOGIC: just get the first name and load users (for now)
        String firstName = getIntent().getStringExtra(Constants.EXTRA_FIRST_NAME);
        if (firstName != null) {
            tvWelcomeMessage.setText("Welcome " + firstName + "! You are logged in as Admin.");
            fetchAllUsersAndDisplay();
        } else {
            tvWelcomeMessage.setText("Welcome, Admin!");
        }
    }

    private void fetchAllUsersAndDisplay() {
        Log.d(TAG, "fetchAllUsersAndDisplay() called");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange triggered");

                if (snapshot.exists()) {
                    Log.d(TAG, "Snapshot exists: " + snapshot.getChildrenCount());

                    userListContainer.removeAllViews();

                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        Log.d(TAG, "Reading user: " + userSnapshot.getKey());

                        String role = userSnapshot.child("role").getValue(String.class);
                        String firstName = userSnapshot.child("firstName").getValue(String.class);
                        String email = userSnapshot.child("email").getValue(String.class);
                        String username = userSnapshot.child("username").getValue(String.class);

                        Log.d(TAG, "Fetched user: " + firstName + " | " + role + " | " + email + " | " + username);

                        StringBuilder userInfo = new StringBuilder();
                        userInfo.append("Name: ").append(firstName != null ? firstName : "N/A").append("\n");
                        userInfo.append("Role: ").append(role != null ? role : "N/A").append("\n");
                        userInfo.append("Email: ").append(email != null ? email : "N/A").append("\n");
                        userInfo.append("Username: ").append(username != null ? username : "N/A").append("\n");
                        userInfo.append("-------------------------");

                        TextView userTextView = new TextView(AdminDashboardActivity.this);
                        userTextView.setText(userInfo.toString());
                        userTextView.setPadding(10, 10, 10, 10);

                        userListContainer.addView(userTextView);
                    }
                } else {
                    Log.w(TAG, "No users found (snapshot.exists() = false)");
                    Toast.makeText(AdminDashboardActivity.this, "No users found in database.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage(), error.toException());
                Toast.makeText(AdminDashboardActivity.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
