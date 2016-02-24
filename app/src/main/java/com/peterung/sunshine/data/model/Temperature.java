package com.peterung.sunshine.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by peter on 7/23/15.
 */
public class Temperature implements Parcelable {
    public float day;
    public float min;
    public float max;
    public float night;
    public float eve;
    public float morn;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.day);
        dest.writeFloat(this.min);
        dest.writeFloat(this.max);
        dest.writeFloat(this.night);
        dest.writeFloat(this.eve);
        dest.writeFloat(this.morn);
    }

    public Temperature() {
    }

    protected Temperature(Parcel in) {
        this.day = in.readFloat();
        this.min = in.readFloat();
        this.max = in.readFloat();
        this.night = in.readFloat();
        this.eve = in.readFloat();
        this.morn = in.readFloat();
    }

    public static final Parcelable.Creator<Temperature> CREATOR = new Parcelable.Creator<Temperature>() {
        public Temperature createFromParcel(Parcel source) {
            return new Temperature(source);
        }

        public Temperature[] newArray(int size) {
            return new Temperature[size];
        }
    };
}
