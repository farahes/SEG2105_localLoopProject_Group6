package com.example.localloopapp_android;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.UUID;

public class CreateAccountActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private EditText editFirstName, editLastName, editUserName, editEmail, editPhone, editPassword, editConfirmPassword;
    private Button buttonCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialising views
        editFirstName = findViewById(R.id.edit_first_name);
        editLastName = findViewById(R.id.edit_last_name);
        editUserName = findViewById(R.id.edit_username);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        editPassword = findViewById(R.id.edit_password);
        editConfirmPassword = findViewById(R.id.edit_confirm_password);
        buttonCreateAccount = findViewById(R.id.button_create_account);

        buttonCreateAccount.setOnClickListener(view -> {
            createAccount();
            // on success, move back to the main activity
        });
    }

    private void createAccount() {
        String firstName = getTrimmedString(editFirstName);
        String lastName = getTrimmedString(editLastName);
        String username = getTrimmedString(editUserName);
        String email = getTrimmedString(editEmail);
        String phone = getTrimmedString(editPhone);
        String password = getTrimmedString(editPassword);
        String confirmPassword = getTrimmedString(editConfirmPassword);

        // Validate input (using InputValidatorClass)
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
        if (TextUtils.isEmpty(username) || !InputValidator.isValidUsername(username)) {
            editUserName.setError("Enter a valid username");
            editUserName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email) || !InputValidator.isValidEmail(email)) {
            editEmail.setError("Enter a valid email");
            editEmail.requestFocus();
            return;
        }
        if (!TextUtils.isEmpty(phone) && phone.length() < 7) {
            editPhone.setError("Enter a valid phone number");
            editPhone.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || !InputValidator.isValidPassword(password)) {
            editPassword.setError("Password must be at least 8 characters");
            editPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("Passwords do not match");
            editConfirmPassword.requestFocus();
            return;
        }

        // Generate unique userID
        String userID = UUID.randomUUID().toString();

        // Create Participant user
        Participant newUser = new Participant(userID, firstName, lastName, username, email, "Participant");

        // Save to Firebase
        mDatabase.child(userID).setValue(newUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CreateAccountActivity.this, "Account created successfully!", Toast.LENGTH_LONG).show();
                finish();  // Close activity and maybe go back to login
            } else {
                Toast.makeText(CreateAccountActivity.this, "Failed to create account. Try again. Or go make yourself some tea, it might take forever.", Toast.LENGTH_LONG).show();
            }
        });
    }

    public String getTrimmedString(EditText input){ return input.getText().toString().trim(); }

}

/**
 * lambda example:
 *
 * textCreateAccount.setOnClickListener(view -> {
 *     Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
 *     startActivity(intent);
 * });
 */