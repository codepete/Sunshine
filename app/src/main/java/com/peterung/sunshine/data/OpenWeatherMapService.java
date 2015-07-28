package com.peterung.sunshine.data;

import com.peterung.sunshine.data.api.OpenWeatherMapApi;

import retrofit.RestAdapter;

/**
 * Created by peter on 7/23/15.
 */
public class OpenWeatherMapService {
    public static final String API_URL = "http://api.openweathermap.org";
    private static OpenWeatherMapApi client;

    public static OpenWeatherMapApi getInstance() {
        if (client != null) {
            return client;
        }

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(API_URL)
                .build();

        client = restAdapter.create(OpenWeatherMapApi.class);

        return client;
    }
}
