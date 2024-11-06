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

        dayText.setTextColor(context.getResources().getColor(R.color.calendarTextColor));

        if (day.isEmpty()) {
            dayText.setText("");
            convertView.setBackgroundColor(context.getResources().getColor(R.color.calendarEmptyDayBackground)); // Background for empty days
        } else {
            dayText.setText(day);
            convertView.setBackgroundColor(context.getResources().getColor(R.color.calendarDayBackground)); // Reset background for actual days

            String dateKey = String.format("%d-%02d-%02d", year, month, Integer.parseInt(day));

            if (eventsMap.containsKey(dateKey) && !eventsMap.get(dateKey).isEmpty()) {
                convertView.setBackgroundColor(context.getResources().getColor(R.color.calendarEventDayBackground)); // Highlight days with events
            }
        }

        return convertView;
    }
}