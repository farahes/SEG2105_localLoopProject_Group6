package com.example.localloopapp_android.activities.dashboard_activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.utils.Constants;

public class ParticipantDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_participant_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView welcomeText = findViewById(R.id.tvWelcomeMessage);

        String firstName = getIntent().getStringExtra(Constants.EXTRA_FIRST_NAME);
        welcomeText.setText(firstName != null
                ? "Welcome " + firstName + "! You are logged in as Participant."
                : "Welcome, Participant!");
    }
}
