package com.dnsoftware.android.fragment;

import android.app.Activity;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dnsoftware.android.DividerItemDecoration;
import com.dnsoftware.android.WeatherApplication;
import com.dnsoftware.android.client.parser.WeatherParser;
import com.dnsoftware.android.R;
import com.dnsoftware.android.VolleySingleton;
import com.dnsoftware.android.adapter.ForecastListAdapter;
import com.dnsoftware.android.model.WeatherData;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;



public class ForecastFragment extends BaseFragment implements WeatherParser.OnParsingCompletedListener{

    public static final String TAG = "Forcast list";
    private VolleySingleton volleySingleton;
    private RequestQueue requestQueue;
    private RecyclerView mForecastList;
    private Location location;


    public ForecastFragment() {
        // Required empty public constructor

        buildGoogleApiClient();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        volleySingleton = VolleySingleton.getInstance();
        requestQueue = volleySingleton.getRequestQueue();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mForecastList==null) {
            mForecastList = (RecyclerView) inflater.inflate(R.layout.fragment_forcast, container, false);
            mForecastList.setLayoutManager(new LinearLayoutManager(mForecastList.getContext()));

            mForecastList.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        }

            mGoogleApiClient.connect();
        return mForecastList;

    }



    @Override
    public void onDetach() {
        super.onDetach();
        requestQueue.cancelAll(TAG);
    }





    @Override
    public void onStop() {
        super.onStop();
        requestQueue.cancelAll(TAG);
    }


    @Override
    public void onConnected(Bundle bundle) {
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location!=null) {
            WeatherApplication.getInstance().setLocation(location);
            if (mForecastList.getAdapter()==null) {
                sendJsonRequest();
                return;
            }
        }else {

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }




    private void sendJsonRequest() {
        String forcastUrl = buildDailyForecastUrl();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, forcastUrl, (String) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                WeatherParser weatherParser = new WeatherParser(ForecastFragment.this);
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar!=null){
            actionBar.setTitle("Forecast");
        }
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


    @Override
    public void onParsingCompleted(WeatherData weatheData) {
        ForecastListAdapter forecastListAdapter = new ForecastListAdapter(getActivity());
        forecastListAdapter.setForcastList(weatheData.getForecastList());

        mForecastList.setAdapter(forecastListAdapter);

    }

}
