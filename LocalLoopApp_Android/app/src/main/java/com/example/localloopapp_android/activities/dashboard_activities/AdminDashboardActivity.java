package com.example.localloopapp_android.activities.dashboard_activities;

import android.content.Intent;
import android.widget.Button;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.activities.ManageCategoriesActivity;
import com.example.localloopapp_android.models.accounts.UserAccount;
import com.example.localloopapp_android.utils.Constants;
import com.example.localloopapp_android.viewmodels.AdminViewModel;

/**
 * AdminDashboardActivity
 *
 * Displays a list of all user accounts (excluding the current admin).
 * Allows the admin to enable/disable or permanently delete user accounts
 * via an overflow menu (⋮).
 *
 * Delegates data operations to AdminViewModel.
 */

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboardActivity";
    private static final String CURRENT_ADMIN_ID = "25VWv0nGiBe4t5XODVOiI7jWMVq1";

    private TextView tvWelcomeMessage;
    private LinearLayout userListContainer;

    private AdminViewModel adminViewModel;

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

        // Correct ViewModel lifecycle-safe usage
        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        String firstName = getIntent().getStringExtra(Constants.EXTRA_FIRST_NAME);
        tvWelcomeMessage.setText(firstName != null
                ? "Welcome " + firstName + "! You are logged in as Admin."
                : "Welcome, Admin!");

        observeUserList();
        adminViewModel.fetchAllUsers(); // triggers loading
        setupManageCategoriesButton();

        ImageButton btnProfile = findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminAccountActivity.class);
                startActivity(intent);
            });
        }
    }

    /**
     * Observes the user list LiveData from the ViewModel.
     * Updates the UI whenever the user list changes.
     */
    private void observeUserList() {
        adminViewModel.getUserList().observe(this, userList -> {
            userListContainer.removeAllViews();
            for (AdminViewModel.UserRow row : userList) {
                if (CURRENT_ADMIN_ID.equals(row.user.getUserID())) continue;
                userListContainer.addView(createUserRow(row.user, row.firebaseKey));
            }
        });
    }

    /**
     * Sets up the "Manage Categories" button to navigate to the ManageCategoriesActivity.
     */
    private void setupManageCategoriesButton() {
        Button btnManageCategories = findViewById(R.id.btnManageCategories);
        btnManageCategories.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageCategoriesActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Creates a user row view for the admin dashboard.
     * Displays user info and sets up the overflow menu for actions.
     *
     * @param user The UserAccount object containing user details.
     * @param firebaseKey The Firebase key for the user.
     * @return A View representing the user row.
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
     * Sets up the overflow menu for user actions (enable/disable, delete).
     * Displays a popup menu when the menu icon is clicked.
     *
     * @param menuIcon The ImageView that acts as the menu button.
     * @param user The UserAccount object for the user.
     * @param firebaseKey The Firebase key for the user.
     */
    private void setupOverflowMenu(ImageView menuIcon, UserAccount user, String firebaseKey) {
        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, menuIcon);
            popup.inflate(R.menu.menu_user_admin);

            MenuItem toggleItem = popup.getMenu().findItem(R.id.action_toggle_status);
            toggleItem.setTitle(user.getStatusEnum() == UserAccount.Status.ACTIVE
                    ? "Disable User"
                    : "Enable User");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_toggle_status) {
                    adminViewModel.toggleUserStatus(firebaseKey, user.getStatusEnum());
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
     * Confirms with the admin before permanently deleting a user.
     * Displays an AlertDialog with confirmation options.
     *
     * @param firebaseKey The Firebase key of the user to delete.
     * @param displayName The display name of the user for confirmation message.
     */
    private void confirmAndDeleteUser(String firebaseKey, String displayName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User?")
                .setMessage("Whoa. What did " + displayName + " do to deserve this? 😬\nThis action cannot be undone.")
                .setPositiveButton("Delete Forever", (dialog, which) ->
                        adminViewModel.deleteUser(firebaseKey))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Logs an error message and shows a Toast to the user.
     * Used for error handling in data operations.
     *
     * @param message The error message to log and display.
     * @param e The exception that caused the error.
     */
    private void logAndToastError(String message, Exception e) {
        Log.e(TAG, message, e);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Returns a non-null string or "N/A" if the value is null.
     * Used to ensure UI displays meaningful information.
     *
     * @param value The string value to check.
     * @return The original value or "N/A" if null.
     */
    private String getOrDefault(String value) {
        return value != null ? value : "N/A";
    }
}

