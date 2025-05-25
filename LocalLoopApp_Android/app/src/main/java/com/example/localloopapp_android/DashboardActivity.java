package com.example.localloopapp_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";
    public static final String EXTRA_USER_FIRST_NAME = "com.example.localloopapp_android.USER_FIRST_NAME";
    public static final String EXTRA_USER_ROLE = "com.example.localloopapp_android.USER_ROLE";

    private TextView tvWelcomeMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("DashboardActivity", "onCreate reached!");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_USER_FIRST_NAME) && intent.hasExtra(EXTRA_USER_ROLE)) {
            String firstName = intent.getStringExtra(EXTRA_USER_FIRST_NAME);
            String role = intent.getStringExtra(EXTRA_USER_ROLE);

            if (firstName != null && role != null) {
                String welcomeText = "Welcome " + firstName + "! You are logged in as \"" + role + "\".";
                tvWelcomeMessage.setText(welcomeText);
            } else {
                tvWelcomeMessage.setText("Welcome! Role information is missing.");
                Toast.makeText(this, "Error: User details not fully provided.", Toast.LENGTH_LONG).show();
            }
        } else {
            tvWelcomeMessage.setText("Welcome! User details not found.");
            Toast.makeText(this, "Error: Could not retrieve user details.", Toast.LENGTH_LONG).show();
        }
    }
}
