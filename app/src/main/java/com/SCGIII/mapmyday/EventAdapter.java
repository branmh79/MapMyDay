package com.SCGIII.mapmyday;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private String selectedDate;
    private Map<String, String> locationNameToAddress;


    public EventAdapter(List<Event> eventList, String selectedDate, Map<String, String> locationNameToAddress) {
        this.eventList = eventList;
        this.selectedDate = selectedDate;
        this.locationNameToAddress = locationNameToAddress;
    }

    public void updateDate(String date) {
        this.selectedDate = date;
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

        // Set event details
        holder.currentDateTextView.setText("Events on: " + selectedDate);
        holder.titleTextView.setText(event.getTitle().isEmpty() ? "No title" : event.getTitle());
        holder.startTimeTextView.setText("Start: " + (event.getStartTime().isEmpty() ? "N/A" : event.getStartTime()));
        holder.endTimeTextView.setText("End: " + (event.getEndTime().isEmpty() ? "N/A" : event.getEndTime()));
        holder.locationTextView.setText("Event Location: " + (event.getLocation().isEmpty() ? "N/A" : event.getLocation()));
        holder.fromLocationTextView.setText("Start Location: " + (event.getFromLocation().isEmpty() ? "N/A" : event.getFromLocation()));

        if (event.getNotes() != null && !event.getNotes().isEmpty()) {
            holder.notesTextView.setText("Notes: " + event.getNotes());
            holder.notesTextView.setVisibility(View.VISIBLE);
        } else {
            holder.notesTextView.setVisibility(View.GONE);
        }

        // Set the travel time
        String travelTime = event.getTravelTime() != null ? event.getTravelTime() : "Calculating...";
        holder.travelTimeTextView.setText("Travel Time: " + travelTime);

        // Only fetch travel time if still "Calculating..."
        if ("Calculating...".equals(travelTime)) {
            // Resolve from and to addresses
            String fromAddress = locationNameToAddress.containsKey(event.getFromLocation())
                    ? locationNameToAddress.get(event.getFromLocation())  // Use address for favorite location
                    : event.getFromLocation(); // Use raw input for non-favorite location

            String toAddress = locationNameToAddress.containsKey(event.getLocation())
                    ? locationNameToAddress.get(event.getLocation()) // Use address for favorite location
                    : event.getLocation(); // Use raw input for non-favorite location

            // Check if addresses are valid
            if (fromAddress != null && !fromAddress.isEmpty() && toAddress != null && !toAddress.isEmpty()) {
                DirectionsAPI directionsAPI = new DirectionsAPI(new DirectionsAPI.OnDirectionsListener() {
                    @Override
                    public void onDirectionsReceived(final String newTravelTime) {
                        event.setTravelTime(newTravelTime); // Cache the result
                        holder.travelTimeTextView.post(() -> {
                            holder.travelTimeTextView.setText("Travel Time: " + newTravelTime);
                            notifyItemChanged(position); // Update only the current item
                        });
                    }

                    @Override
                    public void onDirectionsError(final String error) {
                        holder.travelTimeTextView.post(() -> {
                            holder.travelTimeTextView.setText("Travel Time: Unavailable");
                        });
                    }
                });

                directionsAPI.getTravelTime(fromAddress, toAddress);
            } else {
                holder.travelTimeTextView.setText("Travel Time: Unavailable");
            }
        }
    }






    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView currentDateTextView, titleTextView, startTimeTextView, endTimeTextView, locationTextView, fromLocationTextView, travelTimeTextView, notesTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            currentDateTextView = itemView.findViewById(R.id.currentDateTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            fromLocationTextView = itemView.findViewById(R.id.fromLocationTextView);
            travelTimeTextView = itemView.findViewById(R.id.travelTimeTextView);
            notesTextView = itemView.findViewById(R.id.eventNotesTextView);
        }
    }
}
