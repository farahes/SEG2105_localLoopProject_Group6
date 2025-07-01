package com.example.localloopapp_android.activities.dashboard_activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.activities.CreateEventActivity;
import com.example.localloopapp_android.models.Event;
import com.example.localloopapp_android.utils.Constants;
import com.example.localloopapp_android.viewmodels.OrganizerViewModel;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.Intent;

import java.util.List;

public class OrganizerDashboardActivity extends AppCompatActivity {

    private TextView tvTotalEvents, tvUpcomingEvents;
    private OrganizerViewModel viewModel;
    private LinearLayout eventListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_dashboard);

        // Get the organizer ID from the intent extras
        String organizerId = getIntent().getStringExtra(Constants.EXTRA_USER_ID);

        // edge-to-edge mode for immersive experience (don't worry about it)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // UI setup
        TextView welcomeText = findViewById(R.id.tvWelcomeMessage);
        TextView tvTotalEvents = findViewById(R.id.tvTotalEvents);
        TextView tvUpcomingEvents = findViewById(R.id.tvUpcomingEvents);
        eventListContainer = findViewById(R.id.eventListContainer);

        String firstName = getIntent().getStringExtra(Constants.EXTRA_FIRST_NAME);
        welcomeText.setText(firstName != null
                ? "Welcome " + firstName + "! You are logged in as Organizer."
                : "Welcome, Organizer!");

        // ViewModel setup: initialize with organizerId passed to the constructor
        viewModel = new ViewModelProvider(this).get(OrganizerViewModel.class);
        viewModel.setOrganizerId(organizerId); // Set the organizer ID to fetch their events

        /** This LiveData observer will be triggered whenever the events list in the ViewModel changes.
         * It updates the UI with the latest events and statistics.
         */
        viewModel.getEventsLiveData().observe(this, events -> {
            // UI: refresh the cards
            displayMyEvents();

            // Stats
            tvTotalEvents.setText("Total Events: " + events.size());

            int upcomingCount = 0;
            long now = System.currentTimeMillis();
            for (Event e : events) {
                if (e.getEventStart() > now) {
                    upcomingCount++;
                }
            }

            tvUpcomingEvents.setText("Upcoming: " + upcomingCount);
        });

        // does not display the events. only triggers the data flow that eventually leads to displaying them
        viewModel.fetchEventsByOrganizer();

        FloatingActionButton fabCreateEvent = findViewById(R.id.fabCreateEvent);
        fabCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEventActivity.class);
            intent.putExtra(Constants.EXTRA_USER_ID, viewModel.getOrganizerId());
            startActivity(intent);
        });
    }

    private void displayMyEvents() {
        List<Event> events = viewModel.getMyEvents();
        eventListContainer.removeAllViews(); // Clear previous cards

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Event event : events) {
            View card = inflater.inflate(R.layout.item_event_card, eventListContainer, false);

            TextView nameView = card.findViewById(R.id.tvEventName);
            TextView descView = card.findViewById(R.id.tvEventDescription);
            TextView feeView = card.findViewById(R.id.tvEventFee);
            TextView dateView = card.findViewById(R.id.tvEventTime);

            nameView.setText("🟣 " + event.getName());
            descView.setText(event.getDescription());
            feeView.setText("Fee: $" + event.getFee());

            dateView.setText("📅 " + Constants.formatDate(event.getEventStart()));

            eventListContainer.addView(card);
        }
    }
}
