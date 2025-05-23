package com.example.localloopapp_android;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

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
    }
}

/**
 * Query user data for validation, something like this:
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