package com.example.localloopapp_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etIdentifier;
    private EditText etPassword;
    private Button btnLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsersRef;

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


        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsersRef = FirebaseDatabase.getInstance().getReference("users");

        btnLogin.setOnClickListener(v -> {
            String identifier = etIdentifier.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(identifier)) {
                etIdentifier.setError("Email or Username is required");
                etIdentifier.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password is required");
                etPassword.requestFocus();
                return;
            }

            attemptLogin(identifier, password);
        });
    }

    private void attemptLogin(final String identifier, final String password) {
        if (Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) {
            signInUserWithEmailAndPassword(identifier, password);
        } else {
            fetchEmailForUsernameAndSignIn(identifier.toLowerCase(), password);
        }
    }

    private void fetchEmailForUsernameAndSignIn(final String username, final String password) {
        Toast.makeText(LoginActivity.this, "Looking up username...", Toast.LENGTH_SHORT).show();

        Query usernameQuery = mDatabaseUsersRef.orderByChild("username").equalTo(username);

        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                            String email = user.getEmail();
                            Log.d(TAG, "Username '" + username + "' found. Associated email: " + email);
                            signInUserWithEmailAndPassword(email, password);
                            return;
                        } else {
                            Log.e(TAG, "User found for username '" + username + "' but email is missing or empty.");
                        }
                    }
                    Toast.makeText(LoginActivity.this, "Could not retrieve email for username. User data might be incomplete.", Toast.LENGTH_LONG).show();

                } else {
                    Log.w(TAG, "Username '" + username + "' not found in database.");
                    Toast.makeText(LoginActivity.this, "Login failed: Invalid username or password.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "fetchEmailForUsername:onCancelled", databaseError.toException());
                Toast.makeText(LoginActivity.this, "Failed to query database: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void signInUserWithEmailAndPassword(String email, String password) {
        Toast.makeText(LoginActivity.this, "Logging in...", Toast.LENGTH_SHORT).show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();
                                fetchUserProfileFromDatabaseAndProceed(userId);
                            } else {
                                Toast.makeText(LoginActivity.this, "Authentication failed: No user found after successful sign-in.",
                                        Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Authentication successful but firebaseUser is null");
                            }
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            String errorMessage = "Login failed: Invalid credentials.";
                            if (task.getException() != null && task.getException().getMessage() != null) {
                                Log.e(TAG, "Firebase Auth Error: " + task.getException().getMessage());
                            }
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void fetchUserProfileFromDatabaseAndProceed(String userId) {
        DatabaseReference userRef = mDatabaseUsersRef.child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User genericUser = dataSnapshot.getValue(User.class);

                    if (genericUser != null && genericUser.getRole() != null) {
                        String role = genericUser.getRole();
                        User specificUser = null;

                        if ("Participant".equalsIgnoreCase(role)) {
                            specificUser = dataSnapshot.getValue(Participant.class);
                        } else if ("Organizer".equalsIgnoreCase(role)) {
                            specificUser = dataSnapshot.getValue(Organizer.class);
                        } else if ("Admin".equalsIgnoreCase(role)) {
                            specificUser = dataSnapshot.getValue(Admin.class);
                        } else {
                            specificUser = genericUser;
                            Log.w(TAG, "Unknown role: " + role + ". Defaulting to base User.");
                        }

                        if (specificUser != null) {
                            if (specificUser.getUserID() == null) {
                                specificUser.setUserID(userId);
                            }

                            Log.d(TAG, "Successfully fetched user: " + specificUser.getFirstName() + ", Role: " + specificUser.getRole());
                            Toast.makeText(LoginActivity.this, "Login successful. Welcome " + specificUser.getFirstName(), Toast.LENGTH_SHORT).show();
                            navigateToDashboard(specificUser);
                        } else {
                            Toast.makeText(LoginActivity.this, "Could not parse user data for role: " + role, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Failed to parse specific user type for role: " + role + " UID: " + userId);
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "User data is incomplete (role missing).", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "User data incomplete for UID: " + userId);
                        mAuth.signOut();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User profile not found in database.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "No data found in database for authenticated UID: " + userId);
                    mAuth.signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "loadUser:onCancelled", databaseError.toException());
                Toast.makeText(LoginActivity.this, "Failed to load user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            }
        });
    }

    private void navigateToDashboard(User user) {
        String successMessage = "Login successful! User: " + user.getFirstName() + ", Role: " + user.getRole();
        if (user instanceof Organizer) {
            Organizer organizer = (Organizer) user;
            successMessage += ", Company: " + organizer.getCompanyName();
        } else if (user instanceof Participant) {
        }

        Log.d(TAG, "Navigate to Dashboard: " + successMessage);
        Toast.makeText(LoginActivity.this, successMessage, Toast.LENGTH_LONG).show();
    }
}