package com.peterung.sunshine.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.peterung.sunshine.R;
import com.peterung.sunshine.data.model.Forecast;
import com.peterung.sunshine.data.sync.SunshineSyncAdapter;
import com.peterung.sunshine.utils.WeatherDataUtility;


public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {

    private String mLocation;
    private final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocation = WeatherDataUtility.getPreferredLocation(this);

        if (findViewById(R.id.weather_detail_fragment) != null) {

            mTwoPane = true;

            if (savedInstanceState == null) {
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.weather_detail_fragment, new DetailFragment(), DETAILFRAGMENT_TAG);
                transaction.commit();
            }
        } else {
            mTwoPane = false;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setElevation(0F);
            }
        }


        ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.forecast_fragment);
        if (forecastFragment != null) {
            forecastFragment.setUseTodayLayout(!mTwoPane);
        }

        SunshineSyncAdapter.initializeSyncAdapter(this);
    }


    @Override
    protected void onResume() {
        String currentLocation = WeatherDataUtility.getPreferredLocation(this);
        if (!currentLocation.equals(mLocation)) {
            ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.forecast_fragment);
            if (forecastFragment != null) {
                forecastFragment.onLocationChanged();
            }

            DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (detailFragment != null) {
                detailFragment.onLocationChanged(currentLocation);
            }
            mLocation = currentLocation;
        }

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }




    @Override
    public void onItemSelected(Uri dateUri) {

        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, dateUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.weather_detail_fragment, fragment, DETAILFRAGMENT_TAG)
                    .commit();

        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailFragment.DETAIL_URI, dateUri);
            startActivity(intent);
        }
    }
}
