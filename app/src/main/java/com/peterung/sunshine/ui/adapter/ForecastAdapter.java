package com.peterung.sunshine.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.peterung.sunshine.R;
import com.peterung.sunshine.data.model.Forecast;

import java.util.Collection;
import java.util.List;

/**
 * Created by peter on 7/24/15.
 */
public class ForecastAdapter extends ArrayAdapter<Forecast> {


    private LayoutInflater mInflater;
    private int mLayout;

    public ForecastAdapter(Context context, int resource) {
        super(context, resource);

        mInflater = LayoutInflater.from(context);
        mLayout = resource;
    }

    static class ViewHolder {
        public TextView textView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = mInflater.inflate(mLayout, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.textView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            view.setTag(holder);
        }

        Forecast item = getItem(position);
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.textView.setText();



        return view;
    }
}
