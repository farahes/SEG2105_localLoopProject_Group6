package com.example.localloopapp_android.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.activities.ManageEventActivity;
import com.example.localloopapp_android.models.Category;
import com.example.localloopapp_android.models.Event;
import com.example.localloopapp_android.models.Registration;
import com.example.localloopapp_android.viewmodels.CategoryViewModel;
import com.example.localloopapp_android.viewmodels.EventViewModel;
import com.example.localloopapp_android.viewmodels.RegistrationViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.*;

import android.location.Address;
import android.location.Geocoder;
import android.content.Context;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.bumptech.glide.Glide;

public class ParticipantEventSearchActivity extends AppCompatActivity {

    private EditText etSearchBar;
    private LinearLayout recentQueriesContainer;
    private TextView tvSelectedCategories, tvSelectedDate, tvStartTime;
    private Button btnSelectCategories, btnSelectDate, btnStartTime, btnSearch, btnFilters;
    private ImageView loadingGif;
    private Spinner feeSpinner;
    private LinearLayout resultsContainer;
    ArrayAdapter<String> feeAdapter;

    private List<Category> allCategories = new ArrayList<>();
    private boolean[] selectedCategories;
    private boolean hasSearched = false;
    private List<String> selectedCategoryIds = new ArrayList<>();
    private Calendar selectedDate = null;
    private Integer startHour = null, startMinute = null;
    private String selectedFeeOption = "Any";

