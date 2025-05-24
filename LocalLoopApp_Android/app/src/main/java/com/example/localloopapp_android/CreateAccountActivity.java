package com.example.localloopapp_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

// Firebase Auth imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.auth.FirebaseAuthUserCollisionException; // For specific error handling

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

// Assuming User, Participant, Organizer, Admin classes are correctly defined
// and InputValidator is also available.

public class CreateAccountActivity extends AppCompatActivity {

    private static final String TAG = "CreateAccountActivity";

    private DatabaseReference mDatabaseUsersRef; // Reference to the "users" node
    private FirebaseAuth mAuth;

    private EditText editFirstName, editLastName, editUserName, editEmail, editPhone, editPassword, editConfirmPassword, editCompany;
    private CheckBox checkOrganizer;
    private Button buttonCreateAccount;
    // It's good practice to have a ProgressBar for long operations
    // private ProgressBar progressBarCreateAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsersRef = FirebaseDatabase.getInstance().getReference("users");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editFirstName = findViewById(R.id.edit_first_name);
        editLastName = findViewById(R.id.edit_last_name);
        editUserName = findViewById(R.id.edit_username);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        editPassword = findViewById(R.id.edit_password);
        editConfirmPassword = findViewById(R.id.edit_confirm_password);
        checkOrganizer = findViewById(R.id.check_organizer);
        editCompany = findViewById(R.id.edit_company); // Initially hidden, shown if organizer
        buttonCreateAccount = findViewById(R.id.button_create_account);
        // progressBarCreateAccount = findViewById(R.id.progressBarCreateAccount); // Assuming you add this to your XML

        // Set initial visibility for company EditText
        editCompany.setVisibility(View.GONE);

        checkOrganizer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editCompany.setVisibility(View.VISIBLE);
            } else {
                editCompany.setVisibility(View.GONE);
            }
        });

        buttonCreateAccount.setOnClickListener(view -> attemptAccountCreation());
    }

    private String getTrimmedString(EditText input) {
        if (input.getText() == null) return "";
        return input.getText().toString().trim();
    }

    private void attemptAccountCreation() {
        final String firstName = getTrimmedString(editFirstName);
        final String lastName = getTrimmedString(editLastName);
        final String username = getTrimmedString(editUserName); // This will be stored for potential username login
        final String email = getTrimmedString(editEmail);
        final String phone = getTrimmedString(editPhone);
        final String password = getTrimmedString(editPassword); // Do not trim password, spaces can be intentional
        String confirmPassword = getTrimmedString(editConfirmPassword);
        final boolean isOrganizer = checkOrganizer.isChecked();
        final String companyName = isOrganizer ? getTrimmedString(editCompany) : null;

        // --- Start Input Validation ---
        if (TextUtils.isEmpty(firstName) || !InputValidator.isValidName(firstName)) {
            editFirstName.setError("Enter a valid first name");
            editFirstName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(lastName) || !InputValidator.isValidName(lastName)) {
            editLastName.setError("Enter a valid last name");
            editLastName.requestFocus();
            return;
        }
        // Username validation: ensure it's unique if you plan to use it as a login identifier
        // For now, just basic validation. Uniqueness check would be more complex (query DB).
        if (TextUtils.isEmpty(username) || !InputValidator.isValidUsername(username)) {
            editUserName.setError("Enter a valid username (e.g., alphanumeric, 3-20 chars)");
            editUserName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email) || !InputValidator.isValidEmail(email)) {
            editEmail.setError("Enter a valid email address");
            editEmail.requestFocus();
            return;
        }
        // Phone: Optional, but validate if provided
        if (!TextUtils.isEmpty(phone) && (phone.length() < 7 || !android.util.Patterns.PHONE.matcher(phone).matches())) {
            editPhone.setError("Enter a valid phone number or leave blank");
            editPhone.requestFocus();
            return;
        }
        // Password validation: Firebase requires min 6 chars.
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            editPassword.setError("Password must be at least 6 characters");
            editPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) { // Compare original password string
            editConfirmPassword.setError("Passwords do not match");
            editConfirmPassword.requestFocus();
            return;
        }
        if (isOrganizer && TextUtils.isEmpty(companyName)) {
            editCompany.setError("Company name is required for organizers");
            editCompany.requestFocus();
            return;
        }
        // --- End Input Validation ---

        // Show progress (e.g., progressBarCreateAccount.setVisibility(View.VISIBLE);)
        buttonCreateAccount.setEnabled(false); // Disable button to prevent multiple clicks
        Toast.makeText(CreateAccountActivity.this, "Creating account...", Toast.LENGTH_SHORT).show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> { // Using lambda for brevity
                    // Hide progress (e.g., progressBarCreateAccount.setVisibility(View.GONE);)
                    buttonCreateAccount.setEnabled(true); // Re-enable button

                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            // Optional: Set user's display name in Firebase Auth profile
                            // Display name in Auth is often set to the user's full name.
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(firstName + " " + lastName)
                                    .build();
                            firebaseUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            Log.d(TAG, "User profile updated in Auth.");
                                        }
                                    });

                            // Create User object (Participant or Organizer) for Realtime Database
                            User newUserProfile;
                            if (isOrganizer) {
                                Organizer organizer = new Organizer();
                                organizer.setUserID(userId);
                                organizer.setFirstName(firstName);
                                organizer.setLastName(lastName);
                                organizer.setUsername(username.toLowerCase()); // Store username in lowercase for case-insensitive lookup
                                organizer.setEmail(email); // Store original email from Auth
                                organizer.setRole("Organizer");
                                organizer.setPhoneNumber(phone);
                                organizer.setCompanyName(companyName);
                                newUserProfile = organizer;
                            } else {
                                Participant participant = new Participant();
                                participant.setUserID(userId);
                                participant.setFirstName(firstName);
                                participant.setLastName(lastName);
                                participant.setUsername(username.toLowerCase()); // Store username in lowercase
                                participant.setEmail(email);
                                participant.setRole("Participant");
                                participant.setPhoneNumber(phone);
                                newUserProfile = participant;
                            }

                            // Save user profile to Realtime Database
                            mDatabaseUsersRef.child(userId).setValue(newUserProfile)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(CreateAccountActivity.this, "Account created successfully!", Toast.LENGTH_LONG).show();
                                            // New user is created and profile saved.
                                            // You might want to sign them out so they have to log in.
                                            // mAuth.signOut();
                                            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish(); // Close CreateAccountActivity
                                        } else {
                                            Log.e(TAG, "Failed to save user profile to DB.", dbTask.getException());
                                            // Auth user was created, but DB save failed. This is an inconsistent state.
                                            // Inform user, and ideally, you might try to delete the Auth user.
                                            Toast.makeText(CreateAccountActivity.this, "Account created, but profile save failed. Please contact support or try again.", Toast.LENGTH_LONG).show();
                                            // Example: firebaseUser.delete().addOnCompleteListener(...);
                                        }
                                    });
                        } else {
                            Log.e(TAG, "FirebaseUser is null after successful Auth creation. This should not happen.");
                            Toast.makeText(CreateAccountActivity.this, "An unexpected error occurred after account creation. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        String errorMessage = "Account creation failed.";
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            errorMessage = "This email address is already in use.";
                            editEmail.setError(errorMessage);
                            editEmail.requestFocus();
                        } else if (task.getException() != null) {
                            errorMessage += " " + task.getException().getMessage();
                        }
                        Toast.makeText(CreateAccountActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}