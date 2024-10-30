package com.SCGIII.mapmyday;

//import androidx.room.Entity;
//import androidx.room.PrimaryKey;

public class Event {
    private String title;
    private String date; // Use a suitable format (e.g., "YYYY-MM-DD")
    private String startTime; // Use a suitable format (e.g., "HH:mm")
    private String endTime;
    private String location;
    private String fromLocation;

    public Event(String title, String date, String startTime, String endTime, String location, String fromLocation) {
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.fromLocation = fromLocation;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getLocation() { return location; }
    public String getFromLocation() { return fromLocation; }

    //test test github change test
}

