package com.SCGIII.mapmyday;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
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


    //instantiating variables
    private Calendar calendar;
    private TextView monthYearText;
    private GridView calendarGrid;
    private Map<String, List<Event>> eventsMap; // Map to hold events by date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //gets the layouts from activity_main.xml
        setContentView(R.layout.activity_main);

        calendar = Calendar.getInstance();
        monthYearText = findViewById(R.id.monthYearText);
        calendarGrid = findViewById(R.id.calendarGrid);
        Button prevButton = findViewById(R.id.prevButton);
        Button nextButton = findViewById(R.id.nextButton);

        /*hash map (key-value pairs for events and event details). Can have multiple event details for
          one event using ArrayList. Will implememnt later when we have backend finished*/
        eventsMap = new HashMap<>(); // Initialize the events map

        updateCalendar();

        //calls to update calendar to prev month whenever prev button is pushed
        prevButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        //calls to update calendar to next month whenever next button is pushed
        nextButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });


        /*parent is the whole grid view, view is the view clicked (day), position is position of the (day) item
        , and ID is specific ID of item clicked. Is not used here, but it may come in handy later?
        For ex: we might use ID to query item sin Cody's DB later*/
        calendarGrid.setOnItemClickListener((parent, view, position, ID) -> {
            String selectedDay = getSelectedDay(position);
            showEventAdderDialog(selectedDay);
        });
    }

    //updates the calendar. This method is called every time you click on a new month or load a new view
    private void updateCalendar() {
        monthYearText.setText(String.format(Locale.getDefault(), "%tB %tY", calendar, calendar));
        List<String> days = getDaysInMonth(calendar);
        CalendarAdapter adapter = new CalendarAdapter(this, days, eventsMap);
        calendarGrid.setAdapter(adapter);
    }


    /*utilizng the calendar import in java, implements the correct formatting for days in a month
    for example, not every month starts on a Sunday. Sometime syou have blank days at the start of the week.*/
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

    //this is called when user clicks on a day. gets the correct day in the list of days using the provided position
    private String getSelectedDay(int position) {
        List<String> days = getDaysInMonth(calendar);
        return days.get(position);
    }


//this right here is the meat and potatoes of event adder feature
    private void showEventAdderDialog(String selectedDay) {
        //instanstiates alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //inflates from XML dialog_add_event. sets view
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);

        //gets the Ids from XML :o
        EditText eventTitle = dialogView.findViewById(R.id.eventTitle);
        EditText eventLocation = dialogView.findViewById(R.id.eventLocation);
        EditText eventStartTime = dialogView.findViewById(R.id.eventStartTime);
        EditText eventEndTime = dialogView.findViewById(R.id.eventEndTime);
        EditText eventFromLocation = dialogView.findViewById(R.id.eventFromLocation);
        Button addEventButton = dialogView.findViewById(R.id.addEventButton);

        //creates aforementioned alert dialog
        AlertDialog dialog = builder.create();

        // yet another lambda expression ;)
        //event adder button. These strings and input request display when you click on a day
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

