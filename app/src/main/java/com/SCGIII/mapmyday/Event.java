package com.SCGIII.mapmyday;

public class Event {
    private String title;
    private String date; // Format: "YYYY-MM-DD"
    private String startTime; // Format: "HH:mm"
    private String endTime; // Format: "HH:mm"
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
