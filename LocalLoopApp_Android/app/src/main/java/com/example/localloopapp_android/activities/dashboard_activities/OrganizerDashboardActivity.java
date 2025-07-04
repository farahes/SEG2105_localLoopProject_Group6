package com.example.localloopapp_android.activities.dashboard_activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.activities.CreateEventActivity;
import com.example.localloopapp_android.activities.dashboard_activities.ManageEventsActivity;
import com.example.localloopapp_android.models.Event;
import com.example.localloopapp_android.utils.Constants;
import com.example.localloopapp_android.viewmodels.OrganizerViewModel;
import com.example.localloopapp_android.utils.CalendarUtilsKt;


import com.kizitonwose.calendar.view.CalendarView;

import java.time.DayOfWeek;

import android.content.Intent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;

public class OrganizerDashboardActivity extends AppCompatActivity {

    private TextView tvPastEvents, tvUpcomingEvents, tvMonthTitle;
    private AppCompatButton btnToggleCalendar;
    private ImageButton btnPrevMonth, btnNextMonth;
    private View noEventsPlaceholder; // Placeholder for no events
    CalendarView calendarView;
    private OrganizerViewModel viewModel;
    private LinearLayout eventListContainer;
    private Set<LocalDate> eventDateSet = new HashSet<>();

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh events when returning to this activity
        viewModel.fetchEventsByOrganizer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_dashboard);

        extractIntentExtras();
        setupUI();
        setupViewModel();
        setupCalendar();
        setupFabButton();
    }


    //-------------------------Supporting-Methods---------------------------------

    /**
     * Extracts intent extras and initializes the ViewModel.
     * Sets the welcome message based on the first name provided in the intent.
     */
    private void extractIntentExtras() {
        String organizerId = getIntent().getStringExtra(Constants.EXTRA_USER_ID);
        String firstName = getIntent().getStringExtra(Constants.EXTRA_FIRST_NAME);

        viewModel = new ViewModelProvider(this).get(OrganizerViewModel.class);
        viewModel.setOrganizerId(organizerId);

        TextView welcomeText = findViewById(R.id.tvWelcomeMessage);
        welcomeText.setText(firstName != null
                ? "Welcome " + firstName + "! You are logged in as Organizer."
                : "Welcome, Organizer!");
    }

    /**
     * Sets up the UI components and binds them to their respective views.
     */
    private void setupUI() {

        eventListContainer = findViewById(R.id.eventListContainer);

        // Bind the calendar view and navigation buttons
        tvMonthTitle = findViewById(R.id.tvMonthTitle);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        calendarView = findViewById(R.id.calendarView);

        noEventsPlaceholder = findViewById(R.id.noEventsPlaceholder);
    }

    /**
     * Initializes the ViewModel and sets up the LiveData observer for events.
     * This will trigger the initial fetch of events when the activity is created.
     */
    private void setupViewModel() {
        viewModel.getEventsLiveData().observe(this, events -> {
            // Update eventDateSet here
            eventDateSet.clear();
            for (Event event : events) {
                eventDateSet.add(Instant.ofEpochMilli(event.getEventStart())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate());
            }
        });

        viewModel.fetchEventsByOrganizer();
    }

    /**
     * Sets up the Floating Action Button (FAB) to create a new event.
     * When clicked, it starts the ManageEventActivity with the organizer ID.
     */
    private void setupFabButton() {
        CardView fabCreateEvent = findViewById(R.id.fabCreateEvent);
        CardView manageEventsCard = findViewById(R.id.btnManageEvents);
        CardView manageRegistrationCard = findViewById(R.id.btnManageRegistration);

        fabCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEventActivity.class);
            intent.putExtra(Constants.EXTRA_USER_ID, viewModel.getOrganizerId());
            startActivity(intent);
        });

        manageEventsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageEventsActivity.class);
            intent.putExtra(Constants.EXTRA_USER_ID, viewModel.getOrganizerId());
            startActivity(intent);
        });

        manageRegistrationCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageRegistrationsActivity.class);
            startActivity(intent);
        });
    }

    //----------------------------------------------------------------------------

    /**
     * Displays upcoming events in the event list container.
     *
     * @param allEvents    The list of events to display.
     * @param showUpcoming Whether to show upcoming events (true) or past events (false).
     */
    private void displayEvents(List<Event> allEvents, boolean showUpcoming) {
        eventListContainer.removeAllViews(); // Clear previous cards
        LayoutInflater inflater = LayoutInflater.from(this);
        long now = System.currentTimeMillis();

        // Filter + sort
        List<Event> filteredEvents = allEvents.stream()
                .filter(event -> showUpcoming ? event.getEventStart() > now : event.getEventStart() <= now)
                .sorted(Comparator.comparingLong(Event::getEventStart))
                .toList();

        if (filteredEvents.isEmpty()) {
            noEventsPlaceholder.setVisibility(View.VISIBLE);
        } else {
            noEventsPlaceholder.setVisibility(View.GONE);
            // render event cards

            // Choose color
            String blob = showUpcoming ? "🟣" : "🔵";

            for (Event event : filteredEvents) {
                View card = inflater.inflate(R.layout.item_event_card, eventListContainer, false);

                TextView nameView = card.findViewById(R.id.tvEventName);
                TextView descView = card.findViewById(R.id.tvEventDescription);
                TextView feeView = card.findViewById(R.id.tvEventFee);
                TextView dateView = card.findViewById(R.id.tvEventDate);

                nameView.setText(blob + " " + event.getName());
                descView.setText(event.getDescription());
                if (event.getFee() == 0.0) {
                    feeView.setText("Free");
                    feeView.setTextColor(Color.parseColor("#4CAF50")); // Material green
                    feeView.setTypeface(null, Typeface.BOLD);
                } else {
                    feeView.setText("Fee: $" + event.getFee());
                    feeView.setTextColor(Color.BLACK); // to the normal color
                    feeView.setTypeface(null, Typeface.NORMAL);
                }

                dateView.setText("📅 " + Constants.formatDate(event.getEventStart()));

                card.setOnClickListener(v -> {
                    Intent intent = new Intent(this, CreateEventActivity.class);
                    intent.putExtra(Constants.EXTRA_USER_ID, viewModel.getOrganizerId());
                    intent.putExtra(Constants.EXTRA_EVENT_OBJECT, event);
                    startActivity(intent);
                });

                eventListContainer.addView(card);
            }

        }
    }


    /**
     * Sets up the calendar view with the current month and navigation buttons.
     * It also configures how each day looks based on the events.
     */
    private void setupCalendar() {
        // Set visible calendar range
        YearMonth current = YearMonth.now();
        calendarView.setup(current.minusMonths(6), current.plusMonths(6), DayOfWeek.MONDAY);
        calendarView.scrollToMonth(current);

        // Configure how each day looks
        CalendarUtilsKt.setupCalendar(calendarView, eventDateSet);

        // Toggle visibility
        calendarView.setVisibility(View.VISIBLE);
        findViewById(R.id.calendarHeader).setVisibility(View.VISIBLE);


        // Month name and navigation
        final YearMonth[] currentMonth = {current};
        updateMonthHeader(tvMonthTitle, currentMonth[0]);

        btnPrevMonth.setOnClickListener(v -> {
            currentMonth[0] = currentMonth[0].minusMonths(1);
            calendarView.scrollToMonth(currentMonth[0]);
            updateMonthHeader(tvMonthTitle, currentMonth[0]);
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth[0] = currentMonth[0].plusMonths(1);
            calendarView.scrollToMonth(currentMonth[0]);
            updateMonthHeader(tvMonthTitle, currentMonth[0]);
        });
    }


    // Helper method to update the month header in the calendar view
    private void updateMonthHeader(TextView header, YearMonth yearMonth) {
        String formatted = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " " + yearMonth.getYear();
        header.setText(formatted);
    }

}

