package com.peterung.sunshine.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by peter on 7/23/15.
 */
public class ForecastResponse {
    public City city;
    public String cod;
    public float message;

    @SerializedName("cnt")
    public int count;

    @SerializedName("list")
    public Forecast[] forecasts;

}
