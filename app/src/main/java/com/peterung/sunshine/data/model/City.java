package com.peterung.sunshine.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by peter on 7/23/15.
 */

public class City {
    public int id;
    public String name;

    @SerializedName("coord")
    public Coordinate coordinate;
    public String country;
    public double population;
}
