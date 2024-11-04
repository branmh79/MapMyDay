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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.FirebaseApp; // Imported this line
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Instantiate variables
    private Calendar calendar;
    private TextView monthYearText;
    private GridView calendarGrid;
    private Map<String, List<Event>> eventsMap;
    private ImageButton themeToggleButton;
    private boolean isDarkModeEnabled;

    // Firebase database reference
    private DatabaseReference databaseReference;

@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase explicitly
        FirebaseApp.initializeApp(this);

        // Test Firebase initialization
        if (FirebaseApp.getApps(this).isEmpty()) {
            Toast.makeText(this, "Firebase initialization failed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Firebase initialized successfully", Toast.LENGTH_SHORT).show();
        }

        // Gets the layouts from activity_main.xml
        setContentView(R.layout.activity_main);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("events");


        calendar = Calendar.getInstance();
        monthYearText = findViewById(R.id.monthYearText);
        calendarGrid = findViewById(R.id.calendarGrid);
        Button prevButton = findViewById(R.id.prevButton);
        Button nextButton = findViewById(R.id.nextButton);
        themeToggleButton = findViewById(R.id.themeToggleButton);
        SharedPreferences sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        isDarkModeEnabled = sharedPreferences.getBoolean("isDarkModeEnabled", false);

        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            themeToggleButton.setImageResource(R.drawable.sun);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            themeToggleButton.setImageResource(R.drawable.moon);
        }

        eventsMap = new HashMap<>();
        updateCalendar();

        prevButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        // Calls to update calendar to next month whenever next button is pushed
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

    /* Utilizing the calendar import in Java, implements the correct formatting for days in a month,
       for example, not every month starts on a Sunday. Sometimes you have blank days at the start of the week. */
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

    // This is called when user clicks on a day. Gets the correct day in the list of days using the provided position
    private String getSelectedDay(int position) {
        List<String> days = getDaysInMonth(calendar);
        return days.get(position);
    }

    private void toggleTheme() {
        // Toggle the theme
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            isDarkModeEnabled = false;
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            isDarkModeEnabled = true;
        }

        SharedPreferences.Editor editor = getSharedPreferences("ThemePrefs", MODE_PRIVATE).edit();
        editor.putBoolean("isDarkModeEnabled", isDarkModeEnabled);
        editor.apply();
    }

    // Updates the calendar. This method is called every time you click on a new month or load a new view
    private void updateCalendar() {
        monthYearText.setText(String.format(Locale.getDefault(), "%tB %tY", calendar, calendar));
        List<String> days = getDaysInMonth(calendar);
        CalendarAdapter adapter = new CalendarAdapter(this, days, eventsMap);
        calendarGrid.setAdapter(adapter);
    }

    // Event adder feature
    private void showEventAdderDialog(String selectedDay) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflates from XML dialog_add_event. Sets view
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);

        // Gets the Ids from XML
        EditText eventTitle = dialogView.findViewById(R.id.eventTitle);
        EditText eventLocation = dialogView.findViewById(R.id.eventLocation);
        EditText eventStartTime = dialogView.findViewById(R.id.eventStartTime);
        EditText eventEndTime = dialogView.findViewById(R.id.eventEndTime);
        EditText eventFromLocation = dialogView.findViewById(R.id.eventFromLocation);
        Button addEventButton = dialogView.findViewById(R.id.addEventButton);

        // Creates an alert dialog
        AlertDialog dialog = builder.create();

        // Event adder button
        addEventButton.setOnClickListener(v -> {
            String title = eventTitle.getText().toString();
            String location = eventLocation.getText().toString();
            String startTime = eventStartTime.getText().toString();
            String endTime = eventEndTime.getText().toString();
            String fromLocation = eventFromLocation.getText().toString();

            // Create a new Event object
            String dateKey = String.format(Locale.getDefault(), "%tY-%tm-%s", calendar, calendar, selectedDay);
            Event newEvent = new Event(title, dateKey, startTime, endTime, location, fromLocation);
            addEventToDay(dateKey, newEvent);

            if (fromLocation.isEmpty() || location.isEmpty()) {
                showTravelTimeDialog("Please enter an origin and destination location.");
            } else {
                directionsAPI.getTravelTime(fromLocation, location);  // Call to API
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void addEventToDay(String date, Event event) {
        // Add event to local map
        if (!eventsMap.containsKey(date)) {
            eventsMap.put(date, new ArrayList<>());
        }
        eventsMap.get(date).add(event);

        // Push event to Firebase
        databaseReference.child(date).push().setValue(event)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(MainActivity.this, "Event saved to database", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(MainActivity.this, "Failed to save event", Toast.LENGTH_SHORT).show();
            });
    }

    DirectionsAPI directionsAPI = new DirectionsAPI(new DirectionsAPI.OnDirectionsListener() {
        @Override
        public void onDirectionsReceived(final String travelTime) {
            runOnUiThread(() -> showTravelTimeDialog(travelTime));
        }

        @Override
        public void onDirectionsError(final String error) {
            runOnUiThread(() -> showTravelTimeDialog("Error: " + error));
        }
    });

    private void showTravelTimeDialog(String travelTime) {

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.travel_time_display, null);

        TextView travelTimeTextView = dialogView.findViewById(R.id.travelTimeTextView);
        Button closeButton = dialogView.findViewById(R.id.closeButton);

        travelTimeTextView.setText(travelTime != null ? travelTime : "Unable to retrieve travel time");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        closeButton.setOnClickListener(v -> dialog.dismiss());
    }
}
