package com.SCGIII.mapmyday;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private Calendar calendar;
    private TextView monthYearText;
    private GridView calendarGrid;
    private Map<String, List<Event>> eventsMap; // Map to hold events by date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendar = Calendar.getInstance();
        monthYearText = findViewById(R.id.monthYearText);
        calendarGrid = findViewById(R.id.calendarGrid);
        Button prevButton = findViewById(R.id.prevButton);
        Button nextButton = findViewById(R.id.nextButton);

        eventsMap = new HashMap<>(); // Initialize the events map

        updateCalendar();

        prevButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        nextButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        calendarGrid.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDay = getSelectedDay(position);
            showEventAdderDialog(selectedDay);
        });
    }

    private void updateCalendar() {
        monthYearText.setText(String.format(Locale.getDefault(), "%tB %tY", calendar, calendar));
        List<String> days = getDaysInMonth(calendar);
        CalendarAdapter adapter = new CalendarAdapter(this, days, eventsMap);
        calendarGrid.setAdapter(adapter);
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
        return days.get(position);
    }

    private void showEventAdderDialog(String selectedDay) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);

        EditText eventTitle = dialogView.findViewById(R.id.eventTitle);
        EditText eventLocation = dialogView.findViewById(R.id.eventLocation);
        EditText eventStartTime = dialogView.findViewById(R.id.eventStartTime);
        EditText eventEndTime = dialogView.findViewById(R.id.eventEndTime);
        EditText eventFromLocation = dialogView.findViewById(R.id.eventFromLocation);
        Button addEventButton = dialogView.findViewById(R.id.addEventButton);

        AlertDialog dialog = builder.create();

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
}

