package com.example.localloopapp_android.activities.participant;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.localloopapp_android.R;
import com.example.localloopapp_android.models.Category;
import com.example.localloopapp_android.models.Event;
import com.example.localloopapp_android.viewmodels.CategoryViewModel;
import com.example.localloopapp_android.viewmodels.EventViewModel;

import java.text.SimpleDateFormat;
import java.util.*;

public class ParticipantEventSearchActivity extends AppCompatActivity {

    private EditText etName, etDescription;
    private TextView tvSelectedCategories, tvSelectedDate, tvStartTime, tvEndTime;
    private Button btnSelectCategories, btnSelectDate, btnStartTime, btnEndTime, btnSearch;
    private Spinner feeSpinner;
    private LinearLayout resultsContainer;

    private List<Category> allCategories = new ArrayList<>();
    private boolean[] selectedCategories;
    private List<String> selectedCategoryIds = new ArrayList<>();
    private Calendar selectedDate = null;
    private Integer startHour = null, startMinute = null, endHour = null, endMinute = null;

    private CategoryViewModel categoryViewModel;
    private EventViewModel eventViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_event_search);

        etName = findViewById(R.id.etEventName);
        etDescription = findViewById(R.id.etEventDescription);
        tvSelectedCategories = findViewById(R.id.tvSelectedCategories);
        btnSelectCategories = findViewById(R.id.btnSelectCategories);
        feeSpinner = findViewById(R.id.spinnerFee);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnStartTime = findViewById(R.id.btnStartTime);
        btnEndTime = findViewById(R.id.btnEndTime);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        btnSearch = findViewById(R.id.btnSearchEvents);
        resultsContainer = findViewById(R.id.resultsContainer);

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

        // Setup fee spinner
        ArrayAdapter<String> feeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Any", "Free", "< $50", "> $50"});
        feeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feeSpinner.setAdapter(feeAdapter);

        // Load categories
        categoryViewModel.getCategories().observe(this, categories -> {
            allCategories = categories;
            selectedCategories = new boolean[categories.size()];
        });
        categoryViewModel.fetchCategories();

        btnSelectCategories.setOnClickListener(v -> showCategoryDialog());
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnStartTime.setOnClickListener(v -> showTimePicker(true));
        btnEndTime.setOnClickListener(v -> showTimePicker(false));
        btnSearch.setOnClickListener(v -> searchEvents());
    }

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
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < selectedCategories.length; i++) {
                        if (selectedCategories[i]) {
                            selectedCategoryIds.add(allCategories.get(i).getCategoryId());
                            sb.append(allCategories.get(i).getName()).append(", ");
                        }
                    }
                    if (sb.length() > 0) sb.setLength(sb.length() - 2);
                    tvSelectedCategories.setText(sb.length() > 0 ? sb.toString() : "None");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDatePicker() {
        final Calendar now = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            tvSelectedDate.setText(sdf.format(selectedDate.getTime()));
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker(boolean isStart) {
        final Calendar now = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            if (isStart) {
                startHour = hourOfDay;
                startMinute = minute;
                tvStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            } else {
                endHour = hourOfDay;
                endMinute = minute;
                tvEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void searchEvents() {
        String nameQuery = etName.getText().toString().trim().toLowerCase();
        String descQuery = etDescription.getText().toString().trim().toLowerCase();
        String feeOption = (String) feeSpinner.getSelectedItem();

        eventViewModel.getEvents().observe(this, events -> {
            resultsContainer.removeAllViews();
            for (Event event : events) {
                // Name similarity
                if (!TextUtils.isEmpty(nameQuery) && (event.getName() == null ||
                        !event.getName().toLowerCase().contains(nameQuery))) continue;
                // Description similarity
                if (!TextUtils.isEmpty(descQuery) && (event.getDescription() == null ||
                        !event.getDescription().toLowerCase().contains(descQuery))) continue;
                // Category filter
                if (!selectedCategoryIds.isEmpty() && (event.getCategoryId() == null ||
                        !selectedCategoryIds.contains(event.getCategoryId()))) continue;
                // Fee filter
                double fee = event.getParticipationFee();
                if ("Free".equals(feeOption) && fee != 0) continue;
                if ("< $50".equals(feeOption) && !(fee > 0 && fee < 50)) continue;
                if ("> $50".equals(feeOption) && !(fee > 50)) continue;
                // Date and time filter
                if (selectedDate != null) {
                    Calendar eventCal = Calendar.getInstance();
                    eventCal.setTime(new Date(event.getEventStart()));
                    if (eventCal.get(Calendar.YEAR) != selectedDate.get(Calendar.YEAR) ||
                        eventCal.get(Calendar.MONTH) != selectedDate.get(Calendar.MONTH) ||
                        eventCal.get(Calendar.DAY_OF_MONTH) != selectedDate.get(Calendar.DAY_OF_MONTH)) continue;
                    // Time range
                    int eventHour = eventCal.get(Calendar.HOUR_OF_DAY);
                    int eventMinute = eventCal.get(Calendar.MINUTE);
                    int eventTime = eventHour * 60 + eventMinute;
                    if (startHour != null && startMinute != null) {
                        int startTime = startHour * 60 + startMinute;
                        if (eventTime < startTime) continue;
                    }
                    if (endHour != null && endMinute != null) {
                        int endTime = endHour * 60 + endMinute;
                        if (eventTime > endTime) continue;
                    }
                }
                // Add event card
                View card = getLayoutInflater().inflate(R.layout.participant_event_card, resultsContainer, false);
                ((TextView) card.findViewById(R.id.tvEventName)).setText(event.getName());
                ((TextView) card.findViewById(R.id.tvEventDescription)).setText(event.getDescription());
                ((TextView) card.findViewById(R.id.tvEventCategory)).setText(event.getCategoryName());
                ((TextView) card.findViewById(R.id.tvEventFee)).setText(event.getParticipationFee() == 0 ? "Free" : "$" + event.getParticipationFee());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String dateTime = sdf.format(new Date(event.getEventStart())) + " - " + sdf.format(new Date(event.getEventEnd()));
(               (TextView) card.findViewById(R.id.tvEventDateTime)).setText(dateTime);
                resultsContainer.addView(card);
            }
            if (resultsContainer.getChildCount() == 0) {
                TextView tv = new TextView(this);
                tv.setText("No events found.");
                resultsContainer.addView(tv);
            }
        });
        eventViewModel.fetchEvents();
    }
}