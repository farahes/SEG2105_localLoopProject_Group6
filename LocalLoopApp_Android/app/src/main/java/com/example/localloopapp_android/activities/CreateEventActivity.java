package com.example.localloopapp_android.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.localloopapp_android.R;
import com.example.localloopapp_android.models.Category;
import com.example.localloopapp_android.viewmodels.CategoryViewModel;
import com.example.localloopapp_android.viewmodels.OrganizerViewModel;
import com.example.localloopapp_android.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.*;

public class CreateEventActivity extends AppCompatActivity {

    private EditText etName, etDesc, etFee, etStartDate, etEndDate;
    private Spinner spinnerCategory;
    private Button btnCreateEvent;

    private String organizerId;
    private final Calendar eventStartCalendar = Calendar.getInstance();
    private final Calendar eventEndCalendar = Calendar.getInstance();

    private List<Category> categoryList = new ArrayList<>();
    private String selectedCategoryId = null;

    private OrganizerViewModel organizerViewModel;
    private CategoryViewModel categoryViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        organizerId = getIntent().getStringExtra(Constants.EXTRA_USER_ID);

        etName = findViewById(R.id.etEventName);
        etDesc = findViewById(R.id.etEventDesc);
        etFee = findViewById(R.id.etEventFee);
        etStartDate = findViewById(R.id.etEventStartDate);
        etEndDate = findViewById(R.id.etEventEndDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);

        organizerViewModel = new ViewModelProvider(this).get(OrganizerViewModel.class);
        organizerViewModel.setOrganizerId(organizerId);

        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        setupCategorySpinner();

        setupDatePickers();

        btnCreateEvent.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String feeStr = etFee.getText().toString().trim();

            if (name.isEmpty() || desc.isEmpty() || feeStr.isEmpty() || selectedCategoryId == null) {
                Toast.makeText(this, "Please fill all fields and select a category", Toast.LENGTH_SHORT).show();
                return;
            }

            double fee;
            try {
                fee = Double.parseDouble(feeStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid fee amount", Toast.LENGTH_SHORT).show();
                return;
            }

            long start = eventStartCalendar.getTimeInMillis();
            long end = eventEndCalendar.getTimeInMillis();

            if (end < start) {
                Toast.makeText(this, "End date cannot be before start date", Toast.LENGTH_SHORT).show();
                return;
            }

            organizerViewModel.createEvent(name, desc, selectedCategoryId, fee, start, end);
            Toast.makeText(this, "Event created successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        categoryViewModel.getCategories().observe(this, categories -> {
            categoryList = categories;

            List<String> names = new ArrayList<>();
            for (Category c : categories) {
                names.add(c.getName());
            }

            adapter.clear();
            adapter.addAll(names);
            adapter.notifyDataSetChanged();
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategoryId = categoryList.get(position).getCategoryId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = null;
            }
        });
    }

    private void setupDatePickers() {
        etStartDate.setOnClickListener(v -> {
            int y = eventStartCalendar.get(Calendar.YEAR);
            int m = eventStartCalendar.get(Calendar.MONTH);
            int d = eventStartCalendar.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(this, (view, year, month, day) -> {
                eventStartCalendar.set(year, month, day);
                updateDateField(etStartDate, eventStartCalendar);
            }, y, m, d).show();
        });

        etEndDate.setOnClickListener(v -> {
            int y = eventEndCalendar.get(Calendar.YEAR);
            int m = eventEndCalendar.get(Calendar.MONTH);
            int d = eventEndCalendar.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(this, (view, year, month, day) -> {
                eventEndCalendar.set(year, month, day);
                updateDateField(etEndDate, eventEndCalendar);
            }, y, m, d).show();
        });
    }

    private void updateDateField(EditText editText, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        editText.setText(sdf.format(calendar.getTime()));
    }
}
