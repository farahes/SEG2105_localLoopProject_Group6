package com.example.localloopapp_android;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";
    public static final String EXTRA_USER_FIRST_NAME = "com.example.localloopapp_android.USER_FIRST_NAME";
    public static final String EXTRA_USER_ROLE = "com.example.localloopapp_android.USER_ROLE";

    private TextView tvWelcomeMessage;
    private LinearLayout userListContainer; // container to display user list

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate reached!");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        userListContainer = findViewById(R.id.userListContainer);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Get extras from Intent
        if (getIntent() != null) {
            String firstName = getIntent().getStringExtra(EXTRA_USER_FIRST_NAME);
            String role = getIntent().getStringExtra(EXTRA_USER_ROLE);

            if (firstName != null && role != null) {
                String welcomeText = "Welcome " + firstName + "! You are logged in as \"" + role + "\".";
                tvWelcomeMessage.setText(welcomeText);

                if ("Admin".equalsIgnoreCase(role)) {
                    fetchAllUsersAndDisplay();
                }
            } else {
                tvWelcomeMessage.setText("Welcome! Role information is missing.");
                Toast.makeText(this, "Error: User details not fully provided.", Toast.LENGTH_LONG).show();
            }
        } else {
            tvWelcomeMessage.setText("Welcome! User details not found.");
            Toast.makeText(this, "Error: Could not retrieve user details.", Toast.LENGTH_LONG).show();
        }
    }

    private void fetchAllUsersAndDisplay() {
        Log.d(TAG, "Admin logged in - fetching all users...");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userListContainer.removeAllViews();

                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        // Try to get role for this user
                        String role = userSnapshot.child("role").getValue(String.class);
                        String firstName = userSnapshot.child("firstName").getValue(String.class);
                        String email = userSnapshot.child("email").getValue(String.class);
                        String username = userSnapshot.child("username").getValue(String.class);

                        // Build a user info string
                        StringBuilder userInfo = new StringBuilder();
                        userInfo.append("Name: ").append(firstName != null ? firstName : "N/A").append("\n");
                        userInfo.append("Role: ").append(role != null ? role : "N/A").append("\n");
                        userInfo.append("Email: ").append(email != null ? email : "N/A").append("\n");
                        userInfo.append("Username: ").append(username != null ? username : "N/A").append("\n");
                        userInfo.append("-------------------------");

                        // Create a new TextView to display this user's info
                        TextView userTextView = new TextView(DashboardActivity.this);
                        userTextView.setText(userInfo.toString());
                        userTextView.setPadding(10, 10, 10, 10);

                        userListContainer.addView(userTextView);
                    }
                } else {
                    Toast.makeText(DashboardActivity.this, "No users found in database.", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "No users found in 'users' node.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error fetching users", error.toException());
            }
        });
    }
}
