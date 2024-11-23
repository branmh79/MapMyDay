MapMyDay

MapMyDay is a feature-rich Android application designed to manage your events and favorite locations efficiently. The app provides functionalities such as adding events, calculating travel times, creating reminders with Google Maps integration, and customizing themes with a sleek and user-friendly interface.

Features

    •    Event Management
    •    Add, view, and manage events in a visually appealing calendar interface.
    •    Calculate travel times between locations using the Google Maps Directions API.
    •    View event details, including start time, end time, and travel time.
    •    Favorite Locations
    •    Save favorite locations for quicker event creation.
    •    Automatically fetch accurate addresses for favorite locations.
    •    Widget Integration
    •    Add a home screen widget to display the next event with its leave time.
    •    Hyperlink for Google Maps directions to the event destination.
    •    Customizable Themes
    •    Toggle between light and dark themes for an improved user experience.

How It Works

Event Creation

    
Open the app and click the hamburger menu FAB to reveal options.
Select Add Event to create a new event.
Fill in the event details such as title, start and end time, and locations.
Travel time is automatically calculated if both start and destination locations are valid.

Favorite Locations

    
Use the Add Favorite FAB to save frequently visited locations.
Locations are saved in the Firebase database and are accessible for future events.

Widget

    
Add the widget to your home screen from the widget selection menu.
The widget displays:
•    The next upcoming event.
•    A calculated leave time.
•    A “Directions” link for navigation to the event location.

Tech Stack

    •    Languages: Java, XML
    •    Database: Firebase Realtime Database
    •    API: Google Maps Directions API
    •    UI/UX: Material Design components with dynamic FAB and themes

Installation

Prerequisites

    •    Android Studio (Latest version recommended)
    •    Firebase account with a project linked to your application
    •    Google Maps Directions API key

Steps

    
Clone this repository:

git clone https://github.com/branmh79/MapMyDay.git


    
Open the project in Android Studio.
Add your Firebase configuration file (google-services.json) to the app directory.
Replace the API key in DirectionsAPI.java:

private final String API_KEY = "YOUR_API_KEY";


    
Sync the project and build.

Run

    
Connect an Android device or use an emulator.
Build and run the project from Android Studio.

Usage

Calendar

    •    Navigate through months using the left and right arrows.
    •    Tap on a date to view the list of events for that day.

Widget

    •    Add the MapMyDay Widget to your home screen.
    •    Click on the event title or the “Directions” link to interact with your event.

Dark Mode

    •    Toggle dark mode using the FAB menu.
