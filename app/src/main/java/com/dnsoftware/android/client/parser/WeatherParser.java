package com.dnsoftware.android.client.parser;

import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dnsoftware.android.VolleySingleton;
import com.dnsoftware.android.WeatherApplication;
import com.dnsoftware.android.extras.Keys;
import com.dnsoftware.android.model.OneDayForecast;
import com.dnsoftware.android.model.WeatherData;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.dnsoftware.android.extras.Keys.KEY_DAY_TEMPERATURE;
import static com.dnsoftware.android.extras.Keys.KEY_DAY_WEATHER;
import static com.dnsoftware.android.extras.Keys.KEY_FORCAST_LIST;
import static com.dnsoftware.android.extras.Keys.KEY_TEMPERATURE;
import static com.dnsoftware.android.extras.Keys.KEY_TIME_STAMP;
import static com.dnsoftware.android.extras.Keys.KEY_WEATHER_CONDITION_ICON;
import static com.dnsoftware.android.extras.Keys.KEY_WEATHER_DESCRIPTION;

/**
 * Created by Basil on 8/15/2015.
 */
public class WeatherParser {
    WeatherData weatherData;
    JSONObject response;
    private RequestQueue requestQueue;
    private OnParsingCompletedListener listener;


    public WeatherParser(OnParsingCompletedListener listener){
        this.listener = listener;
        weatherData = new WeatherData();
        requestQueue = VolleySingleton.getInstance().getRequestQueue();



    }
    public WeatherData parse(JSONObject response){
        this.response = response;

        try {

            JSONObject city = response.getJSONObject(Keys.KEY_CITY);
            weatherData.setCityName(city.getString(Keys.KEY_CITY_NAME));
            JSONObject coords = city.getJSONObject(Keys.KEY_COORDINATION);
            double lat = coords.getDouble("lat");
            double lon = coords.getDouble("lon");
            LatLng location = new LatLng(lat,lon);
            weatherData.setLocation(location);
            JSONArray forecastList = response.getJSONArray(Keys.KEY_FORCAST_LIST);
            Long timeStamp = forecastList.getJSONObject(0).getLong(Keys.KEY_TIME_STAMP);
            getTimeZone(location,timeStamp);



        } catch (JSONException e) {
            e.printStackTrace();
        }
        return weatherData;


    }
    private void getTimeZone(LatLng location, final long timeStamp){

        Uri.Builder builer = new Uri.Builder();

        builer.scheme("https")
                .authority("maps.googleapis.com")
                .appendEncodedPath("maps/api/timezone/json")
                .appendQueryParameter("location", Double.toString(location.latitude).concat(",").concat(Double.toString(location.longitude)))
                .appendQueryParameter("timestamp", timeStamp+"");
        String timeZoneFetchingUrl = builer.build().toString();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, timeZoneFetchingUrl, (String) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    String timeZoneId = response.getString("timeZoneId");
                    weatherData.setTimeZone(timeZoneId);
                    WeatherApplication.getInstance().setTimeZone(timeZoneId);
                    constructData(WeatherParser.this.response,timeZoneId);




                } catch (JSONException e) {
                    Toast.makeText(WeatherApplication.getAppContext(), e.toString(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(WeatherApplication.getAppContext(),error.toString(),Toast.LENGTH_LONG).show();

            }
        });
        requestQueue.add(request);


    }
    private void constructData(JSONObject response,String timeZoneId){

        ArrayList<OneDayForecast> forecastList = new ArrayList<>();
        String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
        try {
            JSONArray forcastArray = response.getJSONArray(KEY_FORCAST_LIST);
            // timeStamp = forcastArray.getJSONObject(0).getLong(KEY_TIME_STAMP);




            for (int i=0;i<forcastArray.length();i++) {
                OneDayForecast oneDayForecast  = new OneDayForecast();
                JSONArray weather = forcastArray.getJSONObject(i).getJSONArray(KEY_DAY_WEATHER);
                double temperature = forcastArray.getJSONObject(i).getJSONObject(KEY_TEMPERATURE).getDouble(KEY_DAY_TEMPERATURE);
                String weatherDescription = weather.getJSONObject(0).getString(KEY_WEATHER_DESCRIPTION);
                String iconCode = weather.getJSONObject(0).getString(KEY_WEATHER_CONDITION_ICON);
                long timeStamp = forcastArray.getJSONObject(i).getLong(KEY_TIME_STAMP);
                DateTimeZone timeZone  = DateTimeZone.forID(timeZoneId);
                DateTime dateTime = new DateTime(timeStamp*1000l,timeZone);
                int dayOfWeek = dateTime.getDayOfWeek();
                oneDayForecast.setDay(days[dayOfWeek-1]);
                /*Timestamp timestamp = new Timestamp(timstamptd*1000l);
                Calendar cal = Calendar.getInstance();
                cal.setTime(timestamp);
                oneDayForecast.setDay(days[cal.get(java.util.Calendar.DAY_OF_WEEK)-1]);*/
                oneDayForecast.setIconURL(iconCode);
                oneDayForecast.setTemperature(temperature);
                oneDayForecast.setWeatherCondition(weatherDescription);
                oneDayForecast.buildDescrition();
                forecastList.add(oneDayForecast);
            }
        } catch (JSONException e) {
            Log.d("weather", e.toString());
            Toast.makeText(WeatherApplication.getAppContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }
        weatherData.setForecastList(forecastList);
        listener.onParsingCompleted(weatherData);
    }
    public interface OnParsingCompletedListener{
        void onParsingCompleted(WeatherData weatheData);

    }
}
