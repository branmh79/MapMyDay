package com.SCGIII.mapmyday;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class CalendarAdapter extends BaseAdapter {
    private Context context;
    private List<String> days; // List of days in the month

    public CalendarAdapter(Context context, List<String> days) {
        this.context = context;
        this.days = days;
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

        if (day.isEmpty()) {
            dayText.setText(""); // Set empty text for empty spaces
            convertView.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray)); // Set background for empty days
        } else {
            dayText.setText(day);
            convertView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent)); // Reset background for actual days
        }

        return convertView;
    }


}
