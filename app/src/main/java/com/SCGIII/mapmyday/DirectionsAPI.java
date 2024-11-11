package com.SCGIII.mapmyday;

import android.util.Log;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.io.IOException;

public class DirectionsAPI {

    private final String API_KEY = "AIzaSyC3sbRZqRNm5ZfZuiCu3p3jwz81E61Tuvc";
    private OkHttpClient client = new OkHttpClient();

    // listener to return the result to the front end
    private OnDirectionsListener listener;
    public DirectionsAPI(OnDirectionsListener listener) {
        this.listener = listener;
    }

    public void getTravelTime(String origin, String destination) {
        try {
            // URL encode origin and destination addresses
            String encodedOrigin = URLEncoder.encode(origin, "UTF-8");
            String encodedDestination = URLEncoder.encode(destination, "UTF-8");

            String urlStr = "https://maps.googleapis.com/maps/api/directions/json?origin=" + encodedOrigin
                    + "&destination=" + encodedDestination + "&key=" + API_KEY;

            Log.d("DirectionsAPI", "Request URL: " + urlStr);

            Request request = new Request.Builder()
                    .url(urlStr)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("DirectionsAPI", "Request Failed: " + e.getMessage());
                    listener.onDirectionsError("Network Error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String result = response.body().string();
                        Log.d("DirectionsAPI", "Response: " + result);

                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            JSONArray routes = jsonObject.getJSONArray("routes");

                            if (routes.length() > 0) {
                                JSONObject route = routes.getJSONObject(0);
                                JSONArray legs = route.getJSONArray("legs");
                                JSONObject leg = legs.getJSONObject(0);

                                JSONObject duration = leg.getJSONObject("duration");
                                String travelTime = duration.getString("text");

                                listener.onDirectionsReceived(travelTime);
                            } else {
                                listener.onDirectionsError("No routes found.");
                                Log.e("DirectionsAPI", "No routes found.");
                            }
                        } catch (Exception e) {
                            listener.onDirectionsError("Error parsing response: " + e.getMessage());
                            Log.e("DirectionsAPI", "Parsing Error: " + e.getMessage());
                        }
                    } else {
                        Log.e("DirectionsAPI", "API Error: " + response.message());
                        listener.onDirectionsError("API Error: " + response.message());
                    }
                }
            });
        } catch (Exception e) {
            Log.e("DirectionsAPI", "Encoding Error: " + e.getMessage());
            listener.onDirectionsError("Encoding Error: " + e.getMessage());
        }
    }

    public interface OnDirectionsListener {
        void onDirectionsReceived(String travelTime);
        void onDirectionsError(String error);
    }
}