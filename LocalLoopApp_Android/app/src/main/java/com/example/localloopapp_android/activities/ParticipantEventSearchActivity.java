package com.example.localloopapp_android.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.localloopapp_android.R;
import com.example.localloopapp_android.models.Category;
import com.example.localloopapp_android.models.Event;
import com.example.localloopapp_android.models.Registration;
import com.example.localloopapp_android.viewmodels.CategoryViewModel;
import com.example.localloopapp_android.viewmodels.EventViewModel;
import com.example.localloopapp_android.viewmodels.RegistrationViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ParticipantEventSearchActivity extends AppCompatActivity {

    private EditText etSearchBar;
    private LinearLayout recentQueriesContainer;
    private Button btnSearch, btnFilters;
    private ImageView loadingGif;
    private LinearLayout resultsContainer;
    private ArrayAdapter<String> feeAdapter;

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

    private static final int EVENTS_PER_PAGE = 5;
    private int currentPage = 0;
    private List<Event> lastFilteredEvents = new ArrayList<>();

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
            if (hasSearched) return; // skip default once searched
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
                View card = getLayoutInflater()
                        .inflate(R.layout.item_participant_event_card, resultsContainer, false);
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

        Glide.with(this)
                .asGif()
                .load(R.drawable.ic_loading_packman)
                .into(loadingGif);

        feeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Any", "Free", "< $50", "> $50"});
        feeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categoryViewModel     = new ViewModelProvider(this).get(CategoryViewModel.class);
        eventViewModel        = new ViewModelProvider(this).get(EventViewModel.class);
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
                // before any search, we rely on the default upcoming observer
                return;
            }
            loadingGif.setVisibility(View.GONE);
            resultsContainer.setVisibility(View.VISIBLE);
            resultsContainer.removeAllViews();

            // Move filtering to background thread to avoid ANR
            new AsyncTask<List<Event>, Void, List<Event>>() {
                @Override
                protected List<Event> doInBackground(List<Event>... params) {
                    List<Event> filtered = new ArrayList<>();
                    String searchQuery = etSearchBar.getText().toString().trim().toLowerCase();

                    for (Event event : params[0]) {
                        // 1) Query match
                        boolean matchesQuery = TextUtils.isEmpty(searchQuery)
                                || (event.getName() != null && event.getName().toLowerCase().contains(searchQuery))
                                || (event.getDescription() != null && event.getDescription().toLowerCase().contains(searchQuery));
                        if (!matchesQuery) continue;

                        // 2) Category filter
                        if (!selectedCategoryIds.isEmpty()
                                && (event.getCategoryId() == null || !selectedCategoryIds.contains(event.getCategoryId()))) {
                            continue;
                        }

                        // 3) Fee filter
                        double fee = event.getFee();
                        if ("Free".equals(selectedFeeOption) && fee != 0) continue;
                        if ("< $50".equals(selectedFeeOption) && !(fee > 0 && fee < 50)) continue;
                        if ("> $50".equals(selectedFeeOption) && !(fee > 50)) continue;

                        // 4) Date & time filter
                        if (selectedDate != null) {
                            Calendar eventCal = Calendar.getInstance();
                            eventCal.setTime(new Date(event.getEventStart()));
                            if (eventCal.get(Calendar.YEAR) != selectedDate.get(Calendar.YEAR)
                                    || eventCal.get(Calendar.MONTH) != selectedDate.get(Calendar.MONTH)
                                    || eventCal.get(Calendar.DAY_OF_MONTH) != selectedDate.get(Calendar.DAY_OF_MONTH)) {
                                continue;
                            }
                            if (startHour != null && startMinute != null) {
                                int eventMinutes = eventCal.get(Calendar.HOUR_OF_DAY) * 60 + eventCal.get(Calendar.MINUTE);
                                int startMinutes = startHour * 60 + startMinute;
                                if (eventMinutes < startMinutes) continue;
                            }
                        }

                        filtered.add(event);
                    }
                    return filtered;
                }

                @Override
                protected void onPostExecute(List<Event> filtered) {
                    lastFilteredEvents = filtered;
                    currentPage = 0;
                    showCurrentPage();
                }
            }.execute(events);
        });
    }

    private void showCurrentPage() {
        resultsContainer.removeAllViews();
        int start = currentPage * EVENTS_PER_PAGE;
        int end = Math.min(start + EVENTS_PER_PAGE, lastFilteredEvents.size());
        for (int i = start; i < end; i++) {
            Event event = lastFilteredEvents.get(i);
            View card = getLayoutInflater().inflate(R.layout.item_participant_event_card, resultsContainer, false);
            populateEventCard(card, event);
            resultsContainer.addView(card);
        }
        // Pagination controls
        if (lastFilteredEvents.size() > EVENTS_PER_PAGE) {
            LinearLayout paginationLayout = new LinearLayout(this);
            paginationLayout.setOrientation(LinearLayout.HORIZONTAL);
            paginationLayout.setGravity(android.view.Gravity.CENTER);
            Button prevBtn = new Button(this);
            prevBtn.setText("Previous");
            prevBtn.setEnabled(currentPage > 0);
            prevBtn.setOnClickListener(v -> {
                if (currentPage > 0) {
                    currentPage--;
                    showCurrentPage();
                }
            });
            Button nextBtn = new Button(this);
            nextBtn.setText("Next");
            nextBtn.setEnabled((currentPage + 1) * EVENTS_PER_PAGE < lastFilteredEvents.size());
            nextBtn.setOnClickListener(v -> {
                if ((currentPage + 1) * EVENTS_PER_PAGE < lastFilteredEvents.size()) {
                    currentPage++;
                    showCurrentPage();
                }
            });
            paginationLayout.addView(prevBtn);
            paginationLayout.addView(nextBtn);
            resultsContainer.addView(paginationLayout);
        }
        if (resultsContainer.getChildCount() == 0) {
            TextView tv = new TextView(this);
            tv.setText("No events found.");
            resultsContainer.addView(tv);
        }
    }

    private void populateEventCard(View card, Event event) {
        // Image
        ImageView ivEventImage = card.findViewById(R.id.ivEventAvatar);
        ManageEventActivity.loadEventImage(event.getEventId(), ivEventImage);

        // Text fields
        ((TextView) card.findViewById(R.id.tvEventName)).setText(event.getName());
        ((TextView) card.findViewById(R.id.tvEventDescription)).setText(event.getDescription());
        setCategoryName((TextView) card.findViewById(R.id.tvEventCategory), event.getCategoryId());

        // Fee styling
        TextView tvFee = card.findViewById(R.id.tvEventFee);
        if (event.getFee() == 0.0) {
            tvFee.setText("Free");
            tvFee.setTextColor(getResources().getColor(R.color.green, null));
            tvFee.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            tvFee.setText(String.format(Locale.getDefault(), "$%.2f", event.getFee()));
            tvFee.setTextColor(getResources().getColor(android.R.color.black, null));
            tvFee.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        // Date & time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String dateStr = dateFormat.format(new Date(event.getEventStart()));
        String timeStr = timeFormat.format(new Date(event.getEventStart()))
                + " - " + timeFormat.format(new Date(event.getEventEnd()));
        ((TextView) card.findViewById(R.id.tvEventDate)).setText(dateStr);
        ((TextView) card.findViewById(R.id.tvEventTime)).setText(timeStr);

        // Replace MapView with static map icon
        ImageView staticMapIcon = card.findViewById(R.id.staticMapIcon);
        if (staticMapIcon != null) {
            staticMapIcon.setVisibility(View.VISIBLE);
            staticMapIcon.setImageResource(R.drawable.ic_map_placeholder); // Use your static map icon
        }
        MapView mapView = card.findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.setVisibility(View.GONE);
        }

        // Registration button
        Button btnRegister = card.findViewById(R.id.btnRegisterEvent);
        String status = registrationStatusMap.get(event.getEventId());
        if (status != null) {
            switch (status) {
                case "pending":
                    btnRegister.setText(event.getFee() == 0.0
                            ? "Pending (Free)"
                            : String.format(Locale.getDefault(), "Pending ($%.2f)", event.getFee()));
                    btnRegister.setEnabled(false);
                    btnRegister.setBackgroundColor(getResources().getColor(R.color.yellow, null));
                    break;
                case "accepted":
                    btnRegister.setText("Accepted");
                    btnRegister.setEnabled(false);
                    btnRegister.setBackgroundColor(getResources().getColor(R.color.green, null));
                    break;
                case "rejected":
                    btnRegister.setText("Rejected");
                    btnRegister.setEnabled(false);
                    btnRegister.setBackgroundColor(getResources().getColor(R.color.red, null));
                    break;
                default:
                    btnRegister.setText("Unknown Status");
                    btnRegister.setEnabled(false);
                    btnRegister.setBackgroundColor(getResources().getColor(android.R.color.darker_gray, null));
                    break;
            }
        } else {
            btnRegister.setText(event.getFee() == 0.0
                    ? "Register (Free)"
                    : String.format(Locale.getDefault(), "Register ($%.2f)", event.getFee()));
            btnRegister.setEnabled(true);
            btnRegister.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
            btnRegister.setOnClickListener(v -> registerForEvent(event));
        }

        // Overlay → your popup feature
        Button btnOverlay = card.findViewById(R.id.btnCardOverlay);
        btnOverlay.setOnClickListener(v -> showEventCardPopup(event));
    }

    private void showCategoryDialog() {
        String[] categoryNames = new String[allCategories.size()];
        for (int i = 0; i < allCategories.size(); i++) {
            categoryNames[i] = allCategories.get(i).getName();
        }
        new AlertDialog.Builder(this)
                .setTitle("Select Categories")
                .setMultiChoiceItems(categoryNames, selectedCategories, (dlg, which, isChecked) -> {
                    selectedCategories[which] = isChecked;
                })
                .setPositiveButton("OK", (dlg, which) -> {
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
        DatePickerDialog dlg = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, day);
                    if (targetView != null) {
                        targetView.setText(
                                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        .format(selectedDate.getTime()));
                    }
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        dlg.show();
    }

    private void showTimePicker(@Nullable TextView targetView) {
        final Calendar now = Calendar.getInstance();
        TimePickerDialog dlg = new TimePickerDialog(this,
                (view, hour, minute) -> {
                    startHour = hour;
                    startMinute = minute;
                    if (targetView != null) {
                        targetView.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
                    }
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true);
        dlg.show();
    }

    private void showFiltersDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_event_filters, null);

        // Category
        Button btnDialogCategories = dialogView.findViewById(R.id.btnDialogCategories);
        TextView tvDialogSelectedCategories = dialogView.findViewById(R.id.tvDialogSelectedCategories);
        btnDialogCategories.setOnClickListener(v -> showCategoryDialog());

        // Fee
        Spinner dialogFeeSpinner = dialogView.findViewById(R.id.dialogFeeSpinner);
        dialogFeeSpinner.setAdapter(feeAdapter);
        int feeIndex = 0;
        for (int i = 0; i < feeAdapter.getCount(); i++) {
            if (feeAdapter.getItem(i).equals(selectedFeeOption)) {
                feeIndex = i;
                break;
            }
        }
        dialogFeeSpinner.setSelection(feeIndex);

        // Date
        Button btnDialogDate = dialogView.findViewById(R.id.btnDialogDate);
        TextView tvDialogSelectedDate = dialogView.findViewById(R.id.tvDialogSelectedDate);
        btnDialogDate.setOnClickListener(v -> showDatePicker(tvDialogSelectedDate));

        // Start time
        Button btnDialogStartTime = dialogView.findViewById(R.id.btnDialogStartTime);
        TextView tvDialogStartTime = dialogView.findViewById(R.id.tvDialogStartTime);
        btnDialogStartTime.setOnClickListener(v -> showTimePicker(tvDialogStartTime));

        // Current selections
        tvDialogSelectedCategories.setText(getSelectedCategoriesText());
        tvDialogSelectedDate.setText(selectedDate != null
                ? new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime())
                : "Any");
        tvDialogStartTime.setText(startHour != null && startMinute != null
                ? String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute)
                : "Any");

        new AlertDialog.Builder(this)
                .setTitle("Filters")
                .setView(dialogView)
                .setPositiveButton("Apply", (dlg, which) -> {
                    selectedFeeOption = (String) dialogFeeSpinner.getSelectedItem();
                    searchEvents();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadRecentQueries() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child(uid).child("recentSearches");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                LinkedHashSet<String> unique = new LinkedHashSet<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String q = child.getValue(String.class);
                    if (!TextUtils.isEmpty(q)) {
                        unique.add(q.trim());
                    }
                    if (unique.size() == 5) break;
                }
                displayRecentQueries(new ArrayList<>(unique));
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
        for (String q : queries) {
            TextView tv = new TextView(this);
            tv.setText(q);
            tv.setPadding(8, 8, 8, 8);
            tv.setOnClickListener(v -> etSearchBar.setText(q));
            recentQueriesContainer.addView(tv);
        }
    }

    private void saveRecentQuery(String query) {
        if (TextUtils.isEmpty(query)) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child(uid).child("recentSearches");
        ref.push().setValue(query);
    }

    private void searchEvents() {
        hasSearched = true;
        loadingGif.setVisibility(View.VISIBLE);
        resultsContainer.setVisibility(View.GONE);
        saveRecentQuery(etSearchBar.getText().toString().trim());
        eventViewModel.fetchEvents();
    }

    private void registerForEvent(Event event) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        String registrationId = dbRef.child("registrations").push().getKey();
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
            dbRef.child("registrations").child(registrationId).setValue(registration)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Registration request sent.", Toast.LENGTH_SHORT).show();
                        // Immediately update the map and refresh UI
                        registrationStatusMap.put(event.getEventId(), "pending");
                        // Force refresh of participant registrations and event list
                        String participantIdNow = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        registrationViewModel.loadParticipantRegistrations(participantIdNow);
                        eventViewModel.fetchEvents();
                        // Also refresh event cards immediately
                        setupEventObserver();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to send registration request.", Toast.LENGTH_SHORT).show());
        }
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(strAddress, 1);
            if (addresses == null || addresses.isEmpty()) return null;
            Address loc = addresses.get(0);
            return new LatLng(loc.getLatitude(), loc.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setCategoryName(TextView categoryView, String categoryId) {
        FirebaseDatabase.getInstance()
                .getReference("categories").child(categoryId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        categoryView.setText((name != null && !name.isEmpty()) ? name : "Unknown");
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        categoryView.setText("Unknown");
                    }
                });
    }

    private void showEventCardPopup(Event event) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View popup = getLayoutInflater().inflate(R.layout.item_participant_event_card, null);

        ImageView ivEventImage = popup.findViewById(R.id.ivEventAvatar);
        ManageEventActivity.loadEventImage(event.getEventId(), ivEventImage);
        ((TextView) popup.findViewById(R.id.tvEventName)).setText(event.getName());
        ((TextView) popup.findViewById(R.id.tvEventDescription)).setText(event.getDescription());
        setCategoryName((TextView) popup.findViewById(R.id.tvEventCategory), event.getCategoryId());

        TextView tvFee = popup.findViewById(R.id.tvEventFee);
        if (event.getFee() == 0.0) {
            tvFee.setText("Free");
            tvFee.setTextColor(getResources().getColor(R.color.green, null));
            tvFee.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            tvFee.setText(String.format(Locale.getDefault(), "$%.2f", event.getFee()));
            tvFee.setTextColor(getResources().getColor(android.R.color.black, null));
            tvFee.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String dateStr = dateFormat.format(new Date(event.getEventStart()));
        String startTime = timeFormat.format(new Date(event.getEventStart()));
        String endTime = timeFormat.format(new Date(event.getEventEnd()));
        ((TextView) popup.findViewById(R.id.tvEventDate)).setText(dateStr);
        ((TextView) popup.findViewById(R.id.tvEventTime)).setText(
                startTime.equals(endTime) ? startTime : (startTime + " - " + endTime)
        );

        MapView mapView = popup.findViewById(R.id.mapView);
        mapView.onCreate(null);
        mapView.getMapAsync(googleMap -> {
            LatLng loc = getLocationFromAddress(popup.getContext(), event.getLocation());
            if (loc != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
                googleMap.addMarker(new MarkerOptions().position(loc).title(event.getLocation()));
            }
        });

        TextView hostedByView = popup.findViewById(R.id.tvHostedBy);
        fetchOrganizerInfo(event.getOrganizerId(), hostedByView);

        Button btnRegister = popup.findViewById(R.id.btnRegisterEvent);
        String status = registrationStatusMap.get(event.getEventId());
        if (status != null) {
            switch (status) {
                case "pending":
                    btnRegister.setText("Pending");
                    btnRegister.setEnabled(false);
                    btnRegister.setBackgroundColor(getResources().getColor(R.color.yellow, null));
                    break;
                case "accepted":
                    btnRegister.setText("Accepted");
                    btnRegister.setEnabled(false);
                    btnRegister.setBackgroundColor(getResources().getColor(R.color.green, null));
                    break;
                case "rejected":
                    btnRegister.setText("Rejected");
                    btnRegister.setEnabled(false);
                    btnRegister.setBackgroundColor(getResources().getColor(R.color.red, null));
                    break;
                default:
                    btnRegister.setText("Unknown Status");
                    btnRegister.setEnabled(false);
                    btnRegister.setBackgroundColor(getResources().getColor(android.R.color.darker_gray, null));
                    break;
            }
        } else {
            btnRegister.setText(event.getFee() == 0.0
                    ? "Register (Free)"
                    : String.format(Locale.getDefault(), "Register ($%.2f)", event.getFee()));
            btnRegister.setEnabled(true);
            btnRegister.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
            btnRegister.setOnClickListener(v -> {
                String evtId = event.getEventId();
                if (registrationStatusMap.containsKey(evtId)) {
                    Toast.makeText(
                            ParticipantEventSearchActivity.this,
                            "You have already registered for this event.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
                registerForEvent(event);
                btnRegister.setText("Pending");
                btnRegister.setEnabled(false);
                btnRegister.setBackgroundColor(getResources().getColor(R.color.yellow, null));
                dialog.dismiss();
            });
        }

        Button btnOverlay = popup.findViewById(R.id.btnCardOverlay);
        btnOverlay.setVisibility(View.GONE);

        dialog.setContentView(popup);
        dialog.show();
    }

    private void fetchOrganizerInfo(String organizerId, TextView hostedByView) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child(organizerId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                String first = snap.child("firstName").getValue(String.class);
                String last  = snap.child("lastName").getValue(String.class);
                String comp  = snap.child("companyName").getValue(String.class);
                String name  = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                if (name.isEmpty()) name = "Unknown";
                if (comp == null || comp.isEmpty()) comp = "Unknown";
                hostedByView.setText("Hosted By: " + name + " on behalf of " + comp);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                hostedByView.setText("Hosted By: Unknown");
            }
        });
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

    private String getCategoryName(Event event) {
        String categoryId = event.getCategoryId();
        for (Category category : allCategories) {
            if (category.getCategoryId().equals(categoryId)) {
                return category.getName();
            }
        }
        return "Unknown";
    }
}

