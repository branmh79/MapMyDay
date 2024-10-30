package com.SCGIII.mapmyday;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

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
        String urlStr = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin
                + "&destination=" + destination + "&key=" + API_KEY;

        Request request = new Request.Builder()
                .url(urlStr)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onDirectionsError("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String result = response.body().string();
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
                        }
                    } catch (Exception e) {
                        listener.onDirectionsError("Error parsing response: " + e.getMessage());
                    }
                } else {
                    listener.onDirectionsError("API Error: " + response.message());
                }
            }
        });
    }

    public interface OnDirectionsListener {
        void onDirectionsReceived(String travelTime);
        void onDirectionsError(String error);
    }
}