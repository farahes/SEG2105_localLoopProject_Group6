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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CreateAccountActivity extends AppCompatActivity {

    private static final String TAG = "CreateAccountActivity";

    private DatabaseReference mDatabaseUsersRef;
    private FirebaseAuth mAuth;

    private EditText editFirstName, editLastName, editUserName, editEmail, editPhone, editPassword, editConfirmPassword, editCompany;
    private CheckBox checkOrganizer;
    private Button buttonCreateAccount;

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
        editCompany = findViewById(R.id.edit_company);
        buttonCreateAccount = findViewById(R.id.button_create_account);

        editCompany.setVisibility(View.GONE);

        checkOrganizer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editCompany.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        buttonCreateAccount.setOnClickListener(view -> performInputValidationsAndCreateAccount());
    }

    private String getTrimmedString(EditText input) {
        if (input.getText() == null) return "";
        return input.getText().toString().trim();
    }

    private void setLoadingState(boolean isLoading) {
        buttonCreateAccount.setEnabled(!isLoading);
        editFirstName.setEnabled(!isLoading);
        editLastName.setEnabled(!isLoading);
        editUserName.setEnabled(!isLoading);
        editEmail.setEnabled(!isLoading);
        editPhone.setEnabled(!isLoading);
        editPassword.setEnabled(!isLoading);
        editConfirmPassword.setEnabled(!isLoading);
        checkOrganizer.setEnabled(!isLoading);
        editCompany.setEnabled(!isLoading);
    }

    private void performInputValidationsAndCreateAccount() {
        final String firstName = getTrimmedString(editFirstName);
        final String lastName = getTrimmedString(editLastName);
        final String username = getTrimmedString(editUserName);
        final String email = getTrimmedString(editEmail);
        final String phone = getTrimmedString(editPhone);
        final String password = editPassword.getText().toString();
        String confirmPassword = editConfirmPassword.getText().toString();
        final boolean isOrganizer = checkOrganizer.isChecked();
        final String companyName = isOrganizer ? getTrimmedString(editCompany) : null;

        if (TextUtils.isEmpty(firstName) || InputValidator.isValidName(firstName)) {
            editFirstName.setError("Enter a valid first name");
            editFirstName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(lastName) || InputValidator.isValidName(lastName)) {
            editLastName.setError("Enter a valid last name");
            editLastName.requestFocus();
            return;
        }
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
        if (!TextUtils.isEmpty(phone) && !InputValidator.isValidPhoneNumber(phone)) {
            editPhone.setError("Enter a valid phone number or leave blank");
            editPhone.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || !InputValidator.isValidPassword(password)) {
            editPassword.setError("Password must be at least 6 characters");
            editPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("Passwords do not match");
            editConfirmPassword.requestFocus();
            return;
        }
        if (isOrganizer && TextUtils.isEmpty(companyName)) {
            editCompany.setError("Company name is required for organizers");
            editCompany.requestFocus();
            return;
        }

        setLoadingState(true);
        Toast.makeText(CreateAccountActivity.this, "Checking username...", Toast.LENGTH_SHORT).show();

        checkUsernameUniqueness(username.toLowerCase(), new UniquenessCallback() {
            @Override
            public void onResult(boolean isUnique) {
                if (isUnique) {
                    Toast.makeText(CreateAccountActivity.this, "Creating account...", Toast.LENGTH_SHORT).show();
                    createFirebaseUser(firstName, lastName, username, email, phone, password, isOrganizer, companyName);
                } else {
                    setLoadingState(false);
                    editUserName.setError("This username is already taken. Please choose another.");
                    editUserName.requestFocus();
                    Toast.makeText(CreateAccountActivity.this, "Username already exists.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(DatabaseError error) {
                setLoadingState(false);
                Log.w(TAG, "Username uniqueness check failed:", error.toException());
                Toast.makeText(CreateAccountActivity.this, "Error checking username. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface UniquenessCallback {
        void onResult(boolean isUnique);
        void onError(DatabaseError error);
    }

    private void checkUsernameUniqueness(final String usernameToCheck, final UniquenessCallback callback) {
        Query usernameQuery = mDatabaseUsersRef.orderByChild("username").equalTo(usernameToCheck);

        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    callback.onResult(false);
                } else {
                    callback.onResult(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError);
            }
        });
    }

    private void createFirebaseUser(final String firstName, final String lastName, final String username,
                                    final String email, final String phone, final String password,
                                    final boolean isOrganizer, final String companyName) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(firstName + " " + lastName)
                                    .build();
                            firebaseUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            Log.d(TAG, "User profile updated in Auth.");
                                        }
                                    });

                            User newUserProfile;
                            String role = isOrganizer ? "Organizer" : "Participant";
                            if (isOrganizer) {
                                Organizer organizer = new Organizer();
                                organizer.setUserID(userId);
                                organizer.setFirstName(firstName);
                                organizer.setLastName(lastName);
                                organizer.setUsername(username.toLowerCase());
                                organizer.setEmail(email);
                                organizer.setRole(role);
                                organizer.setPhoneNumber(phone);
                                organizer.setCompanyName(companyName);
                                newUserProfile = organizer;
                            } else {
                                Participant participant = new Participant();
                                participant.setUserID(userId);
                                participant.setFirstName(firstName);
                                participant.setLastName(lastName);
                                participant.setUsername(username.toLowerCase());
                                participant.setEmail(email);
                                participant.setRole(role);
                                participant.setPhoneNumber(phone);
                                newUserProfile = participant;
                            }

                            mDatabaseUsersRef.child(userId).setValue(newUserProfile)
                                    .addOnCompleteListener(dbTask -> {
                                        setLoadingState(false);
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(CreateAccountActivity.this, "Account created successfully!", Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Log.e(TAG, "Failed to save user profile to DB.", dbTask.getException());
                                            Toast.makeText(CreateAccountActivity.this, "Account created, but profile save failed. Please contact support or try again.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            setLoadingState(false);
                            Log.e(TAG, "FirebaseUser is null after successful Auth creation.");
                            Toast.makeText(CreateAccountActivity.this, "An unexpected error occurred after account creation.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        setLoadingState(false);
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