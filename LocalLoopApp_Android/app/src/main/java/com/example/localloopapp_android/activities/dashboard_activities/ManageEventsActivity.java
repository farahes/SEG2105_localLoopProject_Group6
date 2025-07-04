package com.example.localloopapp_android.activities.dashboard_activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.models.Event;
import com.example.localloopapp_android.utils.Constants;
import com.example.localloopapp_android.viewmodels.OrganizerViewModel;
import com.example.localloopapp_android.activities.CreateEventActivity;


import java.util.Comparator;
import java.util.List;

public class ManageEventsActivity extends AppCompatActivity {

    private LinearLayout eventListContainer;
    private View noEventsPlaceholder;
    private OrganizerViewModel viewModel;

    private Button btnUpcoming, btnEventHistory;

    private List<Event> allEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_events);

        setupUI();
        setupViewModel();
        setupTabs();
    }

    private void setupUI() {
        // Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Containers
        eventListContainer = findViewById(R.id.eventListContainer);
        noEventsPlaceholder = findViewById(R.id.noEventsPlaceholder);

        // Tabs
        btnUpcoming = findViewById(R.id.btnUpcoming);
        btnEventHistory = findViewById(R.id.btnEventHistory);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(OrganizerViewModel.class);
        viewModel.getEventsLiveData().observe(this, events -> {
            allEvents = events;
            showUpcomingEvents(); // default
        });

        viewModel.fetchEventsByOrganizer();
    }

    private void setupTabs() {
        btnUpcoming.setOnClickListener(v -> {
            highlightTab(true);
            showUpcomingEvents();
        });

        btnEventHistory.setOnClickListener(v -> {
            highlightTab(false);
            showPastEvents();
        });
    }

    private void highlightTab(boolean isUpcoming) {
        if (isUpcoming) {
            btnUpcoming.setBackgroundTintList(getColorStateList(R.color.purple_700));
            btnUpcoming.setTextColor(getColor(R.color.white));

            btnEventHistory.setBackgroundTintList(getColorStateList(R.color.purple_200));
            btnEventHistory.setTextColor(getColor(R.color.purple_700));
        } else {
            btnUpcoming.setBackgroundTintList(getColorStateList(R.color.purple_200));
            btnUpcoming.setTextColor(getColor(R.color.purple_700));

            btnEventHistory.setBackgroundTintList(getColorStateList(R.color.purple_700));
            btnEventHistory.setTextColor(getColor(R.color.white));
        }
    }

    private void showUpcomingEvents() {
        displayFilteredEvents(true);
    }

    private void showPastEvents() {
        displayFilteredEvents(false);
    }

    private void displayFilteredEvents(boolean upcoming) {
        eventListContainer.removeAllViews();
        long now = System.currentTimeMillis();

        List<Event> filtered = allEvents.stream()
                .filter(e -> upcoming ? e.getEventStart() > now : e.getEventStart() <= now)
                .sorted(Comparator.comparingLong(Event::getEventStart))
                .toList();

        if (filtered.isEmpty()) {
            noEventsPlaceholder.setVisibility(View.VISIBLE);
        } else {
            noEventsPlaceholder.setVisibility(View.GONE);
            LayoutInflater inflater = LayoutInflater.from(this);

            for (Event e : filtered) {
                View card = inflater.inflate(R.layout.item_event_card, eventListContainer, false);

                TextView nameView = card.findViewById(R.id.tvEventName);
                TextView locationView = card.findViewById(R.id.tvEventLocation);
                TextView dateView = card.findViewById(R.id.tvEventDate);

                nameView.setText(e.getName());
                locationView.setText(e.getLocation());
                dateView.setText(Constants.formatDate(e.getEventStart()));

                ImageButton btnEdit = card.findViewById(R.id.btnEdit);
                ImageButton btnDelete = card.findViewById(R.id.btnDelete);

                btnEdit.setOnClickListener(v -> {
                    Intent intent = new Intent(this, CreateEventActivity.class);
                    intent.putExtra(Constants.EXTRA_USER_ID, viewModel.getOrganizerId());
                    intent.putExtra(Constants.EXTRA_EVENT_OBJECT, e);
                    startActivity(intent);
                });

                btnDelete.setOnClickListener(v -> {
                    // TODO: Show confirmation and delete logic
                });

                eventListContainer.addView(card);
            }
        }
    }
}
