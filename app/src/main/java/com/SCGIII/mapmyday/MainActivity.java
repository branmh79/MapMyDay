package com.SCGIII.mapmyday;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Calendar calendar;
    private TextView monthYearText;
    private GridView calendarGrid;
    private Map<String, List<Event>> eventsMap = new HashMap<>();
    private DatabaseReference eventsRef;
    private RecyclerView eventRecyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList = new ArrayList<>();
    private List<FavoriteLocation> favoriteLocations = new ArrayList<>();
    private Map<String, String> locationNameToAddress = new HashMap<>();
    private String selectedDate = "";

    private FloatingActionButton addEventButton;
    private FloatingActionButton fabAddEvent;
    private FloatingActionButton fabAddFavorite;
    private FloatingActionButton fabToggleTheme;
    private boolean isFabMenuOpen = false;
    private boolean isDarkModeEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase reference
        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        // Initialize UI components
        calendar = Calendar.getInstance();
        monthYearText = findViewById(R.id.monthYearText);
        calendarGrid = findViewById(R.id.calendarGrid);
        ImageButton prevButton = findViewById(R.id.prevButton);
        ImageButton nextButton = findViewById(R.id.nextButton);
        eventRecyclerView = findViewById(R.id.eventRecyclerView);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(eventList, selectedDate, locationNameToAddress);
        eventRecyclerView.setAdapter(eventAdapter);

        // Get theme preference
        SharedPreferences sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        isDarkModeEnabled = sharedPreferences.getBoolean("isDarkModeEnabled", false);

        // Initialize FABs
        addEventButton = findViewById(R.id.addEventButton);
        fabAddEvent = findViewById(R.id.fabAddEvent);
        fabAddFavorite = findViewById(R.id.fabAddFavorite);
        fabToggleTheme = findViewById(R.id.fabToggleTheme);

        // Check if all views are found
        if (addEventButton == null || fabAddEvent == null || fabAddFavorite == null || fabToggleTheme == null) {
            Toast.makeText(this, "Error initializing FABs. Check XML IDs.", Toast.LENGTH_SHORT).show();
            return; // Exit early if there’s a null reference
        }

        // Setup FAB listeners
        addEventButton.setOnClickListener(v -> toggleFabMenu());
        fabAddEvent.setOnClickListener(v -> {
            toggleFabMenu();
            showEventAdderDialog(null);
        });
        fabAddFavorite.setOnClickListener(v -> {
            toggleFabMenu();
            showFavoriteLocationDialog();
        });
        fabToggleTheme.setOnClickListener(v -> toggleTheme());

        // Set theme and load events
        setThemeMode();
        loadEvents();
        loadFavoriteLocations();

        // Calendar navigation
        prevButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });
        nextButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        // Calendar click listener
        calendarGrid.setOnItemClickListener((parent, view, position, ID) -> {
            String selectedDate = getSelectedDate(position);
            if (!selectedDate.isEmpty()) {
                showEventsForSelectedDate(selectedDate);
            } else {
                Toast.makeText(this, "No valid day selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setThemeMode() {
        AppCompatDelegate.setDefaultNightMode(
                isDarkModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        fabToggleTheme.setImageResource(isDarkModeEnabled ? R.drawable.sun : R.drawable.moon); // Update icon
    }

    private void toggleTheme() {
        isDarkModeEnabled = !isDarkModeEnabled;
        setThemeMode();

        SharedPreferences.Editor editor = getSharedPreferences("ThemePrefs", MODE_PRIVATE).edit();
        editor.putBoolean("isDarkModeEnabled", isDarkModeEnabled);
        editor.apply();
    }

    private void toggleFabMenu() {
        int visibility = isFabMenuOpen ? View.GONE : View.VISIBLE;
        fabAddEvent.setVisibility(visibility);
        fabAddFavorite.setVisibility(visibility);
        fabToggleTheme.setVisibility(visibility);
        isFabMenuOpen = !isFabMenuOpen;

    }

    private void loadFavoriteLocations() {
        DatabaseReference favLocRef = FirebaseDatabase.getInstance().getReference("favloc");
        favLocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                favoriteLocations.clear();
                locationNameToAddress.clear();  // Ensure map is cleared each load

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FavoriteLocation location = snapshot.getValue(FavoriteLocation.class);
                    if (location != null) {
                        favoriteLocations.add(location);
                        locationNameToAddress.put(location.getName(), location.getAddress());

                        // Logging to confirm each location loaded with correct mapping
                        Log.d("FavoriteLocation", "Loaded: " + location.getName() + " -> " + location.getAddress());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load favorite locations.", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void showFavoriteLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_favorite, null);
        builder.setView(dialogView);

        EditText locationName = dialogView.findViewById(R.id.locationName);
        EditText locationAddress = dialogView.findViewById(R.id.locationAddress);
        Button addFavoriteButton = dialogView.findViewById(R.id.addFavoriteButton);

        AlertDialog dialog = builder.create();

        addFavoriteButton.setOnClickListener(v -> {
            String name = locationName.getText().toString().trim();
            String address = locationAddress.getText().toString().trim();

            if (name.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please enter both a name and an address.", Toast.LENGTH_SHORT).show();
            } else {
                // Create a new FavoriteLocation object
                FavoriteLocation favoriteLocation = new FavoriteLocation(name, address);

                // Store the favorite location under "favloc" in Firebase
                DatabaseReference favLocRef = FirebaseDatabase.getInstance().getReference("favloc");
                String locationId = favLocRef.push().getKey(); // Unique ID for each favorite location
                if (locationId != null) {
                    favLocRef.child(name).setValue(favoriteLocation)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Favorite location added: " + name, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(error -> {
                                Toast.makeText(this, "Failed to add favorite location: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });

        dialog.show();
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

    private String getCurrentMonth() {
        int month = calendar.get(Calendar.MONTH);
        return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
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
        AutoCompleteTextView eventLocation = dialogView.findViewById(R.id.eventLocation);
        AutoCompleteTextView eventFromLocation = dialogView.findViewById(R.id.eventFromLocation);
        EditText eventStartTime = dialogView.findViewById(R.id.eventStartTime);
        EditText eventEndTime = dialogView.findViewById(R.id.eventEndTime);
        EditText eventNotes = dialogView.findViewById(R.id.eventNotes);
        Button addEventButton = dialogView.findViewById(R.id.addEventButton);
        AlertDialog dialog = builder.create();

        // Set up AutoCompleteTextView adapters for favorite locations
        List<String> locationNames = new ArrayList<>(locationNameToAddress.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locationNames);
        eventLocation.setAdapter(adapter);
        eventFromLocation.setAdapter(adapter);

        // Set up date selection
        Calendar calendar = Calendar.getInstance();
        if (selectedDay != null) {
            String[] parts = selectedDay.split("-");
            calendar.set(Calendar.YEAR, Integer.parseInt(parts[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
        }
        updateEventDateText(eventDate, calendar);

        eventDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                        calendar.set(Calendar.YEAR, selectedYear);
                        calendar.set(Calendar.MONTH, selectedMonth);
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth);
                        updateEventDateText(eventDate, calendar);
                    }, year, month, day);
            datePickerDialog.show();
        });

        addEventButton.setOnClickListener(v -> {
            String title = eventTitle.getText().toString().trim();
            String locationInput = eventLocation.getText().toString().trim();
            String fromLocationInput = eventFromLocation.getText().toString().trim();
            String startTime = eventStartTime.getText().toString().trim();
            String endTime = eventEndTime.getText().toString().trim();
            String notes = eventNotes.getText().toString().trim();

            if (title.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if inputs are favorited locations
            String locationAddress = locationNameToAddress.containsKey(locationInput) ? locationNameToAddress.get(locationInput) : locationInput;
            String fromLocationAddress = locationNameToAddress.containsKey(fromLocationInput) ? locationNameToAddress.get(fromLocationInput) : fromLocationInput;

            // Log to verify addresses being used
            Log.d("EventAdder", "From Address: " + fromLocationAddress + ", To Address: " + locationAddress);

            // Create the event object
            String dateKey = String.format(Locale.getDefault(), "%1$tY-%1$tm-%1$td", calendar);
            Event newEvent = new Event(title, dateKey, startTime, endTime, locationInput, fromLocationInput, notes);

            // Call DirectionsAPI to calculate travel time
            DirectionsAPI directionsAPI = new DirectionsAPI(new DirectionsAPI.OnDirectionsListener() {
                @Override
                public void onDirectionsReceived(String travelTime) {
                    // Update travel time in the event and database
                    newEvent.setTravelTime(travelTime);
                    eventsRef.child(dateKey).child(title).setValue(newEvent)
                            .addOnSuccessListener(aVoid -> runOnUiThread(() ->
                                    Toast.makeText(MainActivity.this, "Event added successfully with travel time!", Toast.LENGTH_SHORT).show()))
                            .addOnFailureListener(error -> runOnUiThread(() ->
                                    Toast.makeText(MainActivity.this, "Failed to add event: " + error.getMessage(), Toast.LENGTH_SHORT).show()));
                }

                @Override
                public void onDirectionsError(String error) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Error calculating travel time: " + error, Toast.LENGTH_SHORT).show());
                }
            });

            directionsAPI.getTravelTime(fromLocationAddress, locationAddress);

            // Add event to the UI immediately with "Calculating..."
            addEventToDay(dateKey, newEvent);
            eventAdapter.notifyDataSetChanged();
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

    private void showEventsForSelectedDate(String date) {
        selectedDate = date;
        eventAdapter.updateDate(selectedDate);

        eventsRef.child(selectedDate).addListenerForSingleValueEvent(new ValueEventListener() {
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
                    eventAdapter.notifyDataSetChanged();
                    eventRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(MainActivity.this, "No events for this date", Toast.LENGTH_SHORT).show();
                    eventRecyclerView.setVisibility(View.GONE);
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