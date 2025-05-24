package com.example.localloopapp_android;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;

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

        // input fields and button below
        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnSubmitLogin);

        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // basic non-empty validation
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

            // Add Firebase validation here
            // validateUser(email, password);

            // Placeholder toast
            Toast.makeText(this,
                    "Logging in as " + email,
                    Toast.LENGTH_SHORT).show();
        });
    }
}

/**
 * Query user data for validation, something like this: -- I think this would be a private not public btw - GIO
 *
 * public void validateUser(String userId) {
 *     DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
 *     userRef.addListenerForSingleValueEvent(new ValueEventListener() {
 *         @Override
 *         public void onDataChange(DataSnapshot snapshot) {
 *             if (snapshot.exists()) {
 *                 User user = snapshot.getValue(User.class);
 *                 // Validate user credentials here
 *             } else {
 *                 // User not found
 *             }
 *         }
 *
 *         @Override
 *         public void onCancelled(DatabaseError error) {
 *             // Handle error
 *         }
 *     });
 * }
 */
