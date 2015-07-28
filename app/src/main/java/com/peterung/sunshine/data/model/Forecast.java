package com.peterung.sunshine.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;

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

    public String getDisplayValue() {
        String date = getDate().toString();
        return String.format("%s - %s - %d/%d", )
    }


}

