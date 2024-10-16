package com.SCGIII.mapmyday;

//import androidx.room.Entity;
//import androidx.room.PrimaryKey;

public class Event {
    private String title;
    private String date; // Use a suitable format (e.g., "YYYY-MM-DD")
    private String time; // Use a suitable format (e.g., "HH:mm")
    private String location;

    public Event(String title, String date, String time, String location) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.location = location;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getLocation() { return location; }
}

