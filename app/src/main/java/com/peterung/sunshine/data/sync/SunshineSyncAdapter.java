package com.peterung.sunshine.data.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Log;

import com.peterung.sunshine.R;
import com.peterung.sunshine.data.api.OpenWeatherMapService;
import com.peterung.sunshine.data.model.City;
import com.peterung.sunshine.data.model.Forecast;
import com.peterung.sunshine.data.model.ForecastResponse;
import com.peterung.sunshine.data.model.Temperature;
import com.peterung.sunshine.data.model.Weather;
import com.peterung.sunshine.data.provider.WeatherContract;
import com.peterung.sunshine.ui.MainActivity;
import com.peterung.sunshine.utils.WeatherDataUtility;
import com.peterung.sunshine.utils.WeatherDbUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {

    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;


    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[] {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };

    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;


    public SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d("PLEASERUN", "onPerformSync Called.");

        String locationQuery = WeatherDataUtility.getPreferredLocation(getContext());

        Map<String, String> query = new HashMap<>();
        query.put("zip", locationQuery);
        query.put("cnt", "14");
        ForecastResponse forecastResponse = OpenWeatherMapService.getInstance().getForecast(query);
        addForecasts(locationQuery, forecastResponse);

    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
            Log.d("SUNSHINESYNC", "create new");
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    private void notifyWeather() {
        Context context = getContext();
        //checking the last update and notify if it's the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        long lastNotification = prefs.getLong(lastNotificationKey, 0);

        String showNotificationKey = context.getString(R.string.pref_enable_notifications_key);
        boolean showNotification = prefs.getBoolean(showNotificationKey, false);

        if (showNotification && System.currentTimeMillis() - lastNotification >= DAY_IN_MILLIS) {
            // Last notification was more than 1 day ago, let's send a notification with the weather.
            String locationQuery = WeatherDataUtility.getPreferredLocation(context);

            Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

            // we'll query our contentProvider, as always
            Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                double high = cursor.getDouble(INDEX_MAX_TEMP);
                double low = cursor.getDouble(INDEX_MIN_TEMP);
                String desc = cursor.getString(INDEX_SHORT_DESC);

                int iconId = WeatherDataUtility.getIconResourceForWeatherCondition(weatherId);
                Resources resources = context.getResources();
                Bitmap largeIcon = BitmapFactory.decodeResource(resources, WeatherDataUtility.getArtResourceForWeatherCondition(weatherId));
                String title = context.getString(R.string.app_name);

                // Read high temperature from cursor and update view
                boolean isMetric = WeatherDataUtility.isMetric(context);

                // Define the text of the forecast.
                String contentText = String.format(context.getString(R.string.format_notification),
                        desc,
                        WeatherDataUtility.formatTemperature(context, high, isMetric),
                        WeatherDataUtility.formatTemperature(context, low, isMetric));


                // NotificationCompatBuilder is a very convenient way to build backward-compatible
                // notifications.  Just throw in some data.
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getContext())
                                .setColor(resources.getColor(R.color.sunshine_light_blue))
                                .setSmallIcon(iconId)
                                .setLargeIcon(largeIcon)
                                .setContentTitle(title)
                                .setContentText(contentText);

                // Make something interesting happen when the user clicks on the notification.
                // In this case, opening the app is sufficient.
                Intent resultIntent = new Intent(context, MainActivity.class);

                // The stack builder object will contain an artificial back stack for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);

                NotificationManager mNotificationManager =
                        (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
                mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());


                //refreshing last notification
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.commit();
            }
        }

    }

    private long addLocation(String locationSetting, City city) {
        String selection = WeatherContract.LocationEntry.COLUMN_CITY_NAME + "= ?";
        String[] selectionArgs = new String[] {city.name};
        Cursor c = getContext().getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI, null, selection, selectionArgs, null);

        if (c == null) { return -1; }

        long locationId;
        if (c.moveToFirst()) {
            int colIdx = c.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = c.getLong(colIdx);
        } else {
            ContentValues cv = new ContentValues();
            cv.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            cv.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, city.name);
            cv.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, city.coordinate.lat);
            cv.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, city.coordinate.lon);

            Uri uri = getContext().getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, cv);

            locationId = ContentUris.parseId(uri);
        }

        c.close();
        return locationId;
    }


    public int addForecasts(String locationSetting, ForecastResponse forecastResponse) {
        ArrayList<ContentValues> cVVector = new ArrayList<>();
        long dateTime;
        int i = 0;
        long locationId = addLocation(locationSetting, forecastResponse.city);
        // we start at the day returned by local time. Otherwise this is a mess.
        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        for (Forecast forecast: forecastResponse.forecasts) {
            ContentValues weatherValues = new ContentValues();

            Temperature temp = forecast.temperature;
            Weather weather = forecast.weathers[0];

            dateTime = dayTime.setJulianDay(julianStartDay+i);

            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, forecast.humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, forecast.pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, forecast.speed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, forecast.deg);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, temp.max);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, temp.min);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, weather.main);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weather.id);

            cVVector.add(weatherValues);
            i++;
        }

        if (cVVector.size() > 0) {
            ContentValues[] values = new ContentValues[cVVector.size()];
            cVVector.toArray(values);

            notifyWeather();

            // Cleanup old forecasts
            getContext().getContentResolver().delete(
                    WeatherContract.WeatherEntry.CONTENT_URI,
                    WeatherContract.WeatherEntry.COLUMN_DATE +  "=< ?",
                    new String[] {Long.toString(dayTime.setJulianDay(julianStartDay-1))}
            );

            return getContext().getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, values);
        }
        return 0;
    }
}