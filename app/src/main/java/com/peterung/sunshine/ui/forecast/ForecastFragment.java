package com.peterung.sunshine.ui.forecast;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.peterung.sunshine.R;
import com.peterung.sunshine.data.provider.WeatherContract;
import com.peterung.sunshine.data.sync.SunshineSyncAdapter;
import com.peterung.sunshine.utils.WeatherDataUtility;

import java.util.Locale;

import rx.subscriptions.CompositeSubscription;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int FORECAST_LOADER = 0;
    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;
    public static final int COL_COORD_LAT = 7;
    public static final int COL_COORD_LONG = 8;

    private boolean mUseTodayLayout;
    private ListView mListView;
    private ForecastAdapter mForecastAdapter;
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private Callback mCallback;
    private int mPosition;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt("pos", 0);
        }

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mListView = (ListView) view.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = mForecastAdapter.getCursor();
                if (mCallback != null && cursor != null) {
                    String locationSetting = WeatherDataUtility.getPreferredLocation(getContext());
                    Uri dateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
                    mCallback.onItemSelected(dateUri);
                }

                mPosition = position;
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (Callback) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException(context.toString() + " must implement Callback");
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_map:
                openPreferredLocationInMap();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


    private void openPreferredLocationInMap() {
        Uri gmmIntentUri;
        if (mForecastAdapter != null) {
            Cursor c = mForecastAdapter.getCursor();
            long lat = c.getLong(COL_COORD_LAT);
            long lon = c.getLong(COL_COORD_LONG);

            gmmIntentUri = Uri.parse(String.format(Locale.US, "geo:%d,%d", lat, lon));

        } else {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String preferredLocation = sharedPreferences.getString("zipcode", getString(R.string.pref_zipcode_default));

            gmmIntentUri = Uri.parse("geo:0,0").buildUpon()
                    .appendQueryParameter("q", preferredLocation)
                    .build();

        }

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        if (mapIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(getContext(), "Could not find map application to perform action", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mForecastAdapter != null && mForecastAdapter.getCursor() != null) {
            outState.putInt("pos", mForecastAdapter.getCursor().getPosition());
        }

        super.onSaveInstanceState(outState);
    }


    private void getWeatherData() {
//        Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
//        alarmIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, WeatherDataUtility.getPreferredLocation(getActivity()));
//
//        //Wrap in a pending intent which only fires once.
//        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);//getBroadcast(context, 0, i, 0);
//
//        AlarmManager am=(AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
//
//        //Set the AlarmManager to wake up the system.
//        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);

        SunshineSyncAdapter.syncImmediately(getContext());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case FORECAST_LOADER:
                String locationSetting = WeatherDataUtility.getPreferredLocation(getActivity());

                // Sort order:  Ascending, by date.
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                        locationSetting, System.currentTimeMillis());

                return new CursorLoader(
                        getActivity(),
                        weatherForLocationUri,
                        FORECAST_COLUMNS,
                        null,
                        null,
                        sortOrder
                );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);

        if (mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    public void onLocationChanged() {
        getWeatherData();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }


    public interface Callback {
        public void onItemSelected(Uri dateUri);
    }

    public void setUseTodayLayout(boolean value) {
        mUseTodayLayout = value;
        if (mForecastAdapter == null) {
            return;
        }

        mForecastAdapter.setUseTodayLayout(value);
    }
}
