package com.SCGIII.mapmyday;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Calendar calendar;
    private TextView monthYearText;
    private GridView calendarGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendar = Calendar.getInstance();
        monthYearText = findViewById(R.id.monthYearText);
        calendarGrid = findViewById(R.id.calendarGrid);
        Button prevButton = findViewById(R.id.prevButton);
        Button nextButton = findViewById(R.id.nextButton);

        updateCalendar();

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, -1);
                updateCalendar();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, 1);
                updateCalendar();
            }
        });
    }

    private void updateCalendar() {
        monthYearText.setText(String.format(Locale.getDefault(), "%tB %tY", calendar, calendar));
        List<String> days = getDaysInMonth(calendar);
        CalendarAdapter adapter = new CalendarAdapter(this, days);
        calendarGrid.setAdapter(adapter);
    }

    private List<String> getDaysInMonth(Calendar calendar) {
        List<String> days = new ArrayList<>();

        // Set the calendar to the first day of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // Get the number of days in the month
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Get the day of the week for the first day of the month
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // 1 = Sunday, 2 = Monday, ..., 7 = Saturday

        // Add empty spaces for days before the first day of the month
        for (int i = 1; i < firstDayOfWeek; i++) {
            days.add(""); // Add empty string for empty spaces
        }

        // Add the actual days of the month
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(String.valueOf(i));
        }

        return days;
    }

}
