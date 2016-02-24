package com.peterung.sunshine.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by peter on 7/23/15.
 */
public class Coordinate implements Parcelable {
    public double lat;
    public double lon;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lon);
    }

    public Coordinate() {
    }

    protected Coordinate(Parcel in) {
        this.lat = in.readDouble();
        this.lon = in.readDouble();
    }

    public static final Parcelable.Creator<Coordinate> CREATOR = new Parcelable.Creator<Coordinate>() {
        public Coordinate createFromParcel(Parcel source) {
            return new Coordinate(source);
        }

        public Coordinate[] newArray(int size) {
            return new Coordinate[size];
        }
    };
}
