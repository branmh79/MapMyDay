package com.SCGIII.mapmyday;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Calendar calendar;
    private TextView monthYearText;
    private GridView calendarGrid;
    private Map<String, List<Event>> eventsMap = new HashMap<>();
    private ImageButton themeToggleButton;
    private boolean isDarkModeEnabled;
    private DatabaseReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase reference
        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        // Initialize UI components
        calendar = Calendar.getInstance();
        monthYearText = findViewById(R.id.monthYearText);
        calendarGrid = findViewById(R.id.calendarGrid);
        Button prevButton = findViewById(R.id.prevButton);
        Button nextButton = findViewById(R.id.nextButton);
        themeToggleButton = findViewById(R.id.themeToggleButton);

        SharedPreferences sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        isDarkModeEnabled = sharedPreferences.getBoolean("isDarkModeEnabled", false);

        // Set the theme based on saved preference
        setThemeMode();

        // Load events from Firebase
        loadEvents();

        // Set button listeners for navigation and theme toggle
        prevButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        nextButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        calendarGrid.setOnItemClickListener((parent, view, position, ID) -> {
            String selectedDay = getSelectedDay(position);
            showEventAdderDialog(selectedDay);
        });

        themeToggleButton.setOnClickListener(v -> toggleTheme());
    }

    private void setThemeMode() {
        AppCompatDelegate.setDefaultNightMode(
                isDarkModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        themeToggleButton.setImageResource(isDarkModeEnabled ? R.drawable.sun : R.drawable.moon);
    }

    private void toggleTheme() {
        isDarkModeEnabled = !isDarkModeEnabled;
        setThemeMode();

        SharedPreferences.Editor editor = getSharedPreferences("ThemePrefs", MODE_PRIVATE).edit();
        editor.putBoolean("isDarkModeEnabled", isDarkModeEnabled);
        editor.apply();
    }

    // Load events from Firebase
    private void loadEvents() {
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventsMap.clear();
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey() != null ? dateSnapshot.getKey() : ""; // Default to empty string
                    List<Event> eventsList = new ArrayList<>();
                    for (DataSnapshot eventSnapshot : dateSnapshot.getChildren()) {
                        Event event = eventSnapshot.getValue(Event.class);
                        if (event != null) {
                            eventsList.add(sanitizeEvent(event)); // Ensure non-null fields
                        }
                    }
                    eventsMap.put(dateKey, eventsList);
                }
                updateCalendar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Error loading events: " + error.getMessage());
                Toast.makeText(MainActivity.this, "Failed to load events. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to ensure Event objects have non-null values
    private Event sanitizeEvent(Event event) {
        if (event.getTitle() == null) event.setTitle(""); // Default title
        if (event.getDate() == null) event.setDate(""); // Default date
        if (event.getStartTime() == null) event.setStartTime(""); // Default start time
        if (event.getEndTime() == null) event.setEndTime(""); // Default end time
        if (event.getLocation() == null) event.setLocation(""); // Default location
        if (event.getFromLocation() == null) event.setFromLocation(""); // Default from location
        return event;
    }

    private List<String> getDaysInMonth(Calendar calendar) {
        List<String> days = new ArrayList<>();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        for (int i = 1; i < firstDayOfWeek; i++) {
            days.add(""); // Add empty string for empty spaces
        }

        for (int i = 1; i <= daysInMonth; i++) {
            days.add(String.valueOf(i));
        }

        return days;
    }

    private String getSelectedDay(int position) {
        List<String> days = getDaysInMonth(calendar);
        return position < days.size() ? days.get(position) : "";
    }

    private void updateCalendar() {
        monthYearText.setText(String.format(Locale.getDefault(), "%tB %tY", calendar, calendar));
        List<String> days = getDaysInMonth(calendar);
        CalendarAdapter adapter = new CalendarAdapter(this, days, eventsMap);
        calendarGrid.setAdapter(adapter);
    }

    private void showEventAdderDialog(String selectedDay) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);

        EditText eventTitle = dialogView.findViewById(R.id.eventTitle);
        EditText eventLocation = dialogView.findViewById(R.id.eventLocation);
        EditText eventStartTime = dialogView.findViewById(R.id.eventStartTime);
        EditText eventEndTime = dialogView.findViewById(R.id.eventEndTime);
        Button addEventButton = dialogView.findViewById(R.id.addEventButton);
        AlertDialog dialog = builder.create();

        addEventButton.setOnClickListener(v -> {
            String title = eventTitle.getText().toString().trim();
            String location = eventLocation.getText().toString().trim();
            String startTime = eventStartTime.getText().toString().trim();
            String endTime = eventEndTime.getText().toString().trim();

            if (title.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String dateKey = selectedDay != null ? selectedDay : ""; // Ensure non-null dateKey
            String eventId = eventsRef.child(dateKey).push().getKey();

            Event newEvent = new Event(eventId != null ? eventId : "", title, dateKey, startTime, endTime, location);
            if (eventId != null) {
                eventsRef.child(dateKey).child(eventId).setValue(newEvent)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(error -> Toast.makeText(this, "Failed to add event: " + error.getMessage(), Toast.LENGTH_SHORT).show());
            }

            dialog.dismiss();
        });

        dialog.show();
    }
}
