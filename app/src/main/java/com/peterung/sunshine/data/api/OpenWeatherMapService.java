package com.peterung.sunshine.data.api;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.peterung.sunshine.data.api.OpenWeatherMapApi;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 * Created by peter on 7/23/15.
 */
public class OpenWeatherMapService {
    public static final String API_URL = "http://api.openweathermap.org";
    public static final String API_KEY = "7562113f25e1f1d4b4096d4cf3e72897";
    public static final String RESPONSE_FORMAT = "json";
    public static final String DEFAULT_UNITS = "metric";
    private static OpenWeatherMapApi client;

    public static OpenWeatherMapApi getInstance() {
        if (client != null) {
            return client;
        }

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addQueryParam("APPID", API_KEY);
                        request.addQueryParam("format", RESPONSE_FORMAT);
                        request.addQueryParam("units", DEFAULT_UNITS);
                    }
                })
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(API_URL)
                .build();

        client = restAdapter.create(OpenWeatherMapApi.class);

        return client;
    }

}
