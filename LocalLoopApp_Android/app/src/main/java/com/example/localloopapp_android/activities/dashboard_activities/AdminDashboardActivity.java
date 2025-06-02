package com.example.localloopapp_android.activities.dashboard_activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
    private static final String CURRENT_ADMIN_ID = "25VWv0nGiBe4t5XODVOiI7jWMVq1";

    private TextView tvWelcomeMessage;
    private LinearLayout userListContainer;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate reached!");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        userListContainer = findViewById(R.id.userListContainer);
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        String firstName = getIntent().getStringExtra(Constants.EXTRA_FIRST_NAME);
        tvWelcomeMessage.setText(firstName != null
                ? "Welcome " + firstName + "! You are logged in as Admin."
                : "Welcome, Admin!");

        fetchAllUsersAndDisplay();
    }

    private void fetchAllUsersAndDisplay() {
        Log.d(TAG, "fetchAllUsersAndDisplay() called");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    showToast("No users found in database.");
                    return;
                }

                userListContainer.removeAllViews();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (isCurrentAdmin(userSnapshot)) continue;

                    User user = parseUser(userSnapshot);
                    View userRow = createUserRow(user, userSnapshot.getKey());
                    userListContainer.addView(userRow);
                }

                // Fix scroll bug by requesting layout after scrollview render
                userListContainer.post(userListContainer::requestLayout);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                logAndToastError("Failed to load users: " + error.getMessage(), error.toException());
            }
        });
    }

    private boolean isCurrentAdmin(DataSnapshot snapshot) {
        String userId = snapshot.child("userID").getValue(String.class);
        return CURRENT_ADMIN_ID.equals(userId);
    }

    private User parseUser(DataSnapshot snapshot) {
        User user = new User() {}; // Anonymous subclass because User is abstract
        user.setUserID(snapshot.child("userID").getValue(String.class));
        user.setFirstName(snapshot.child("firstName").getValue(String.class));
        user.setLastName(snapshot.child("lastName").getValue(String.class));
        user.setUsername(snapshot.child("username").getValue(String.class));
        user.setEmail(snapshot.child("email").getValue(String.class));
        user.setPhoneNumber(snapshot.child("phoneNumber").getValue(String.class));
        user.setRole(snapshot.child("role").getValue(String.class));
        user.setStatus(snapshot.child("status").getValue(String.class)); // ✅ this is all you need

        return user;
    }

    private View createUserRow(User user, String firebaseKey) {
        View row = getLayoutInflater().inflate(R.layout.item_user_admin, userListContainer, false);
        TextView tvInfo = row.findViewById(R.id.tv_user_info);
        ImageView ivMenu = row.findViewById(R.id.iv_overflow_menu);

        String info = String.format("Name: %s\nRole: %s\nEmail: %s\nUsername: %s",
                getOrDefault(user.getFirstName()),
                getOrDefault(user.getRole()),
                getOrDefault(user.getEmail()),
                getOrDefault(user.getUsername()));

        if (user.getStatusEnum() == User.Status.INACTIVE) {
            info += "\n🚫 Inactive";
            row.setAlpha(0.5f);
        }

        tvInfo.setText(info);
        setupOverflowMenu(ivMenu, user, firebaseKey);

        return row;
    }

    private void setupOverflowMenu(ImageView menuIcon, User user, String firebaseKey) {
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, menuIcon);
            popup.inflate(R.menu.menu_user_admin);

            MenuItem toggleItem = popup.getMenu().findItem(R.id.action_toggle_status);
            toggleItem.setTitle(user.getStatusEnum() == User.Status.ACTIVE ? "Disable User" : "Enable User");

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_toggle_status) {
                    toggleUserStatus(firebaseKey, user.getStatusEnum());
                    return true;
                } else if (id == R.id.action_delete_user) {
                    showToast("Delete not implemented yet");
                    return true;
                }
                return false;
            });

            popup.show();
        });
    }

    private void toggleUserStatus(String firebaseKey, User.Status currentStatus) {
        User.Status newStatus = currentStatus == User.Status.ACTIVE
                ? User.Status.INACTIVE
                : User.Status.ACTIVE;

        usersRef.child(firebaseKey).child("status").setValue(newStatus.name())
                .addOnSuccessListener(unused -> {
                    showToast("Status updated");
                    fetchAllUsersAndDisplay();
                })
                .addOnFailureListener(e -> showToast("Failed to update status"));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void logAndToastError(String message, Exception e) {
        Log.e(TAG, message, e);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private String getOrDefault(String value) {
        return value != null ? value : "N/A";
    }
}
