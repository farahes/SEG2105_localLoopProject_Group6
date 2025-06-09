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

// ... imports omitted for brevity ...
import com.example.localloopapp_android.services.AdminService;

/**
 * AdminDashboardActivity
 *
 * Displays a list of all user accounts (excluding the current admin).
 * Allows the admin to enable/disable or permanently delete user accounts
 * via an overflow menu (⋮).
 *
 * Delegates data operations to AdminService.
 */

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboardActivity";
    private static final String CURRENT_ADMIN_ID = "25VWv0nGiBe4t5XODVOiI7jWMVq1";

    private TextView tvWelcomeMessage;
    private LinearLayout userListContainer;

    private AdminService adminService;

    /**
     * Initializes the admin dashboard screen and triggers user fetch.
     */
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
        adminService = new AdminService();

        String firstName = getIntent().getStringExtra(Constants.EXTRA_FIRST_NAME);
        tvWelcomeMessage.setText(firstName != null
                ? "Welcome " + firstName + "! You are logged in as Admin."
                : "Welcome, Admin!");

        fetchAllUsersAndDisplay();
    }

    /**
     * Fetches all users from the database and displays them in the UI.
     */
    private void fetchAllUsersAndDisplay() {
        adminService.getAllUsers(userList -> {
            userListContainer.removeAllViews();
            for (AdminService.UserRow row : userList) {
                if (CURRENT_ADMIN_ID.equals(row.user.getUserID())) continue;
                userListContainer.addView(createUserRow(row.user, row.firebaseKey));
            }
        }, error -> {
            logAndToastError("Failed to load users: " + error.getMessage(), error.toException());
        });
    }

    /**
     * Creates a styled row (card) for a single user, including overflow menu.
     */
    private View createUserRow(UserAccount user, String firebaseKey) {
        View row = getLayoutInflater().inflate(R.layout.item_user_admin, userListContainer, false);
        TextView tvInfo = row.findViewById(R.id.tv_user_info);
        ImageView ivMenu = row.findViewById(R.id.iv_overflow_menu);

        String info = String.format("Name: %s\nRole: %s\nEmail: %s\nUsername: %s",
                getOrDefault(user.getFirstName()),
                getOrDefault(user.getRole()),
                getOrDefault(user.getEmail()),
                getOrDefault(user.getUsername()));

        if (user.getStatusEnum() == UserAccount.Status.INACTIVE) {
            info += "\n🚫 Inactive";
            row.setAlpha(0.5f);
        }

        tvInfo.setText(info);
        setupOverflowMenu(ivMenu, user, firebaseKey);
        return row;
    }

    /**
     * Sets up the overflow (⋮) menu for a user, with enable/disable and delete options.
     */
    private void setupOverflowMenu(ImageView menuIcon, UserAccount user, String firebaseKey) {
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, menuIcon);
            popup.inflate(R.menu.menu_user_admin);

            MenuItem toggleItem = popup.getMenu().findItem(R.id.action_toggle_status);
            toggleItem.setTitle(user.getStatusEnum() == UserAccount.Status.ACTIVE ? "Disable User" : "Enable User");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_toggle_status) {
                    adminService.toggleUserStatus(firebaseKey, user.getStatusEnum(), this::fetchAllUsersAndDisplay);
                    return true;
                } else if (item.getItemId() == R.id.action_delete_user) {
                    confirmAndDeleteUser(firebaseKey, user.getFirstName());
                    return true;
                }
                return false;
            });

            popup.show();
        });
    }

    /**
     * Shows a confirmation dialog, then deletes a user if confirmed.
     */
    private void confirmAndDeleteUser(String firebaseKey, String displayName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User?")
                .setMessage("Whoa. What did " + displayName + " do to deserve this? 😬\nThis action cannot be undone.")
                .setPositiveButton("Delete Forever", (dialog, which) ->
                        adminService.deleteUser(firebaseKey, this::fetchAllUsersAndDisplay))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Logs an error and shows a toast to the user.
     */
    private void logAndToastError(String message, Exception e) {
        Log.e(TAG, message, e);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Returns the value if not null, or "N/A" otherwise (for UI display).
     */
    private String getOrDefault(String value) {
        return value != null ? value : "N/A";
    }
}

