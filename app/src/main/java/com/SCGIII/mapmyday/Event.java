package com.SCGIII.mapmyday;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Event {
    @NonNull
    private String title;
    @NonNull
    private String date; // Use a suitable format (e.g., "YYYY-MM-DD")
    private String startTime; // Use a suitable format (e.g., "HH:mm")
    private String endTime;
    private String location;
    private String fromLocation;
    private String travelTime = "Calculating...";
    private String notes;

    // Recurrence!
    private String recurrenceFrequency;
    private int recurrenceInterval;
    private String recurrenceEndDate;
    private List<String> recurrenceDaysOfWeek;

    // No-argument constructor required by Firebase
    public Event() {
        this.title = "";  // Assign a default non-null value
        this.date = "";   // Assign a default non-null value
        this.recurrenceFrequency = "None"; // Assigning a default for non-recurring events
        this.recurrenceInterval = 1;
        this.recurrenceEndDate = "";
        this.recurrenceDaysOfWeek = new ArrayList<>();
    }

    public String getTravelTime(){
        return travelTime != null ? travelTime : "Calculating...";
    }

    public void setTravelTime(String travelTime)
    {
        this.travelTime = travelTime;
    }

    // Constructor with parameters
    public Event(@NonNull String title, @NonNull String date, String startTime, String endTime, String location, String fromLocation, String notes, String recurrenceFrequency, int recurrenceInterval,
                 String recurrenceEndDate, List<String> recurrenceDaysOfWeek) {
        this.title = title != null ? title : "";
        this.date = date != null ? date : "";
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.fromLocation = fromLocation;
        this.travelTime = "Calculating...";
        this.notes = notes;
        this.recurrenceFrequency = recurrenceFrequency != null ? recurrenceFrequency : "None";
        this.recurrenceInterval = recurrenceInterval;
        this.recurrenceEndDate = recurrenceEndDate != null ? recurrenceEndDate : "";
        this.recurrenceDaysOfWeek = recurrenceDaysOfWeek != null ? recurrenceDaysOfWeek : new ArrayList<>();
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

    public String getNotes() { return notes; }

    public String getRecurrenceFrequency() { return recurrenceFrequency; }

    public int getRecurrenceInterval() { return recurrenceInterval; }

    public String getRecurrenceEndDate() { return recurrenceEndDate; }

    public List<String> getRecurrenceDaysOfWeek() { return recurrenceDaysOfWeek; }

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

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setRecurrenceFrequency(String recurrenceFrequency) { this.recurrenceFrequency = recurrenceFrequency; }

    public void setRecurrenceInterval(int recurrenceInterval) { this.recurrenceInterval = recurrenceInterval; }

    public void setRecurrenceEndDate(String recurrenceEndDate) { this.recurrenceEndDate = recurrenceEndDate; }

    public void setRecurrenceDaysOfWeek(List<String> recurrenceDaysOfWeek) { this.recurrenceDaysOfWeek = recurrenceDaysOfWeek; }

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
