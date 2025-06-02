package com.example.localloopapp_android.activities.dashboard_activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.models.User;
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
                    String currentAdminId = "25VWv0nGiBe4t5XODVOiI7jWMVq1";

                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        Log.d(TAG, "Reading user: " + userSnapshot.getKey());

                        String userId = userSnapshot.child("userID").getValue(String.class);
                        if (userId != null && userId.equals(currentAdminId)) {
                            Log.d(TAG, "Skipping current admin: " + userId);
                            continue;
                        }

                        String role = userSnapshot.child("role").getValue(String.class);
                        String firstName = userSnapshot.child("firstName").getValue(String.class);
                        String email = userSnapshot.child("email").getValue(String.class);
                        String username = userSnapshot.child("username").getValue(String.class);
                        String statusStr = userSnapshot.child("status").getValue(String.class);

                        User.Status status = User.Status.ACTIVE;
                        if (statusStr != null) {
                            try {
                                status = User.Status.valueOf(statusStr.toUpperCase());
                            } catch (IllegalArgumentException ignored) {}
                        }

                        // Make status effectively final for inner class usage
                        final User.Status userStatus = status;

                        View userRow = getLayoutInflater().inflate(R.layout.item_user_admin, userListContainer, false);
                        TextView tvInfo = userRow.findViewById(R.id.tv_user_info);
                        ImageView ivOverflowMenu = userRow.findViewById(R.id.iv_overflow_menu);

                        String userInfo = "Name: " + (firstName != null ? firstName : "N/A") + "\n"
                                + "Role: " + (role != null ? role : "N/A") + "\n"
                                + "Email: " + (email != null ? email : "N/A") + "\n"
                                + "Username: " + (username != null ? username : "N/A");

                        tvInfo.setText(userInfo);

                        if (userStatus == User.Status.INACTIVE) {
                            userRow.setAlpha(0.5f);
                            tvInfo.append("\n🚫 Inactive");
                        }

                        ivOverflowMenu.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PopupMenu popup = new PopupMenu(AdminDashboardActivity.this, ivOverflowMenu);
                                popup.inflate(R.menu.menu_user_admin);

                                MenuItem toggleItem = popup.getMenu().findItem(R.id.action_toggle_status);
                                toggleItem.setTitle(userStatus == User.Status.ACTIVE ? "Disable User" : "Enable User");

                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        int itemId = item.getItemId();

                                        if (itemId == R.id.action_toggle_status) {
                                            User.Status newStatus = (userStatus == User.Status.ACTIVE)
                                                    ? User.Status.INACTIVE
                                                    : User.Status.ACTIVE;

                                            usersRef.child(userSnapshot.getKey()).child("status").setValue(newStatus.name())
                                                    .addOnSuccessListener(unused -> {
                                                        Toast.makeText(AdminDashboardActivity.this, "Status updated", Toast.LENGTH_SHORT).show();
                                                        fetchAllUsersAndDisplay(); // ✅ force reload for correct UI and label
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(AdminDashboardActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                                                    });

                                            return true;

                                        } else if (itemId == R.id.action_delete_user) {
                                            Toast.makeText(AdminDashboardActivity.this, "Delete not implemented yet", Toast.LENGTH_SHORT).show();
                                            return true;

                                        } else {
                                            return false;
                                        }
                                    }

                                });

                                popup.show();
                            }
                        });

                        userListContainer.addView(userRow);
                    }
                    /**
                     * forces a layout refresh after the scollView finished rendering
                     * BECAUSE SCROLLVIEW WASNT SCROLLING
                     * duh
                     */
                    userListContainer.post(() -> {
                        userListContainer.requestLayout();
                    });

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
