package com.peterung.sunshine.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by peter on 7/23/15.
 */
public class Forecast implements Parcelable {
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.dt);
        dest.writeParcelable(this.temperature, 0);
        dest.writeFloat(this.pressure);
        dest.writeInt(this.humidity);
        dest.writeTypedArray(this.weathers, flags);
        dest.writeFloat(this.speed);
        dest.writeInt(this.deg);
    }

    public Forecast() {
    }

    protected Forecast(Parcel in) {
        this.dt = in.readLong();
        this.temperature = in.readParcelable(Temperature.class.getClassLoader());
        this.pressure = in.readFloat();
        this.humidity = in.readInt();
        this.weathers = in.createTypedArray(Weather.CREATOR);
        this.speed = in.readFloat();
        this.deg = in.readInt();
    }

    public static final Parcelable.Creator<Forecast> CREATOR = new Parcelable.Creator<Forecast>() {
        public Forecast createFromParcel(Parcel source) {
            return new Forecast(source);
        }

        public Forecast[] newArray(int size) {
            return new Forecast[size];
        }
    };

}

