
package com.example.localloopapp_android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.localloopapp_android.models.RegistrationForm;
import com.example.localloopapp_android.utils.InputValidator;
import com.example.localloopapp_android.utils.Convenience;
import com.example.localloopapp_android.models.accounts.OrganizerAccount;
import com.example.localloopapp_android.models.accounts.ParticipantAccount;
import com.example.localloopapp_android.R;
import com.example.localloopapp_android.models.accounts.UserAccount;
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

    /**
     * Sets up the Create Account screen. Initializes Firebase, configures UI components,
     * and applies edge-to-edge layout. Hides the company name field unless "Organizer" is checked.
     * Registers a listener to trigger account creation on button press.
     */
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

        initializeUIElements();
        setupCompanyFieldToggle();
        setupCreateAccountButtonListener();
    }

    // region: UI setup
    /**
     * Initializes all UI components.
     */
    private void initializeUIElements() {
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
    }

    /**
     * Shows/hides the company name field based on the Organizer checkbox.
     */
    private void setupCompanyFieldToggle() {
        editCompany.setVisibility(View.GONE);
        checkOrganizer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editCompany.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
    }

    /**
     * Sets up the click listener to validate input and trigger account creation.
     */
    private void setupCreateAccountButtonListener() {
        buttonCreateAccount.setOnClickListener(view -> performInputValidationsAndCreateAccount());
    }
    // endregion


    // region: Account Creation
    /**
     * Gathers form input, validates each field, and initiates account creation.
     * Trims values, applies validation rules, checks password match, and ensures
     * company name is provided for organizers.
     * If valid, checks username uniqueness
     * and creates a Firebase account if available.
     */
    private void performInputValidationsAndCreateAccount() {
        RegistrationForm form = getUserInputFromForm();

        if (!validateInputFields(form)) {
            return;
        }

        setLoadingState(true);
        Toast.makeText(this, "Checking username...", Toast.LENGTH_SHORT).show();

        checkUsernameUniqueness(form.username, new UniquenessCallback() {
            @Override
            public void onResult(boolean isUnique) {
                if (isUnique) {
                    Toast.makeText(CreateAccountActivity.this, "Creating account...", Toast.LENGTH_SHORT).show();
                    createAccountWithValidatedInputs(form);
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

    /**
     * Validates all input fields and shows appropriate UI error messages.
     * Returns true if all validations pass, false otherwise.
     */
    private boolean validateInputFields(RegistrationForm form) {
        if (!Convenience.validateField(editFirstName, form.firstName, s -> !InputValidator.isValidName(s), "Enter a valid first name")) return false;
        if (!Convenience.validateField(editLastName, form.lastName, s -> !InputValidator.isValidName(s), "Enter a valid last name")) return false;
        if (!Convenience.validateField(editUserName, form.username, InputValidator::isValidUsername, "Enter a valid username \n(alphanumeric and between 3-20 characters long)")) return false;
        if (!Convenience.validateField(editEmail, form.email, InputValidator::isValidEmail, "Enter a valid email address")) return false;

        if (!TextUtils.isEmpty(form.phone) && !InputValidator.isValidPhoneNumber(form.phone)) {
            editPhone.setError("Enter a valid phone number or leave blank");
            editPhone.requestFocus();
            return false;
        }

        if (!Convenience.validateField(editPassword, form.password, InputValidator::isValidPassword, "Enter a valid password \n(must be between 8 and 16 characters long)")) return false;

        if (!form.password.equals(form.confirmPassword)) {
            editConfirmPassword.setError("Passwords do not match");
            editConfirmPassword.requestFocus();
            return false;
        }

        if (form.isOrganizer && TextUtils.isEmpty(form.companyName)) {
            editCompany.setError("Company name is required for organizers");
            editCompany.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Given validated inputs, creates a new account.
     * 1. Assumes inputs are valid
     * 2. Triggers Username uniqueness check
     * 3. Creates Firebase Auth account
     */
    private void createAccountWithValidatedInputs(RegistrationForm form) {
        setLoadingState(true);
        Toast.makeText(CreateAccountActivity.this, "Checking username...", Toast.LENGTH_SHORT).show();

        checkUsernameUniqueness(form.username, new UniquenessCallback() {
            @Override
            public void onResult(boolean isUnique) {
                if (isUnique) {
                    Toast.makeText(CreateAccountActivity.this, "Creating account...", Toast.LENGTH_SHORT).show();
                    createFirebaseUser(form);
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

    /**
     * Creates a Firebase Auth account and stores the user profile in the Realtime Database.
     * Sets up display name and initializes user data based on role (Organizer or Participant).
     * On success, navigates to the login screen; otherwise, displays appropriate error messages.
     */
    private void createFirebaseUser(final RegistrationForm form) {
        mAuth.createUserWithEmailAndPassword(form.email, form.password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            firebaseUser.updateProfile(
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(form.firstName + " " + form.lastName)
                                            .build()
                            );

                            UserAccount newUserProfile = buildNewUserProfile(userId, form);

                            mDatabaseUsersRef.child(userId).setValue(newUserProfile)
                                    .addOnCompleteListener(dbTask -> {
                                        setLoadingState(false);
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(this, LoginActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Log.e(TAG, "Failed to save user profile to DB.", dbTask.getException());
                                            Toast.makeText(this, "Account created, but profile save failed. Please contact support or try again.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            setLoadingState(false);
                            Log.e(TAG, "FirebaseUser is null after successful Auth creation.");
                            Toast.makeText(this, "An unexpected error occurred after account creation.", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Builds the Organizer or Participant object based on the form data.
     */
    private UserAccount buildNewUserProfile(String userId, RegistrationForm form) {
        if (form.isOrganizer) {
            OrganizerAccount organizer = new OrganizerAccount();
            organizer.setUserID(userId);
            organizer.setFirstName(form.firstName);
            organizer.setLastName(form.lastName);
            organizer.setUsername(form.username);
            organizer.setEmail(form.email);
            organizer.setPhoneNumber(form.phone);
            organizer.setCompanyName(form.companyName);
            organizer.setRole("Organizer");
            organizer.setStatusEnum(UserAccount.Status.ACTIVE);
            return organizer;
        } else {
            ParticipantAccount participant = new ParticipantAccount();
            participant.setUserID(userId);
            participant.setFirstName(form.firstName);
            participant.setLastName(form.lastName);
            participant.setUsername(form.username);
            participant.setEmail(form.email);
            participant.setPhoneNumber(form.phone);
            participant.setRole("Participant");
            participant.setStatusEnum(UserAccount.Status.ACTIVE);
            return participant;
        }
    }

    // endregion


    // region: Firebase Utilities
    private void checkUsernameUniqueness(final String usernameToCheck, final UniquenessCallback callback) {
        Query usernameQuery = mDatabaseUsersRef.orderByChild("username").equalTo(usernameToCheck);

        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callback.onResult(!dataSnapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError);
            }
        });
    }
    // endregion


    // region: Helpers
    /**
     * Extracts and trims form input into a RegistrationForm object.
     * createRegistrationFormFromInputFields() is better ?
     */
    private RegistrationForm getUserInputFromForm() {
        String firstName = Convenience.capitalizeFirstLetter(Convenience.getTrimmedString(editFirstName));
        String lastName = Convenience.capitalizeFirstLetter(Convenience.getTrimmedString(editLastName));
        String username = (Convenience.getTrimmedString(editUserName)).toLowerCase();
        String email = Convenience.getTrimmedString(editEmail);
        String phone = Convenience.getTrimmedString(editPhone);
        String password = editPassword.getText().toString();
        String confirmPassword = editConfirmPassword.getText().toString();
        boolean isOrganizer = checkOrganizer.isChecked();
        String companyName = isOrganizer ? Convenience.getTrimmedString(editCompany) : null;

        return new RegistrationForm(firstName, lastName, username, email, phone, password, confirmPassword, isOrganizer, companyName);
    }

    /**
     * Disables/enables all input fields and the create button
     * while network calls (username check / FirebaseAuth) are in progress.
     */
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

    interface UniquenessCallback {
        void onResult(boolean isUnique);
        void onError(DatabaseError error);
    }
    // endregion
}

/**
 * You can find the Clean Code Rule Book in notion, I made it for fun. It explains the structure of this code.
 *
 * I realise it may be a bit overkill to have this much structure for a simple Create Account screen,
 * but I wanted to ensure that the code is modular, testable, and easy to maintain.
 * This way, if we need to add more features or validations in the future,
 * we can do it more easily.
 *
 * And also I was kinda interested how far the stucturization can go since I've never really done it before.
 *
 * This note was almost fully written by Copilot, as I started typing it completed the rest,
 * exactly how I wanted to say it. Like it reads my mind. That's crazy.
 *
 * -- Mariia
 */
