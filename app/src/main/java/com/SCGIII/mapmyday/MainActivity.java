package com.SCGIII.mapmyday;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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


    protected void loadEvents() {
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventsMap.clear();
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    //String dateKey = dateSnapshot.getKey() != null ? dateSnapshot.getKey() : "";
                    //List<Event> eventsList = new ArrayList<>();
                    for (DataSnapshot eventSnapshot : dateSnapshot.getChildren()) {
                        Event event = eventSnapshot.getValue(Event.class);
                        if (event != null) {
                            if (event.getRecurrenceFrequency() != null && !event.getRecurrenceFrequency().equalsIgnoreCase("None")){
                                List<String> occurrenceDates = calculateOccurrences(event);
                                for (String occurrenceDate : occurrenceDates) {
                                    addEventToMap(occurrenceDate, event);
                                }
                            } else {
                                addEventToMap(event.getDate(), event);
                                //eventsList.add(event);
                            }
                        }
                    }
                    //eventsMap.put(dateKey, eventsList);
                }

                // Calculate the next event
                Event nextEvent = getNextEvent();
                saveNextEventToPreferences(nextEvent);

                // Notify widget to refresh
                updateWidget();

                updateCalendar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load events.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWidget() {
        Intent intent = new Intent(this, ReminderWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        // Send broadcast to update all widgets
        int[] ids = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(this, ReminderWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
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
        // Recurrence fields here
        Spinner recurrenceFrequency = dialogView.findViewById(R.id.recurrenceFrequency);
        TextView recurrenceEndDate = dialogView.findViewById(R.id.recurrenceEndDate);
        LinearLayout daysOfWeekLayout = dialogView.findViewById(R.id.daysOfWeekLayout);
        // Initializing the checkboxes
        CheckBox checkSunday = dialogView.findViewById(R.id.checkSunday);
        CheckBox checkMonday = dialogView.findViewById(R.id.checkMonday);
        CheckBox checkTuesday = dialogView.findViewById(R.id.checkTuesday);
        CheckBox checkWednesday = dialogView.findViewById(R.id.checkWednesday);
        CheckBox checkThursday = dialogView.findViewById(R.id.checkThursday);
        CheckBox checkFriday = dialogView.findViewById(R.id.checkFriday);
        CheckBox checkSaturday = dialogView.findViewById(R.id.checkSaturday);
        // Setting up the recurrency options
        String[] frequencies = new String[]{"None", "Daily", "Weekly", "Bi-Weekly", "Monthly"};
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, frequencies);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recurrenceFrequency.setAdapter(frequencyAdapter);
        // Set the default
        recurrenceFrequency.setSelection(0); // Index, None
        // Visibility based on which one selected
        recurrenceFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedFrequency = (String) adapterView.getItemAtPosition(i);

                if (!"NONE".equalsIgnoreCase(selectedFrequency)){
                    recurrenceEndDate.setVisibility(View.VISIBLE);
                } else {
                    recurrenceEndDate.setVisibility(View.GONE);
                }

                if ("WEEKLY".equalsIgnoreCase(selectedFrequency) || "BI-WEEKLY".equalsIgnoreCase(selectedFrequency)) {
                    daysOfWeekLayout.setVisibility(View.VISIBLE);
                } else {
                    daysOfWeekLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Nothing
            }
        });
        // Date picker for the end date of the recurrence
        Calendar recurrenceEndCalendar = Calendar.getInstance();
        recurrenceEndDate.setOnClickListener(v -> {
            int year = recurrenceEndCalendar.get(Calendar.YEAR);
            int month = recurrenceEndCalendar.get(Calendar.MONTH);
            int day = recurrenceEndCalendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                recurrenceEndCalendar.set(Calendar.YEAR, selectedYear);
                recurrenceEndCalendar.set(Calendar.MONTH, selectedMonth);
                recurrenceEndCalendar.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth);
                String formattedDate = String.format(Locale.getDefault(), "%1$tY-%1$tm-%1$td", recurrenceEndCalendar);
                recurrenceEndDate.setText(formattedDate);
            }, year, month, day);
            datePickerDialog.show();
        });

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
            //Recurrence
            String frequencyInput = ((String) recurrenceFrequency.getSelectedItem()).toUpperCase();
            int interval = 1;

            if ("Bi-Weekly".equalsIgnoreCase(frequencyInput)){
                frequencyInput = "WEEKLY";
                interval = 2;
            } else if ("Weekly".equalsIgnoreCase(frequencyInput) || "Monthly".equalsIgnoreCase(frequencyInput) || "Daily".equalsIgnoreCase(frequencyInput)) {
                // Stays at 1
            } else if ("None".equalsIgnoreCase(frequencyInput)) {
                frequencyInput = "NONE";
            } else {
                frequencyInput = frequencyInput.toUpperCase();
            }

            String recurrenceEndDateInput = recurrenceEndDate.getText().toString().trim();
            List<String> selectedDaysOfWeek = new ArrayList<>();
            if ("WEEKLY".equalsIgnoreCase(frequencyInput)){
                if (checkSunday.isChecked()){ selectedDaysOfWeek.add("SU"); }
                if (checkMonday.isChecked()){ selectedDaysOfWeek.add("MO"); }
                if (checkTuesday.isChecked()){ selectedDaysOfWeek.add("TU"); }
                if (checkWednesday.isChecked()){ selectedDaysOfWeek.add("WE"); }
                if (checkThursday.isChecked()){ selectedDaysOfWeek.add("TH"); }
                if (checkFriday.isChecked()){ selectedDaysOfWeek.add("FR"); }
                if (checkSaturday.isChecked()){ selectedDaysOfWeek.add("SA"); }
            }

            if (title.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // End Date validation for repeating events
            if (!"NONE".equalsIgnoreCase(frequencyInput)) {
                if (recurrenceEndDateInput.isEmpty()) {
                    Toast.makeText(this, "Please set a recurrence end date.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if ("WEEKLY".equalsIgnoreCase(frequencyInput) && selectedDaysOfWeek.isEmpty()){
                Toast.makeText(this, "Please select at least one day of the week.", Toast.LENGTH_SHORT).show();
                return;
            }


            // Check if inputs are favorited locations
            String locationAddress = locationNameToAddress.containsKey(locationInput) ? locationNameToAddress.get(locationInput) : locationInput;
            String fromLocationAddress = locationNameToAddress.containsKey(fromLocationInput) ? locationNameToAddress.get(fromLocationInput) : fromLocationInput;

            // Log to verify addresses being used
            Log.d("EventAdder", "From Address: " + fromLocationAddress + ", To Address: " + locationAddress);

            // Create the event object
            String dateKey = String.format(Locale.getDefault(), "%1$tY-%1$tm-%1$td", calendar);
            Event newEvent = new Event(title, dateKey, startTime, endTime, locationInput, fromLocationInput, notes,
                    frequencyInput, interval, recurrenceEndDateInput, selectedDaysOfWeek);

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

        List<Event> eventsForDate = eventsMap.getOrDefault(selectedDate, new ArrayList<>());

        if (!eventsForDate.isEmpty()){
            eventList.clear();
            eventList.addAll(eventsForDate);
            eventAdapter.notifyDataSetChanged();
            eventRecyclerView.setVisibility(View.VISIBLE);
        } else{
            Toast.makeText(MainActivity.this, "No events for this date", Toast.LENGTH_SHORT).show();
            eventRecyclerView.setVisibility(View.GONE);
        }

        /*
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
         */
    }


    private void saveNextEventToPreferences(Event nextEvent) {
        SharedPreferences preferences = getSharedPreferences("NextEventPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (nextEvent != null) {
            String leaveTime = calculateLeaveTime(nextEvent.getStartTime(), nextEvent.getTravelTime());
            editor.putString("eventTitle", nextEvent.getTitle());
            editor.putString("leaveTime", String.format("Leave By: %s", leaveTime));
        } else {
            editor.putString("eventTitle", "No Upcoming Events");
            editor.putString("leaveTime", "Enjoy your free time!");
        }

        editor.apply();
    }



    public String calculateLeaveTime(String startTime, String travelTime) {
        try {
            // Parse startTime (HH:mm)
            String[] timeParts = startTime.split(":");
            int eventHour = Integer.parseInt(timeParts[0]);
            int eventMinute = Integer.parseInt(timeParts[1]);

            // Convert event start time to minutes
            int eventStartMinutes = (eventHour * 60) + eventMinute;

            // Parse travelTime (e.g., "15 mins")
            String[] travelParts = travelTime.split(" ");
            int travelMinutes = Integer.parseInt(travelParts[0]);

            // Calculate leave time in minutes
            int leaveTimeMinutes = eventStartMinutes - travelMinutes;

            // Convert leave time back to HH:mm
            int leaveHour = leaveTimeMinutes / 60;
            int leaveMinute = leaveTimeMinutes % 60;

            // Format time to HH:mm
            return String.format(Locale.getDefault(), "%02d:%02d", leaveHour, leaveMinute);
        } catch (Exception e) {
            Log.e("CalculateLeaveTime", "Error parsing time: " + e.getMessage());
            return "Error calculating leave time";
        }
    }

    public Event getNextEvent() {
        long currentTimeMillis = System.currentTimeMillis();
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(currentTimeMillis);

        Event nextEvent = null;
        long smallestDifference = Long.MAX_VALUE;

        for (List<Event> events : eventsMap.values()) {
            for (Event event : events) {
                try {
                    // Parse event date and start time
                    String[] dateParts = event.getDate().split("-");
                    String[] timeParts = event.getStartTime().split(":");

                    int eventYear = Integer.parseInt(dateParts[0]);
                    int eventMonth = Integer.parseInt(dateParts[1]) - 1; // Calendar months are 0-based
                    int eventDay = Integer.parseInt(dateParts[2]);
                    int eventHour = Integer.parseInt(timeParts[0]);
                    int eventMinute = Integer.parseInt(timeParts[1]);

                    // Create a Calendar object for the event
                    Calendar eventTime = Calendar.getInstance();
                    eventTime.set(eventYear, eventMonth, eventDay, eventHour, eventMinute);

                    // Calculate time difference
                    long timeDifference = eventTime.getTimeInMillis() - currentTimeMillis;

                    // Check if this event is closer than the current nextEvent
                    if (timeDifference > 0 && timeDifference < smallestDifference) {
                        smallestDifference = timeDifference;
                        nextEvent = event;
                    }
                } catch (Exception e) {
                    Log.e("GetNextEvent", "Error parsing event: " + e.getMessage());
                }
            }
        }

        return nextEvent;
    }

    private List<String> calculateOccurrences(Event event){
        List<String> occurrenceDates = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());

        try {
            Date startDate = sdf.parse(event.getDate());
            Date endDate = sdf.parse(event.getRecurrenceEndDate());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            String recurrenceFrequency = event.getRecurrenceFrequency().toUpperCase();
            int interval = event.getRecurrenceInterval();

            switch (recurrenceFrequency) {
                    case "DAILY" :
                        while (!calendar.getTime().after(endDate)) {
                            occurrenceDates.add(sdf.format(calendar.getTime()));
                            calendar.add(Calendar.DAY_OF_MONTH, interval);
                        }
                        break;
                    case "WEEKLY":
                        while (!calendar.getTime().after(endDate)) {
                            Calendar weekStart = (Calendar) calendar.clone();
                            for (int i = 0; i < 7; i++) {
                                if (calendar.getTime().after(endDate)){
                                    break;
                                }
                                String currentDayOfWeek = new SimpleDateFormat("EE", Locale.ENGLISH)
                                        .format(calendar.getTime()).toUpperCase(Locale.ENGLISH).substring(0,2);
                                if (event.getRecurrenceDaysOfWeek().contains(currentDayOfWeek)) {
                                    occurrenceDates.add(sdf.format(calendar.getTime()));
                                }
                                calendar.add(Calendar.DAY_OF_MONTH, 1);
                            }
                            calendar = (Calendar) weekStart.clone();
                            calendar.add(Calendar.WEEK_OF_YEAR, interval);
                        }
                        break;
                    case "MONTHLY":
                        while (!calendar.getTime().after(endDate)) {
                            if (calendar.get(Calendar.DAY_OF_MONTH) == Integer.parseInt(event.getDate().split("-")[2])) {
                                occurrenceDates.add(sdf.format(calendar.getTime()));
                            }
                            calendar.add(Calendar.MONTH, interval);
                        }
                        break;
                    default:
                        break;
                }
            } catch (ParseException e) {
            e.printStackTrace();
        }
        return occurrenceDates;
    }

    private void addEventToMap(String date, Event event){
        if (!eventsMap.containsKey(date)){
            eventsMap.put(date, new ArrayList<>());
        }
        eventsMap.get(date).add(event);
    }

}