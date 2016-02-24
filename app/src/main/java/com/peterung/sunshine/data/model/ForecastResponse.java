package com.peterung.sunshine.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by peter on 7/23/15.
 */
public class ForecastResponse implements Parcelable {
    public City city;
    public String cod;
    public float message;

    @SerializedName("cnt")
    public int count;

    @SerializedName("list")
    public Forecast[] forecasts;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.city, 0);
        dest.writeString(this.cod);
        dest.writeFloat(this.message);
        dest.writeInt(this.count);
        dest.writeParcelableArray(this.forecasts, 0);
    }

    public ForecastResponse() {
    }

    protected ForecastResponse(Parcel in) {
        this.city = in.readParcelable(City.class.getClassLoader());
        this.cod = in.readString();
        this.message = in.readFloat();
        this.count = in.readInt();
        this.forecasts = (Forecast[]) in.readParcelableArray(Forecast.class.getClassLoader());
    }

    public static final Parcelable.Creator<ForecastResponse> CREATOR = new Parcelable.Creator<ForecastResponse>() {
        public ForecastResponse createFromParcel(Parcel source) {
            return new ForecastResponse(source);
        }

        public ForecastResponse[] newArray(int size) {
            return new ForecastResponse[size];
        }
    };
}
