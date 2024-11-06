package com.SCGIII.mapmyday;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private String selectedDate;
    private DirectionsAPI directionsAPI;

    // Constructor to initialize event list and selected date
    public EventAdapter(List<Event> eventList, String selectedDate) {
        this.eventList = eventList;
        this.selectedDate = selectedDate;
        this.directionsAPI = new DirectionsAPI(new DirectionsAPI.OnDirectionsListener() {
            @Override
            public void onDirectionsReceived(final String travelTime) {

            }

            @Override
            public void onDirectionsError(final String error) {

            }
        });
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.currentDateTextView.setText("Events on: " + selectedDate);
        holder.currentDateTextView.setVisibility(View.VISIBLE); // Make it visible

        holder.titleTextView.setText(event.getTitle());
        holder.startTimeTextView.setText("Start: " + event.getStartTime());
        holder.endTimeTextView.setText("End: " + event.getEndTime());
        holder.locationTextView.setText("Event Location: " + event.getLocation());
        holder.fromLocationTextView.setText("Start Location: " + event.getFromLocation());
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView currentDateTextView, titleTextView, startTimeTextView, endTimeTextView, locationTextView, fromLocationTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            currentDateTextView = itemView.findViewById(R.id.currentDateTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            fromLocationTextView = itemView.findViewById(R.id.fromLocationTextView);
        }
    }
}
