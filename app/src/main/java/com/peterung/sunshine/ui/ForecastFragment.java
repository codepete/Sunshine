package com.peterung.sunshine.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.peterung.sunshine.R;
import com.peterung.sunshine.data.OpenWeatherMapService;
import com.peterung.sunshine.data.model.ForecastResponse;
import com.peterung.sunshine.ui.adapter.ForecastAdapter;

import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    ForecastAdapter mForecastAdapter;
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

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

        ListView listView = (ListView) view.findViewById(R.id.listview_forecast);
        mForecastAdapter = new ForecastAdapter(getActivity(), R.layout.list_item_forecast);
        listView.setAdapter(mForecastAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        getWeatherData();
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
            case R.id.action_refresh:
                getWeatherData();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getWeatherData() {
        Map<String, String> query = new HashMap<>();
        query.put("zip", "98052");
        query.put("cnt", "7");
        Subscription subscription = OpenWeatherMapService.getInstance().getForecast(query)
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

        mCompositeSubscription.add(subscription);
    }
}
