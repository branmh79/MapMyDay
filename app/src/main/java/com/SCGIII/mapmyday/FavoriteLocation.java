package com.SCGIII.mapmyday;

public class FavoriteLocation {
    private String name;
    private String address;

    // Default constructor for Firebase
    public FavoriteLocation() { }

    public FavoriteLocation(String name, String address) {
        this.name = name;
        this.address = address;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
