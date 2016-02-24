package com.peterung.sunshine.data.api;

import com.peterung.sunshine.data.model.ForecastResponse;

import java.util.Map;

import retrofit.http.GET;
import retrofit.http.QueryMap;

public interface OpenWeatherMapApi {

    @GET("/data/2.5/forecast/daily")
    ForecastResponse getForecast(@QueryMap Map<String, String> options);

}
