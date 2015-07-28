package com.peterung.sunshine.ui;

import android.database.Observable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.peterung.sunshine.R;
import com.peterung.sunshine.data.OpenWeatherMapService;
import com.peterung.sunshine.data.api.OpenWeatherMapApi;
import com.peterung.sunshine.data.model.Forecast;
import com.peterung.sunshine.data.model.ForecastResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    ArrayAdapter<Forecast> mForecastAdapter;

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
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        String[] data = {
            "Mon 6/23 - Sunny - 31/17",
            "Tue 6/24 - Foggy - 21/8",
            "Wed 6/25 - Cloudy - 22/17",
            "Thurs 6/26 - Rainy - 18/11",
            "Fri 6/27 - Foggy - 21/10",
            "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
            "Sun 6/29 - Sunny - 20/7"
        };
        List<String> weekForecast = new ArrayList<>(Arrays.asList(data));

        ListView listView = (ListView) view.findViewById(R.id.listview_forecast);
        mForecastAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast);
        listView.setAdapter(mForecastAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh:
                Map<String, String> query = new HashMap<>();
                query.put("zip", "98052");
                query.put("cnt", "7");
                OpenWeatherMapService.getInstance().getForecast(query)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<ForecastResponse>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("OpenWeatherMapApi", e.getMessage());

                            }

                            @Override
                            public void onNext(ForecastResponse forecastResponse) {
                                Log.i("OpenWeatherMapApi", "test: " + forecastResponse.city);
                                mForecastAdapter.addAll(forecastResponse.forecasts);
                            }
                        });
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
