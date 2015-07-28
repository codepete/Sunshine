package com.peterung.sunshine.data.model;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by peter on 7/23/15.
 */
public class Forecast {
    public long dt;

    @SerializedName("temp")
    public Temperature temperature;

    public  float pressure;
    public int humidity;

    @SerializedName("weather")
    public Weather[] weathers;
    public float speed;
    public int deg;

    public Date getDate() {
        return new Date(dt * 1000);
    }

    public String getWeatherValue() {
        if (weathers.length == 0) {
            return null;
        }

        Weather weather = weathers[0];
        return weather.main;
    }

    public String getDisplayValue() {
        String date = new SimpleDateFormat("EEE, MMM d", Locale.US).format(getDate());
        return String.format("%s - %s - %d/%d", date, getWeatherValue(), (int)temperature.max, (int)temperature.min);
    }


}

