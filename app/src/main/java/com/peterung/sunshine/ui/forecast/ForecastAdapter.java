package com.peterung.sunshine.ui.forecast;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.peterung.sunshine.R;
import com.peterung.sunshine.utils.WeatherDataUtility;

public class ForecastAdapter extends CursorAdapter {

    private boolean mUseTodayLayout;
    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE = 1;
    private Context mContext;
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layoutType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        if (mUseTodayLayout) {
            switch (layoutType) {
                case VIEW_TYPE_FUTURE:
                    layoutId = R.layout.list_item_forecast;
                    break;
                case VIEW_TYPE_TODAY:
                    layoutId = R.layout.list_item_forecast_today;
                    break;
            }
        } else {
            layoutId = R.layout.list_item_forecast;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int layoutType = getItemViewType(cursor.getPosition());

        if (mUseTodayLayout) {
            switch (layoutType) {
                case VIEW_TYPE_FUTURE:
                    viewHolder.iconView.setImageResource(WeatherDataUtility.getIconResourceForWeatherCondition(weatherId));
                    break;
                case VIEW_TYPE_TODAY:
                    viewHolder.iconView.setImageResource(WeatherDataUtility.getArtResourceForWeatherCondition(weatherId));
                    break;
            }
        } else {
            viewHolder.iconView.setImageResource(WeatherDataUtility.getIconResourceForWeatherCondition(weatherId));
        }


        Long dateInMilis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        viewHolder.dateView.setText(WeatherDataUtility.getFriendlyDayString(view.getContext(), dateInMilis));

        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.descriptionView.setText(description);

        boolean isMetric = WeatherDataUtility.isMetric(view.getContext());
        double highTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.highTempView.setText(WeatherDataUtility.formatTemperature(context, highTemp, isMetric));

        double lowTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(WeatherDataUtility.formatTemperature(context, lowTemp, isMetric));


    }


    public void setUseTodayLayout(boolean value) {
        mUseTodayLayout = value;
    }
}
