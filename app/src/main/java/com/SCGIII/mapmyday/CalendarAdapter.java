package com.SCGIII.mapmyday;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class CalendarAdapter extends BaseAdapter {
    private Context context;
    private List<String> days;
    private Map<String, List<Event>> eventsMap;
    private int year;
    private int month;

    public CalendarAdapter(Context context, List<String> days, Map<String, List<Event>> eventsMap, int year, int month) {
        this.context = context;
        this.days = days;
        this.eventsMap = eventsMap;
        this.year = year;
        this.month = month;
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

        // Detect if in night mode
        boolean isNightMode = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        // Set background color for empty cells
        if (day.isEmpty()) {
            convertView.setBackgroundColor(isNightMode ?
                    context.getResources().getColor(R.color.calendarEmptyDayDarkBackground) :
                    context.getResources().getColor(R.color.calendarEmptyDayBackground));
            dayText.setText("");
        } else {
            // Background color for regular days
            convertView.setBackgroundColor(context.getResources().getColor(R.color.calendarDayBackground));
            dayText.setText(day);

            // Highlight days with events if applicable
            String dateKey = String.format("%d-%02d-%02d", year, month, Integer.parseInt(day));
            if (eventsMap.containsKey(dateKey) && !eventsMap.get(dateKey).isEmpty()) {
                convertView.setBackgroundColor(context.getResources().getColor(R.color.calendarEventDayBackground));
            }
        }

        return convertView;
    }



}