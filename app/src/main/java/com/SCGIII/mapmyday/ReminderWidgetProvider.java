package com.SCGIII.mapmyday;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class ReminderWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // Loop through each widget instance
        for (int appWidgetId : appWidgetIds) {
            // Load data from shared preferences
            SharedPreferences preferences = context.getSharedPreferences("NextEventPrefs", Context.MODE_PRIVATE);
            String eventTitle = preferences.getString("eventTitle", "No Upcoming Events");
            String leaveTime = preferences.getString("leaveTime", "Enjoy your free time!");

            // Set up RemoteViews
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setTextViewText(R.id.widgetEventTitle, eventTitle);
            views.setTextViewText(R.id.widgetLeaveTime, leaveTime);

            // Set up click listener for app icon
            Intent appLaunchIntent = new Intent(context, MainActivity.class);
            PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appLaunchIntent, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widgetAppIcon, appPendingIntent);

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
