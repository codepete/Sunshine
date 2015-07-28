package com.peterung.sunshine.data.api;

import com.peterung.sunshine.data.model.ForecastResponse;

import java.util.Map;

import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.http.QueryMap;
import rx.Observable;

/**
 * Created by peter on 7/23/15.
 */
public interface OpenWeatherMapApi {

    @GET("/data/2.5/forecast/daily?format=json&units=metric")
    Observable<ForecastResponse> getForecast(@QueryMap Map<String, String> options);
}
