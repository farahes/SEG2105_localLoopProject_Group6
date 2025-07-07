package com.example.localloopapp_android.activities.dashboard_activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.localloopapp_android.models.Category;
import com.example.localloopapp_android.viewmodels.CategoryViewModel;

import java.util.HashMap;
import java.util.Map;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.activities.ManageEventActivity;
import com.example.localloopapp_android.models.Event;
import com.example.localloopapp_android.utils.Constants;
import com.example.localloopapp_android.viewmodels.OrganizerViewModel;

import java.util.Comparator;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;


public class ManageEventsActivity extends AppCompatActivity {
    private Button btnUpcoming, btnEventHistory;
    private LinearLayout eventListContainer;
    private View noEventsPlaceholder;
    private OrganizerViewModel viewModel;

    private List<Event> allEvents;

    private CategoryViewModel categoryViewModel;
    private Map<String, String> categoryIdToName = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_events);

        setupUI();
        setupViewModel();
        setupTabs();

        applyInitialTabStyle();

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        categoryViewModel.getCategories().observe(this, categories -> {
            categoryIdToName.clear();
            for (Category c : categories) {
                categoryIdToName.put(c.getCategoryId(), c.getName());
            }
            // Optionally refresh event list UI here if needed
        });

        // Bind views
        btnUpcoming = findViewById(R.id.btnUpcoming);
        btnEventHistory = findViewById(R.id.btnEventHistory);
        eventListContainer = findViewById(R.id.eventListContainer);
        noEventsPlaceholder = findViewById(R.id.noEventsPlaceholder);


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
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        String organizerId = getIntent().getStringExtra(Constants.EXTRA_USER_ID);
        viewModel.setOrganizerId(organizerId);
        viewModel.getEventsLiveData().observe(this, events -> {
            allEvents = events;
            showUpcomingEvents(); // default
        });

        viewModel.fetchEventsByOrganizer();

        // Observe categories
        categoryViewModel.getCategories().observe(this, categories -> {
            categoryIdToName.clear();
            for (Category category : categories) {
                categoryIdToName.put(category.getCategoryId(), category.getName());
            }
        });
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
            btnEventHistory.setTextColor(ContextCompat.getColor(this, R.color.purple_200));  // lighter purple here
        } else {
            btnUpcoming.setBackgroundResource(R.drawable.bg_tab_left_unselected);
            btnUpcoming.setTextColor(ContextCompat.getColor(this, R.color.purple_200));  // lighter purple here

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
        if (allEvents == null) return; // Prevents NullPointerException
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
                View card = inflater.inflate(R.layout.item_organizer_event_card, eventListContainer, false);

                // Load and display the event image as a small round avatar
                ImageView ivEventAvatar = card.findViewById(R.id.ivEventAvatar);
                com.example.localloopapp_android.activities.ManageEventActivity.loadEventImage(e.getEventId(), ivEventAvatar);

                TextView nameView = card.findViewById(R.id.tvEventName);
                TextView locationView = card.findViewById(R.id.tvEventLocation);
                TextView dateView = card.findViewById(R.id.tvEventDate);

                TextView timeView = card.findViewById(R.id.tvEventTime);
                long startMillis = e.getEventStart();
                long endMillis = e.getEventEnd();
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String timeStr = timeFormat.format(new Date(startMillis)) + " - " + timeFormat.format(new Date(endMillis));
                timeView.setText(timeStr);
                

                // Set category name
                String categoryName = categoryIdToName.get(e.getCategoryId());
                TextView tvCategory = card.findViewById(R.id.tvEventCategory);
                tvCategory.setText(categoryName != null ? categoryName : "Unknown Category");

                // Set event description
                TextView tvDesc = card.findViewById(R.id.tvEventDescription);
                tvDesc.setText(e.getDescription());

                // Set event fee
                TextView tvFee = card.findViewById(R.id.tvEventFee);
                if (e.getFee() == 0.0) {
                    tvFee.setText("Free");
                    tvFee.setTextColor(ContextCompat.getColor(this, R.color.green));
                    tvFee.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    tvFee.setText("Fee: $" + String.format("%.2f", e.getFee()));
                    tvFee.setTextColor(ContextCompat.getColor(this, R.color.cat_grey));
                    tvFee.setTypeface(null, android.graphics.Typeface.NORMAL);
                }

                nameView.setText(e.getName());
                locationView.setText(e.getLocation());
                dateView.setText(Constants.formatDate(e.getEventStart()));

                ImageButton btnEdit = card.findViewById(R.id.btnEdit);

                btnEdit.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ManageEventActivity.class);
                    intent.putExtra(Constants.EXTRA_USER_ID, viewModel.getOrganizerId());
                    intent.putExtra(Constants.EXTRA_EVENT_OBJECT, e);
                    startActivity(intent);
                });


                eventListContainer.addView(card);
            }
        }
    }

    private void applyInitialTabStyle() {
        btnUpcoming.setBackgroundResource(R.drawable.bg_tab_left_selected);
        btnUpcoming.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        btnEventHistory.setBackgroundResource(R.drawable.bg_tab_right_unselected);
        btnEventHistory.setTextColor(ContextCompat.getColor(this, R.color.purple_200));  // Use lighter purple here
    }


}
