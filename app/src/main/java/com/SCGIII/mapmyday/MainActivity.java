package com.SCGIII.mapmyday;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private RecyclerView eventRecyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList = new ArrayList<>();


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
        eventRecyclerView = findViewById(R.id.eventRecyclerView);

        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventRecyclerView.setAdapter(eventAdapter);

        SharedPreferences sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        isDarkModeEnabled = sharedPreferences.getBoolean("isDarkModeEnabled", false);

        FloatingActionButton addEventButton = findViewById(R.id.addEventButton);
        addEventButton.setOnClickListener(v -> showEventAdderDialog(null));

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
            String selectedDate = getSelectedDate(position);
            if (!selectedDate.isEmpty()) {
                showEventsForSelectedDate(selectedDate);
            } else {
                Toast.makeText(this, "No valid day selected", Toast.LENGTH_SHORT).show();
            }
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

    private String getCurrentMonth()
    {
        int month = calendar.get(Calendar.MONTH);
        String monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        return monthName;
    }

    private String getSelectedDay(int position) {
        List<String> days = getDaysInMonth(calendar);
        return position < days.size() ? days.get(position) : "";
    }

    private String getSelectedDate(int position) {
        List<String> days = getDaysInMonth(calendar);
        String dayString = position < days.size() ? days.get(position) : "";

        if (dayString.isEmpty()) {
            return ""; // No valid day selected
        }

        int day = Integer.parseInt(dayString);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Months are 0-indexed, so add 1

        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
    }

    private void updateCalendar() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        monthYearText.setText(String.format(Locale.getDefault(), "%tB %tY", calendar, calendar));
        List<String> days = getDaysInMonth(calendar);
        CalendarAdapter adapter = new CalendarAdapter(this, days, eventsMap, year, month);
        calendarGrid.setAdapter(adapter);
    }

    private void showEventAdderDialog(String selectedDay) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);

        TextView eventDate = dialogView.findViewById(R.id.eventDate);
        EditText eventTitle = dialogView.findViewById(R.id.eventTitle);
        EditText eventLocation = dialogView.findViewById(R.id.eventLocation);
        EditText eventStartTime = dialogView.findViewById(R.id.eventStartTime);
        EditText eventEndTime = dialogView.findViewById(R.id.eventEndTime);
        EditText eventFromLocation = dialogView.findViewById(R.id.eventFromLocation);
        Button addEventButton = dialogView.findViewById(R.id.addEventButton);
        AlertDialog dialog = builder.create();

        Calendar calendar = Calendar.getInstance();
        if (selectedDay != null) {
            String[] parts = selectedDay.split("-");
            calendar.set(Calendar.YEAR, Integer.parseInt(parts[0]));
            // this is bc of the month indexing
            calendar.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
        }
        //displays the initial date
        updateEventDateText(eventDate, calendar);

        eventDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                        //updates the calendar to the selected date
                        calendar.set(Calendar.YEAR, selectedYear);
                        calendar.set(Calendar.MONTH, selectedMonth);
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth);
                        updateEventDateText(eventDate, calendar); //this display the selected date
                    }, year, month, day);
            datePickerDialog.show();
        });

        addEventButton.setOnClickListener(v -> {
            String title = eventTitle.getText().toString().trim();
            String location = eventLocation.getText().toString().trim();
            String startTime = eventStartTime.getText().toString().trim();
            String endTime = eventEndTime.getText().toString().trim();
            String fromLocation = eventFromLocation.getText().toString().trim();

            if (title.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentMonth = getCurrentMonth();
            String dateKey = String.format(Locale.getDefault(), "%1$tY-%1$tm-%1$td", calendar);
            String day = String.format(Locale.getDefault(), "%1$td", calendar);
            String eventId = eventsRef.child(day).push().getKey();

            Event newEvent = new Event(title, dateKey, startTime, endTime, location, fromLocation);
            addEventToDay(dateKey, newEvent);

            if (eventId != null) {
                eventsRef.child(dateKey).child(title).setValue(newEvent)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(error -> Toast.makeText(this, "Failed to add event: " + error.getMessage(), Toast.LENGTH_SHORT).show());
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void addEventToDay(String date, Event event) {
        if (!eventsMap.containsKey(date)) {
            eventsMap.put(date, new ArrayList<>());
        }
        eventsMap.get(date).add(event);
    }

    private void updateEventDateText(TextView eventDate, Calendar calendar) {
        String formattedDate = String.format(Locale.getDefault(), "%1$tY-%1$tm-%1$td", calendar);
        eventDate.setText(formattedDate);
    }


    // This method retrieves and displays events for the selected day
    private void showEventsForSelectedDate(String date) {
        eventAdapter = new EventAdapter(eventList, date);
        eventRecyclerView.setAdapter(eventAdapter);

        eventsRef.child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventList.clear();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    if (event != null) {
                        eventList.add(event);
                    }
                }

                if (!eventList.isEmpty()) {
                    eventAdapter.notifyDataSetChanged(); // Update the RecyclerView
                    eventRecyclerView.setVisibility(View.VISIBLE); // Show the RecyclerView
                } else {
                    Toast.makeText(MainActivity.this, "No events for this date", Toast.LENGTH_SHORT).show();
                    eventRecyclerView.setVisibility(View.GONE); // Hide if no events are available
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error loading events: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                eventRecyclerView.setVisibility(View.GONE);
            }
        });
    }


    private void showEventDialog(List<Event> events) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        StringBuilder eventInfo = new StringBuilder();

        for (Event event : events) {
            eventInfo.append("Title: ").append(event.getTitle()).append("\n");
            eventInfo.append("Date: ").append(event.getDate()).append("\n");
            eventInfo.append("Start Time: ").append(event.getStartTime()).append("\n");
            eventInfo.append("End Time: ").append(event.getEndTime()).append("\n");
            eventInfo.append("Location: ").append(event.getLocation()).append("\n");
            eventInfo.append("From: ").append(event.getFromLocation()).append("\n\n");
        }

        builder.setTitle("Events for the Day")
                .setMessage(eventInfo.toString())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
