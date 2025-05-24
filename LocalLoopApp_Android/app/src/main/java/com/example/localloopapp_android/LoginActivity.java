package com.example.localloopapp_android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRoot; // Renamed for clarity, points to root

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

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnSubmitLogin);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRoot = FirebaseDatabase.getInstance().getReference(); // Get root reference

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Required");
                etEmail.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("Required");
                etPassword.requestFocus();
                return;
            }

            // Call the method that contains the Firebase Auth logic
            signInUserWithEmailAndPassword(email, password);
        });
    }

    // This is where the Firebase Authentication logic goes
    private void signInUserWithEmailAndPassword(String email, String password) {
        Toast.makeText(LoginActivity.this, "Logging in...", Toast.LENGTH_SHORT).show();
        // Show progress bar here

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() { // Ensure 'this' refers to the Activity context
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Hide progress bar here
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid(); // THIS IS THE KEY
                                // Now you have the userId, you can fetch their profile data
                                fetchUserProfileFromDatabaseAndProceed(userId);
                            } else {
                                // This case should ideally not happen if task is successful but good to handle
                                Toast.makeText(LoginActivity.this, "Authentication failed: No user found after successful sign-in.",
                                        Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Authentication successful but firebaseUser is null");
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown authentication error";
                            Toast.makeText(LoginActivity.this, "Authentication failed: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    // Renamed this method for clarity to match its new trigger point
    private void fetchUserProfileFromDatabaseAndProceed(String userId) {
        DatabaseReference userRef = mDatabaseRoot.child("users").child(userId); // Reference specific user

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User genericUser = dataSnapshot.getValue(User.class); // Get generic user to read role

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
                            if (specificUser.getUserID() == null) { // Double check if UID was set by Firebase
                                specificUser.setUserID(userId); // Manually set if not automatically populated
                            }

                            Log.d(TAG, "Successfully fetched user: " + specificUser.getFirstName() + ", Role: " + specificUser.getRole());
                            Toast.makeText(LoginActivity.this, "Login successful. Welcome " + specificUser.getFirstName(), Toast.LENGTH_SHORT).show();

                            navigateToDashboard(specificUser);
                        } else {
                            Toast.makeText(LoginActivity.this, "Could not parse user data for role: " + role, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Failed to parse specific user type for role: " + role + " UID: " + userId);
                            mAuth.signOut(); // Sign out if essential user data can't be parsed
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "User data is incomplete (role missing).", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "User data incomplete for UID: " + userId);
                        mAuth.signOut(); // Sign out if essential user data is missing
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
            }
        });
    }


    private void navigateToDashboard(User user) {
        // ---- TEMPORARY TEST CODE ----
        String successMessage = "Login successful! User: " + user.getFirstName() + ", Role: " + user.getRole();
        if (user instanceof Organizer) {
            Organizer organizer = (Organizer) user;
            successMessage += ", Company: " + organizer.getCompanyName(); // Example for Organizer
        }

        Log.d(TAG, "navigateToDashboard (TEMPORARY): " + successMessage);
        Toast.makeText(LoginActivity.this, successMessage, Toast.LENGTH_LONG).show();

        // finish(); // You MIGHT want to call finish() if you want the login screen to close
        // But for testing, maybe you want to stay on it to try another login.
        // If you call finish(), and this is your launcher activity, the app might close.
        // For now, let's keep it commented out to stay on the LoginActivity.

        // ---- ORIGINAL NAVIGATION CODE (Commented out for now) ----
    /*
    Intent intent = new Intent(LoginActivity.this, NewDashboardActivity.class);
    // Optional: Pass data. If User is Parcelable:
    // intent.putExtra("USER_OBJECT", user);
    startActivity(intent);
    finish();
    */
    }
}