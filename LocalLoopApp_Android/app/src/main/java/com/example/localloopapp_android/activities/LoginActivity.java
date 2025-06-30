package com.example.localloopapp_android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.activities.dashboard_activities.AdminDashboardActivity;
import com.example.localloopapp_android.activities.dashboard_activities.OrganizerDashboardActivity;
import com.example.localloopapp_android.activities.dashboard_activities.ParticipantDashboardActivity;
import com.example.localloopapp_android.models.UserRole;
import com.example.localloopapp_android.models.accounts.UserAccount;
import com.example.localloopapp_android.utils.Constants;
import com.example.localloopapp_android.utils.Convenience;
import com.example.localloopapp_android.services.LoginService;

/**
 * Handles user login via email or username, delegates Firebase logic to LoginService,
 * and navigates the user to their appropriate dashboard upon successful login.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etIdentifier;
    private EditText etPassword;
    private Button btnLogin;

    private final LoginService loginService = new LoginService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etIdentifier = findViewById(R.id.etIdentifier);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnSubmitLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    /**
     * Attempts to log in the user by validating input fields and calling the login service.
     * Displays error messages for invalid inputs and handles login success or failure.
     */
    private void attemptLogin() {
        String identifier = etIdentifier.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean isValid = Convenience.validateField(etIdentifier, identifier,
                val -> !TextUtils.isEmpty(val), "Email or Username is required")
                && Convenience.validateField(etPassword, password,
                val -> !TextUtils.isEmpty(val), "Password is required");

        if (!isValid) return;

        loginService.login(identifier, password, this, new LoginService.LoginCallback() {
            @Override
            public void onSuccess(UserAccount user) {
                navigateToDashboard(user);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Navigates the user to their appropriate dashboard based on their role.
     * Uses Intent to start the corresponding activity and passes user details.
     *
     * @param user The logged-in user account containing role and personal details.
     */
    private void navigateToDashboard(UserAccount user) {
        Intent intent;

        switch (UserRole.fromString(user.getRole())) {
            case PARTICIPANT:
                intent = new Intent(this, ParticipantDashboardActivity.class);
                break;
            case ORGANIZER:
                intent = new Intent(this, OrganizerDashboardActivity.class);
                break;
            case ADMIN:
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            default:
                Toast.makeText(this, "Unknown role!", Toast.LENGTH_SHORT).show();
                return;
        }

        intent.putExtra(Constants.EXTRA_USER_ID, user.getUserID());
        intent.putExtra(Constants.EXTRA_FIRST_NAME, user.getFirstName());
        intent.putExtra(Constants.EXTRA_LAST_NAME, user.getLastName());
        startActivity(intent);
    }
}
