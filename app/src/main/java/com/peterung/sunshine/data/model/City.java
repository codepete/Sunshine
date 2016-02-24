package com.peterung.sunshine.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by peter on 7/23/15.
 */

public class City implements Parcelable {
    public int id;
    public String name;

    @SerializedName("coord")
    public Coordinate coordinate;
    public String country;
    public double population;

    public City() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeParcelable(this.coordinate, flags);
        dest.writeString(this.country);
        dest.writeDouble(this.population);
    }

    protected City(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.coordinate = in.readParcelable(Coordinate.class.getClassLoader());
        this.country = in.readString();
        this.population = in.readDouble();
    }

    public static final Creator<City> CREATOR = new Creator<City>() {
        public City createFromParcel(Parcel source) {
            return new City(source);
        }

        public City[] newArray(int size) {
            return new City[size];
        }
    };
}
