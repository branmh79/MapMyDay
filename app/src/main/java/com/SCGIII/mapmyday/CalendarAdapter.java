package com.SCGIII.mapmyday;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class CalendarAdapter extends BaseAdapter {
    private Context context;
    private List<String> days; // List of days in the month
    private Map<String, List<Event>> eventsMap; // Map to hold events by date

    public CalendarAdapter(Context context, List<String> days, Map<String, List<Event>> eventsMap) {
        this.context = context;
        this.days = days;
        this.eventsMap = eventsMap;
    }

    @Override
    public int getCount() {
        return days.size();
    }

    @Override
    public Object getItem(int position) {
        return days.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.calendar_day_item, parent, false);
        }

        TextView dayText = convertView.findViewById(R.id.dayText);
        String day = days.get(position);

        dayText.setTextColor(context.getResources().getColor(R.color.calendarTextColor));

        // Check if the day is empty
        if (day.isEmpty()) {
            dayText.setText(""); // Set empty text for empty spaces
            convertView.setBackgroundColor(context.getResources().getColor(R.color.calendarEmptyDayBackground)); // Background for empty days
        } else {
            dayText.setText(day);
            convertView.setBackgroundColor(context.getResources().getColor(R.color.calendarDayBackground)); // Reset background for actual days

            // Check if there are events for this day
            String dateKey = "2023-10-" + day; // Example date format, adjust as needed
            if (eventsMap.containsKey(dateKey) && !eventsMap.get(dateKey).isEmpty()) {
                convertView.setBackgroundColor(context.getResources().getColor(R.color.calendarEventDayBackground)); // Highlight days with events
            }
        }

        return convertView;
    }
}
