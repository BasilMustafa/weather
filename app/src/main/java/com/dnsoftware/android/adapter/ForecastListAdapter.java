package com.dnsoftware.android.adapter;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dnsoftware.android.R;
import com.dnsoftware.android.VolleySingleton;
import com.dnsoftware.android.WeatherApplication;
import com.dnsoftware.android.client.parser.WeatherParser;
import com.dnsoftware.android.fragment.ForecastFragment;
import com.dnsoftware.android.model.OneDayForecast;
import com.dnsoftware.android.model.WeatherData;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class ForecastListAdapter extends RecyclerView.Adapter<ForecastListAdapter.ForcastViewHolder> implements WeatherParser.OnParsingCompletedListener {

    LayoutInflater layoutInflater;
    private static final String ICON_BASE_URL = "http://openweathermap.org/img/w/";
    private ArrayList<OneDayForecast> forcastList;
    private VolleySingleton volleySingleton;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    private Location location;

    public ForecastListAdapter(Context context) {
        location = WeatherApplication.getInstance().getLocation();
        layoutInflater = LayoutInflater.from(context);
        forcastList = new ArrayList<>();
        volleySingleton = VolleySingleton.getInstance();
        imageLoader = volleySingleton.getImageLoader();
        requestQueue = volleySingleton.getRequestQueue();

    }



    @Override
    public ForcastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.forcast_list_item, parent, false);
        ForcastViewHolder viewHolder = new ForcastViewHolder(view);
        return viewHolder;


    }

    @Override
    public void onBindViewHolder(final ForcastViewHolder holder, int position) {
        OneDayForecast currentDayForecast = forcastList.get(position);
        holder.temperatureTextView.setText(Long.toString(Math.round(currentDayForecast.getTemperature())));
        holder.weatherCondtionTextView.setText(currentDayForecast.getWeatherDayDescrition());
        String iconURL = ICON_BASE_URL + currentDayForecast.getIconURL() + ".png";

        imageLoader.get(iconURL, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

                holder.weatherImageView.setImageBitmap(response.getBitmap());

            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return forcastList.size();
    }


    @Override
    public void onParsingCompleted(WeatherData weatherData) {
        setForcastList(weatherData.getForecastList());
    }



    public void setForcastList(ArrayList<OneDayForecast> forcastList) {

        this.forcastList = forcastList;
        notifyItemRangeChanged(0, forcastList.size());
    }


    private void sendJsonRequest() {
        String forcastUrl = buildDailyForecastUrl();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, forcastUrl, (String) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                WeatherParser weatherParser = new WeatherParser(ForecastListAdapter.this);
                weatherParser.parse(response);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(WeatherApplication.getAppContext(), error.toString(), Toast.LENGTH_LONG).show();

            }
        });
        request.setTag(ForecastFragment.TAG);
        requestQueue.add(request);
    }


    private String buildDailyForecastUrl() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("api.openweathermap.org")
                .appendPath("data/2.5/forecast/daily")
                .appendQueryParameter("lat", Double.toString(location.getLatitude()))
                .appendQueryParameter("lon", Double.toString(location.getLongitude()))
                .appendQueryParameter("cnt", "16");
        if (PreferenceManager.getDefaultSharedPreferences(WeatherApplication.getAppContext()).getString("temperatureUnits", "Celsius").equals("Celsius")) {
            builder.appendQueryParameter("units", "metric");
        }
        return builder.build().toString();
    }

    public static class ForcastViewHolder extends RecyclerView.ViewHolder {
        public TextView weatherCondtionTextView;
        public TextView temperatureTextView;
        public CircleImageView weatherImageView;

        public ForcastViewHolder(View itemView) {
            super(itemView);
            weatherCondtionTextView = (TextView) itemView.findViewById(R.id.weather_condition);
            temperatureTextView = (TextView) itemView.findViewById(R.id.temperature);
            weatherImageView = (CircleImageView) itemView.findViewById(R.id.weather_icon);
        }

    }

}
