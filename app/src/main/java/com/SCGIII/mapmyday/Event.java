package com.SCGIII.mapmyday;

import androidx.annotation.NonNull;

public class Event {
    @NonNull
    private String title;
    @NonNull
    private String date; // Use a suitable format (e.g., "YYYY-MM-DD")
    private String startTime; // Use a suitable format (e.g., "HH:mm")
    private String endTime;
    private String location;
    private String fromLocation;

    // No-argument constructor required by Firebase
    public Event() {
        this.title = "";  // Assign a default non-null value
        this.date = "";   // Assign a default non-null value
    }

    // Constructor with parameters
    public Event(@NonNull String title, @NonNull String date, String startTime, String endTime, String location, String fromLocation) {
        this.title = title != null ? title : "";
        this.date = date != null ? date : "";
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.fromLocation = fromLocation;
    }

    // Getters
    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    // Setters
    public void setTitle(@NonNull String title) {
        this.title = title != null ? title : "";
    }

    public void setDate(@NonNull String date) {
        this.date = date != null ? date : "";
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
    }

    // toString method for debugging
    @NonNull
    @Override
    public String toString() {
        return "Event{" +
                "title='" + title + '\'' +
                ", date='" + date + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", location='" + location + '\'' +
                ", fromLocation='" + fromLocation + '\'' +
                '}';
    }
}
