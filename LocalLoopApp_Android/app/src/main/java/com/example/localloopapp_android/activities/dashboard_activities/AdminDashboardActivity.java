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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.models.accounts.UserAccount;
import com.example.localloopapp_android.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboardActivity";
    private static final String CURRENT_ADMIN_ID = "25VWv0nGiBe4t5XODVOiI7jWMVq1"; // hardcoded admin ID — yes, it's ugly. We'll live.

    private TextView tvWelcomeMessage;
    private LinearLayout userListContainer;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate reached!");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard); // Loads Admin-specific layout

        // Handle padding for system UI (status bar, nav bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init views and Firebase reference
        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        userListContainer = findViewById(R.id.userListContainer);
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Greet admin by name if available, otherwise generic welcome
        String firstName = getIntent().getStringExtra(Constants.EXTRA_FIRST_NAME);
        tvWelcomeMessage.setText(firstName != null
                ? "Welcome " + firstName + "! You are logged in as Admin."
                : "Welcome, Admin!");

        fetchAllUsersAndDisplay(); // Main attraction
    }

    /**
     * Functions:
     * 1. display all users
     * 2. disable/ enable user
     * 3. delete user
     */

    private void fetchAllUsersAndDisplay() {
        Log.d(TAG, "fetchAllUsersAndDisplay() called");

        // One-time read of the users node from Firebase
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    showToast("No users found in database.");
                    return;
                }

                userListContainer.removeAllViews(); // Clear previous list

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (isCurrentAdmin(userSnapshot)) continue; // Don't show yourself, ego isn't *that* big

                    UserAccount user = parseUser(userSnapshot);
                    View userRow = createUserRow(user, userSnapshot.getKey());
                    userListContainer.addView(userRow);
                }

                // Patch for that annoying ScrollView bug that sometimes refuses to scroll
                userListContainer.post(userListContainer::requestLayout);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                logAndToastError("Failed to load users: " + error.getMessage(), error.toException());
            }
        });
    }

    // DISABLE/ENABLE user
    private void toggleUserStatus(String firebaseKey, UserAccount.Status currentStatus) {
        UserAccount.Status newStatus = currentStatus == UserAccount.Status.ACTIVE
                ? UserAccount.Status.INACTIVE
                : UserAccount.Status.ACTIVE;

        usersRef.child(firebaseKey).child("status").setValue(newStatus.name())
                .addOnSuccessListener(unused -> {
                    showToast("Status updated");
                    fetchAllUsersAndDisplay(); // Reflect the changes immediately !!!!
                })
                .addOnFailureListener(e -> showToast("Failed to update status"));
    }

    // This is where the admin gets to play judge, jury, and executioner
    private void confirmAndDeleteUser(String firebaseKey, String displayName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User?")
                .setMessage("Whoa. What did " + displayName + " do to deserve this? 😬\nThis action cannot be undone.")
                .setPositiveButton("Delete Forever", (dialog, which) -> deleteUserAfterConfirmed(firebaseKey))
                .setNegativeButton("Cancel", null)
                .show();
    }


    /**
     * helper methods
     */


    // Spoiler alert: you're the admin
    private boolean isCurrentAdmin(DataSnapshot snapshot) {
        String userId = snapshot.child("userID").getValue(String.class);
        return CURRENT_ADMIN_ID.equals(userId);
    }

    // Actually erases the user from Firebase. No coming back from this one.
    private void deleteUserAfterConfirmed(String firebaseKey) {
        usersRef.child(firebaseKey).removeValue()
                .addOnSuccessListener(unused -> {
                    showToast("User deleted");
                    fetchAllUsersAndDisplay(); // Refresh the list to reflect the apocalypse
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to delete user");
                    Log.e(TAG, "deleteUser: " + e.getMessage(), e);
                });
    }

    // Converts Firebase snapshot into a User object
    private UserAccount parseUser(DataSnapshot snapshot) {
        UserAccount user = new UserAccount() {}; // Anonymous subclass because User is abstract — don’t @ me
        user.setUserID(snapshot.child("userID").getValue(String.class));
        user.setFirstName(snapshot.child("firstName").getValue(String.class));
        user.setLastName(snapshot.child("lastName").getValue(String.class));
        user.setUsername(snapshot.child("username").getValue(String.class));
        user.setEmail(snapshot.child("email").getValue(String.class));
        user.setPhoneNumber(snapshot.child("phoneNumber").getValue(String.class));
        user.setRole(snapshot.child("role").getValue(String.class));
        user.setStatus(snapshot.child("status").getValue(String.class)); // Let User handle its own enum drama

        return user;
    }

    // Builds a UI card for a single user, with info and the fabled ⋮ menu
    private View createUserRow(UserAccount user, String firebaseKey) {
        View row = getLayoutInflater().inflate(R.layout.item_user_admin, userListContainer, false);
        TextView tvInfo = row.findViewById(R.id.tv_user_info);
        ImageView ivMenu = row.findViewById(R.id.iv_overflow_menu);

        // Basic user info — the boring but necessary stuff
        String info = String.format("Name: %s\nRole: %s\nEmail: %s\nUsername: %s",
                getOrDefault(user.getFirstName()),
                getOrDefault(user.getRole()),
                getOrDefault(user.getEmail()),
                getOrDefault(user.getUsername()));

        // If they’re inactive, show them the door (visually)
        if (user.getStatusEnum() == UserAccount.Status.INACTIVE) {
            info += "\n🚫 Inactive";
            row.setAlpha(0.5f); // ghost them, literally
        }

        tvInfo.setText(info);
        setupOverflowMenu(ivMenu, user, firebaseKey);

        return row;
    }

    // Sets up the famous ⋮ menu with "Enable/Disable" and "Delete"
    private void setupOverflowMenu(ImageView menuIcon, UserAccount user, String firebaseKey) {
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, menuIcon);
            popup.inflate(R.menu.menu_user_admin);

            // Dynamically label the status toggle
            MenuItem toggleItem = popup.getMenu().findItem(R.id.action_toggle_status);
            toggleItem.setTitle(user.getStatusEnum() == UserAccount.Status.ACTIVE ? "Disable User" : "Enable User");

            // What happens when you click a menu item
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_toggle_status) {
                    toggleUserStatus(firebaseKey, user.getStatusEnum());
                    return true;
                } else if (id == R.id.action_delete_user) {
                    confirmAndDeleteUser(firebaseKey, user.getFirstName());
                    return true;
                }
                return false;
            });

            popup.show();
        });
    }

    // Toast shortcut, for when you don't feel like typing `.makeText(...)` every time
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Combine logging and user-facing error reporting, because why duplicate pain?
    private void logAndToastError(String message, Exception e) {
        Log.e(TAG, message, e);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Null-safe fallback for missing user data — a polite "IDK"
    private String getOrDefault(String value) {
        return value != null ? value : "N/A";
    }
}
