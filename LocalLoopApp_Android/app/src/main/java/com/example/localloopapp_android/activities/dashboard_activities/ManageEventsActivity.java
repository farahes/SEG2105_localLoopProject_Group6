package com.example.localloopapp_android.activities.dashboard_activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.activities.CreateEventActivity;
import com.example.localloopapp_android.models.Event;
import com.example.localloopapp_android.utils.Constants;
import com.example.localloopapp_android.viewmodels.OrganizerViewModel;

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
        applyInitialTabStyle();

    }


    private void setupUI() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        eventListContainer = findViewById(R.id.eventListContainer);
        noEventsPlaceholder = findViewById(R.id.noEventsPlaceholder);

        btnUpcoming = findViewById(R.id.btnUpcoming);
        btnEventHistory = findViewById(R.id.btnEventHistory);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(OrganizerViewModel.class);
        String organizerId = getIntent().getStringExtra(Constants.EXTRA_USER_ID);
        viewModel.setOrganizerId(organizerId);
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
            btnUpcoming.setBackgroundResource(R.drawable.bg_tab_left_selected);
            btnUpcoming.setTextColor(ContextCompat.getColor(this, android.R.color.white));

            btnEventHistory.setBackgroundResource(R.drawable.bg_tab_right_unselected);
            btnEventHistory.setTextColor(ContextCompat.getColor(this, R.color.purple_700));
        } else {
            btnUpcoming.setBackgroundResource(R.drawable.bg_tab_left_unselected);
            btnUpcoming.setTextColor(ContextCompat.getColor(this, R.color.purple_700));

            btnEventHistory.setBackgroundResource(R.drawable.bg_tab_right_selected);
            btnEventHistory.setTextColor(ContextCompat.getColor(this, android.R.color.white));
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

                TextView timeView = card.findViewById(R.id.tvEventTime);
                TextView categoryView = card.findViewById(R.id.tvEventCategory);

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
                    viewModel.deleteEvent(e);
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                });

                eventListContainer.addView(card);
            }
        }
    }
    private void applyInitialTabStyle() {
        // Set default: UPCOMING selected
        btnUpcoming.setBackgroundResource(R.drawable.bg_tab_left_selected);
        btnUpcoming.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        btnEventHistory.setBackgroundResource(R.drawable.bg_tab_right_unselected);
        btnEventHistory.setTextColor(ContextCompat.getColor(this, R.color.purple_700));
    }


}