    private CategoryViewModel categoryViewModel;
    private EventViewModel eventViewModel;
    private RegistrationViewModel registrationViewModel;
    private Map<String, String> registrationStatusMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_event_search);

        setupUI();
        setupEventObserver();
        setupRegistrationObserver();

        // Load categories
        categoryViewModel.getCategories().observe(this, categories -> {
            allCategories = categories;
            selectedCategories = new boolean[categories.size()];
        });
        categoryViewModel.fetchCategories();

        // Show 5 upcoming events by default
        eventViewModel.getEvents().observe(this, events -> {
            if (hasSearched) return; // Don't show default if searching
            List<Event> upcoming = new ArrayList<>();
            long now = System.currentTimeMillis();
            for (Event event : events) {
                if (event.isEventActive() && event.getEventStart() >= now) {
                    upcoming.add(event);
                }
            }
            upcoming.sort(Comparator.comparingLong(Event::getEventStart));
            resultsContainer.removeAllViews();
            for (int i = 0; i < Math.min(5, upcoming.size()); i++) {
                Event event = upcoming.get(i);
                View card = getLayoutInflater().inflate(R.layout.item_participant_event_card, resultsContainer, false);
                populateEventCard(card, event);
                resultsContainer.addView(card);
            }
        });
        eventViewModel.fetchEvents();

        setupListeners();
    }

    private void setupUI() {
        etSearchBar = findViewById(R.id.etSearchBar);
        recentQueriesContainer = findViewById(R.id.recentQueriesContainer);
        btnSearch = findViewById(R.id.btnSearchEvents);
        btnFilters = findViewById(R.id.btnFilters);
        resultsContainer = findViewById(R.id.resultsContainer);
        loadingGif = findViewById(R.id.loadingGif);
        Glide.with(this).asGif().load(R.drawable.ic_loading_packman).into(loadingGif);

        feeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Any", "Free", "< $50", "> $50"});
        feeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        registrationViewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> searchEvents());
        btnFilters.setOnClickListener(v -> showFiltersDialog());

        etSearchBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) loadRecentQueries();
        });
    }

    private void setupRegistrationObserver() {
        String participantId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        registrationViewModel.loadParticipantRegistrations(participantId);
        registrationViewModel.getParticipantRegistrations().observe(this, registrations -> {
            registrationStatusMap.clear();
            for (Registration r : registrations) {
                registrationStatusMap.put(r.getEventId(), r.getStatus());
            }
            if (hasSearched) {
                eventViewModel.fetchEvents();
            }
        });
    }

    private void setupEventObserver() {
        eventViewModel.getEvents().observe(this, events -> {
            if (!hasSearched) {
                resultsContainer.removeAllViews();
                loadingGif.setVisibility(View.GONE);
                resultsContainer.setVisibility(View.VISIBLE);
                return;
            }
            loadingGif.setVisibility(View.GONE);
            resultsContainer.setVisibility(View.VISIBLE);
            resultsContainer.removeAllViews();

            String searchQuery = etSearchBar.getText().toString().trim().toLowerCase();
            String feeOption = selectedFeeOption;

            for (Event event : events) {
                // Search bar similarity (name or description)
                boolean matchesQuery = TextUtils.isEmpty(searchQuery)
                        || (event.getName() != null && event.getName().toLowerCase().contains(searchQuery))
                        || (event.getDescription() != null && event.getDescription().toLowerCase().contains(searchQuery));
                if (!matchesQuery) continue;

                // Category filter
                if (!selectedCategoryIds.isEmpty() && (event.getCategoryId() == null ||
                        !selectedCategoryIds.contains(event.getCategoryId()))) continue;

                // Fee filter
                double fee = event.getFee();
                if ("Free".equals(feeOption) && fee != 0) continue;
                if ("< $50".equals(feeOption) && !(fee > 0 && fee < 50)) continue;
                if ("> $50".equals(feeOption) && !(fee > 50)) continue;

                // Date and start time filter
                if (selectedDate != null) {
                    Calendar eventCal = Calendar.getInstance();
                    eventCal.setTime(new Date(event.getEventStart()));
                    if (eventCal.get(Calendar.YEAR) != selectedDate.get(Calendar.YEAR) ||
                            eventCal.get(Calendar.MONTH) != selectedDate.get(Calendar.MONTH) ||
                            eventCal.get(Calendar.DAY_OF_MONTH) != selectedDate.get(Calendar.DAY_OF_MONTH))
                        continue;
                    int eventHour = eventCal.get(Calendar.HOUR_OF_DAY);
                    int eventMinute = eventCal.get(Calendar.MINUTE);
                    int eventTime = eventHour * 60 + eventMinute;
                    if (startHour != null && startMinute != null) {
                        int startTime = startHour * 60 + startMinute;
                        if (eventTime < startTime) continue;
                    }
                }

                View card = getLayoutInflater().inflate(R.layout.item_participant_event_card, resultsContainer, false);
                populateEventCard(card, event);
                resultsContainer.addView(card);
            }
            if (resultsContainer.getChildCount() == 0) {
                TextView tv = new TextView(this);
                tv.setText("No events found.");
                resultsContainer.addView(tv);
            }
        });
    }

    private void populateEventCard(View card, Event event) {
        ImageView ivEventImage = card.findViewById(R.id.ivEventAvatar);
        ManageEventActivity.loadEventImage(event.getEventId(), ivEventImage);
        ((TextView) card.findViewById(R.id.tvEventName)).setText(event.getName());
        ((TextView) card.findViewById(R.id.tvEventDescription)).setText(event.getDescription());
        ((TextView) card.findViewById(R.id.tvEventCategory)).setText(getCategoryName(event));
        TextView tvFee = card.findViewById(R.id.tvEventFee);
        if (event.getFee() == 0.0) {
            tvFee.setText("Free");
            tvFee.setTextColor(getResources().getColor(R.color.green, null));
            tvFee.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            tvFee.setText("$" + event.getFee());
            tvFee.setTextColor(getResources().getColor(android.R.color.black, null));
            tvFee.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String dateStr = dateFormat.format(new Date(event.getEventStart()));
        String timeStr = timeFormat.format(new Date(event.getEventStart())) + " - " + timeFormat.format(new Date(event.getEventEnd()));
        ((TextView) card.findViewById(R.id.tvEventDate)).setText(dateStr);
        ((TextView) card.findViewById(R.id.tvEventTime)).setText(timeStr);

        MapView mapView = card.findViewById(R.id.mapView);
        mapView.onCreate(null);
        mapView.getMapAsync(googleMap -> {
            LatLng location = getLocationFromAddress(card.getContext(), event.getLocation());
            if (location != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                googleMap.addMarker(new MarkerOptions().position(location).title(event.getLocation()));
            }
        });

        Button btnRegister = card.findViewById(R.id.btnRegisterEvent);
        String status = registrationStatusMap.get(event.getEventId());
        if (status != null) {
            btnRegister.setText(status);
            btnRegister.setEnabled(false);
        } else {
            btnRegister.setText("Register");
            btnRegister.setEnabled(true);
            btnRegister.setOnClickListener(v -> registerForEvent(event));
        }
    }

    // Dialogs and pickers
    private void showCategoryDialog() {
        String[] categoryNames = new String[allCategories.size()];
        for (int i = 0; i < allCategories.size(); i++) {
            categoryNames[i] = allCategories.get(i).getName();
        }
        new AlertDialog.Builder(this)
                .setTitle("Select Categories")
                .setMultiChoiceItems(categoryNames, selectedCategories, (dialog, which, isChecked) -> {
                    selectedCategories[which] = isChecked;
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    selectedCategoryIds.clear();
                    for (int i = 0; i < selectedCategories.length; i++) {
                        if (selectedCategories[i]) {
                            selectedCategoryIds.add(allCategories.get(i).getCategoryId());
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDatePicker(@Nullable TextView targetView) {
        final Calendar now = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            if (targetView != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                targetView.setText(sdf.format(selectedDate.getTime()));
            }
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker(@Nullable TextView targetView) {
        final Calendar now = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            startHour = hourOfDay;
            startMinute = minute;
            if (targetView != null) {
                targetView.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void showFiltersDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_event_filters, null);

        // Category multi-select
        Button btnDialogCategories = dialogView.findViewById(R.id.btnDialogCategories);
        TextView tvDialogSelectedCategories = dialogView.findViewById(R.id.tvDialogSelectedCategories);
        btnDialogCategories.setOnClickListener(v -> showCategoryDialog());

        // Fee spinner
        Spinner dialogFeeSpinner = dialogView.findViewById(R.id.dialogFeeSpinner);
        dialogFeeSpinner.setAdapter(feeAdapter);

        // Set spinner selection to current filter value
        int feeIndex = 0;
        for (int i = 0; i < feeAdapter.getCount(); i++) {
            if (feeAdapter.getItem(i).equals(selectedFeeOption)) {
                feeIndex = i;
                break;
            }
        }
        dialogFeeSpinner.setSelection(feeIndex);

        // Date picker
        Button btnDialogDate = dialogView.findViewById(R.id.btnDialogDate);
        TextView tvDialogSelectedDate = dialogView.findViewById(R.id.tvDialogSelectedDate);
        btnDialogDate.setOnClickListener(v -> showDatePicker(tvDialogSelectedDate));

        // Start time picker
        Button btnDialogStartTime = dialogView.findViewById(R.id.btnDialogStartTime);
        TextView tvDialogStartTime = dialogView.findViewById(R.id.tvDialogStartTime);
        btnDialogStartTime.setOnClickListener(v -> showTimePicker(tvDialogStartTime));

        // Set current filter values in dialog
        tvDialogSelectedCategories.setText(getSelectedCategoriesText());
        tvDialogSelectedDate.setText(selectedDate != null ?
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime()) : "Any");
        tvDialogStartTime.setText(startHour != null && startMinute != null ?
            String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute) : "Any");

        new AlertDialog.Builder(this)
            .setTitle("Filters")
            .setView(dialogView)
            .setPositiveButton("Apply", (dialog, which) -> {
                selectedFeeOption = (String) dialogFeeSpinner.getSelectedItem();
                searchEvents();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void loadRecentQueries() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("recentSearches");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                LinkedHashSet<String> uniqueQueries = new LinkedHashSet<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String query = child.getValue(String.class);
                    if (!TextUtils.isEmpty(query)) {
                        uniqueQueries.add(query.trim());
                    }
                    if (uniqueQueries.size() == 5) break;
                }
                displayRecentQueries(new ArrayList<>(uniqueQueries));
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void displayRecentQueries(List<String> queries) {
        recentQueriesContainer.removeAllViews();
        if (queries.isEmpty()) {
            recentQueriesContainer.setVisibility(View.GONE);
            return;
        }
        recentQueriesContainer.setVisibility(View.VISIBLE);
        for (String query : queries) {
            TextView tv = new TextView(this);
            tv.setText(query);
            tv.setPadding(8, 8, 8, 8);
            tv.setOnClickListener(v -> etSearchBar.setText(query));
            recentQueriesContainer.addView(tv);
        }
    }

    private void saveRecentQuery(String query) {
        if (TextUtils.isEmpty(query)) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("recentSearches");
        ref.push().setValue(query);
    }

    // Search events
    private void searchEvents() {
        hasSearched = true;
        loadingGif.setVisibility(View.VISIBLE);
        resultsContainer.setVisibility(View.GONE);
        saveRecentQuery(etSearchBar.getText().toString().trim());
        eventViewModel.fetchEvents();
    }

    // Helper methods
    private void registerForEvent(Event event) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String registrationId = databaseReference.child("registrations").push().getKey();
        String participantId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (registrationStatusMap.containsKey(event.getEventId())) {
            Toast.makeText(this, "You have already registered for this event.", Toast.LENGTH_SHORT).show();
            return;
        }

        Registration registration = new Registration(
                registrationId,
                event.getEventId(),
                participantId,
                "pending",
                System.currentTimeMillis()
        );
        registration.setOrganizerId(event.getOrganizerId());

        if (registrationId != null) {
            databaseReference.child("registrations").child(registrationId).setValue(registration)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ParticipantEventSearchActivity.this, "Registration request sent.", Toast.LENGTH_SHORT).show();
                        registrationStatusMap.put(event.getEventId(), "pending");
                        setupEventObserver();
                    })
                    .addOnFailureListener(e -> Toast.makeText(ParticipantEventSearchActivity.this, "Failed to send registration request.", Toast.LENGTH_SHORT).show());
        }
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(strAddress, 1);
            if (addresses == null || addresses.isEmpty()) {
                return null;
            }
            Address location = addresses.get(0);
            return new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getCategoryName(Event event) {
        String categoryId = event.getCategoryId();
        for (Category category : allCategories) {
            if (category.getCategoryId().equals(categoryId)) {
                return category.getName();
            }
        }
        return "Unknown";
    }

    private String getSelectedCategoriesText() {
        if (selectedCategoryIds.isEmpty() || allCategories.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (String id : selectedCategoryIds) {
            for (Category cat : allCategories) {
                if (cat.getCategoryId().equals(id)) {
                    sb.append(cat.getName()).append(", ");
                    break;
                }
            }
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 2);
        return sb.toString();
}
}