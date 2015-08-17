package com.dnsoftware.android.fragment;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
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
import com.android.volley.toolbox.NetworkImageView;
import com.dnsoftware.android.R;
import com.dnsoftware.android.VolleySingleton;
import com.dnsoftware.android.WeatherApplication;
import com.dnsoftware.android.extras.Keys;
import com.dnsoftware.android.extras.NetworkUtils;
import com.google.android.gms.location.LocationServices;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;

import de.hdodenhof.circleimageview.CircleImageView;


public class TodayWeatherFragment extends BaseFragment {

    private static final String COMMA = ",";
    private static final String TAG = "Today";
    private static final String ICON_BASE_URL = "http://openweathermap.org/img/w/";
    private VolleySingleton volleySingleton;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    private static final String URL= "http://api.openweathermap.org/data/2.5/forecast/daily?q=Prague&units=metric&cnt=7";
    private static final String FLICKR_BASE_URL = "api.flickr.com";
    private TextView cityNameTextView;
    private TextView weatherConditionTextView;
    private TextView temparetureTextView;
    private TextView mHumidity;
    private TextView mPercitipation;
    private TextView mPressure;
    private TextView mWindSpeed;
    private TextView mWindDirection;
    private com.android.volley.toolbox.NetworkImageView headerImageView;
    private CircleImageView weatherIcon;
    private Location mUserLocation;
    private String photoTags;
    private String cityName;
    private boolean isCreated;
    private String mContextTags;
    View view;
    public static TodayWeatherFragment newInstance(String param1, String param2) {
        TodayWeatherFragment fragment = new TodayWeatherFragment();

        return fragment;
    }

    public TodayWeatherFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photoTags = "";
        volleySingleton = VolleySingleton.getInstance();
        requestQueue = volleySingleton.getRequestQueue();
        imageLoader = volleySingleton.getImageLoader();
        setRetainInstance(true);




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

            if (view ==null) {
                view = inflater.inflate(R.layout.fragment_today_weather, container, false);
                temparetureTextView = (TextView) view.findViewById(R.id.today_temperature);
                weatherConditionTextView = (TextView) view.findViewById(R.id.today_weather_condition);
                cityNameTextView = (TextView) view.findViewById(R.id.today_city_name);
                headerImageView = (NetworkImageView) view.findViewById(R.id.today_header_image);
                weatherIcon = (CircleImageView) view.findViewById(R.id.today_weather_icon);
                mHumidity = (TextView) view.findViewById(R.id.humidity);
                mPercitipation = (TextView) view.findViewById(R.id.percipitation);
                mPressure = (TextView) view.findViewById(R.id.pressure);
                mWindSpeed = (TextView) view.findViewById(R.id.wind);
                mWindDirection = (TextView) view.findViewById(R.id.direction);
            }
        if (NetworkUtils.isConnectedToInternet(getActivity())) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();

        }else {
            isCreated=false;
            showOffline();
        }



        return view;
    }




    @Override
    public void onDetach() {
        super.onDetach();
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar!=null){
            actionBar.setTitle("Today");
        }
    }











    private void sendJsonRequest(Location location){
        String Url = buildWeatherURL(location);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Url, (String) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseJSONResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(WeatherApplication.getAppContext(), error.toString()+"JSPN", Toast.LENGTH_LONG).show();

            }
        });
        request.setTag(TAG);
        requestQueue.add(request);
    }
    private void parseJSONResponse(JSONObject response){

        if(response==null || response.length()==0){
            Toast.makeText(WeatherApplication.getAppContext(), "No response", Toast.LENGTH_SHORT).show();
            return;

        }
        try {

            if(response.has(Keys.KEY_TODAY_MAIN)) {
                JSONObject mainWeather = response.getJSONObject(Keys.KEY_TODAY_MAIN);
                int pressure = 0;
                if (mainWeather.has(Keys.KEY_PRESSURE)){
                    pressure = mainWeather.getInt(Keys.KEY_PRESSURE);

                }
                if (pressure!=0){
                    mPressure.setText(Integer.toString(pressure)+"Pha");
                } else{
                    mPressure.setText("N/A");
                }
                int humidity = -1;
                if (mainWeather.has(Keys.KEY_HUMIDITY)) {

                    humidity = mainWeather.getInt(Keys.KEY_HUMIDITY);

                }
                if (humidity!=-1){
                    mHumidity.setText(Integer.toString(humidity)+"%");
                }else {
                    mHumidity.setText("N/A");
                }
            }
            if (response.has(Keys.KEY_WIND)){
                String windDirection = "NA";
                JSONObject wind = response.getJSONObject(Keys.KEY_WIND);
                if (wind.has(Keys.KEY_DIRECTION)){
                    double degree = wind.getDouble(Keys.KEY_DIRECTION);
                    windDirection = degreetoDirection(degree);

                }
                mWindDirection.setText(windDirection);
                double windSpeed = -1d;
                String windSpeedUnit = "Km/h";
                if (wind.has(Keys.KEY_SPEED)){
                    windSpeed = wind.getDouble(Keys.KEY_SPEED);
                    if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("lengthUnits","Meter").equals("Mile")){
                        windSpeed = windSpeed*0.621371;//convert to Mph
                        windSpeedUnit = "MpH";
                    }


                }
                if (windSpeed!=-1){
                    mWindSpeed.setText(Double.toString(windSpeed)+windSpeedUnit);
                }else{
                    mWindSpeed.setText("N/A");
                }
            }


            if (response.has(Keys.KEY_DAY_WEATHER)){
                JSONArray weaArray = response.getJSONArray(Keys.KEY_DAY_WEATHER);
                        if (weaArray.length()>0){
                            JSONObject currentWeather = weaArray.getJSONObject(0);
                            String weatherCondition = "Not Available";
                            if (currentWeather.has(Keys.KEY_WEATHER_DESCRIPTION)){
                                weatherCondition = currentWeather.getString(Keys.KEY_WEATHER_DESCRIPTION);
                            }
                            weatherConditionTextView.setText(weatherCondition);

                            if (currentWeather.has(Keys.KEY_WEATHER_CONDITION_ICON)){
                                String iconSymbol = currentWeather.getString(Keys.KEY_WEATHER_CONDITION_ICON);
                                imageLoader.get(ICON_BASE_URL + iconSymbol + ".png", new ImageLoader.ImageListener() {
                                    @Override
                                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                                        weatherIcon.setImageBitmap(response.getBitmap());
                                        weatherIcon.setVisibility(View.VISIBLE);
                                        if (headerImageView.getDrawable()!=null){
                                            isCreated = true;
                                        }

                                    }

                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                    }
                                });
                            }
                        }
            }
            double temperature =-1111d;
            if (response.has(Keys.KEY_TODAY_MAIN)){
                JSONObject mainWeather = response.getJSONObject(Keys.KEY_TODAY_MAIN);
                if (mainWeather.has(Keys.KEY_TEMPERATURE)){
                    temperature = mainWeather.getDouble(Keys.KEY_TEMPERATURE);

                }
                if(temperature!=-1111d){
                    temparetureTextView.setText(Long.toString(Math.round(temperature)) + "ْ");
                }else {
                    temparetureTextView.setText("N/A");
                }
            }
            if (response.has(Keys.KEY_CITY_NAME)){
                cityName = response.getString(Keys.KEY_CITY_NAME);
            }



            double perc = -1;
            if (response.has(Keys.KEY_RAIN)){
                JSONObject rain = response.getJSONObject(Keys.KEY_RAIN);
                if (rain.has(Keys.KEY_THREE_HOURS)){
                    perc = rain.getDouble(Keys.KEY_THREE_HOURS);

                }if (perc!=-1){
                    mPercitipation.setText(Double.toString(perc));
                }else {
                    mPercitipation.setText("N/A");
                }
            }
            fetchAndSetHeaderImage(mUserLocation,mContextTags);


            cityNameTextView.setText(cityName);







        }catch (JSONException e){

        }

    }

    private void fetchAndSetHeaderImage(Location location, String tags){
        Uri.Builder builer = new Uri.Builder();
        builer.scheme("https")
                .authority(FLICKR_BASE_URL)
                .path("services/rest")
                .appendQueryParameter("method", "flickr.photos.search")
                .appendQueryParameter("api_key","51921b1f5deda9318b020cf5fb8813a4")
                .appendQueryParameter("tags", tags)
                .appendQueryParameter("lat", Double.toString(location.getLatitude()))
                .appendQueryParameter("long", Double.toString(location.getLatitude()))
                .appendQueryParameter("accuracy", "11")
                .appendQueryParameter("page", "1")
                .appendQueryParameter("per_page","1")
                .appendQueryParameter("format","json")
                .appendQueryParameter("nojsoncallback","1");
        final String imagefetchingURL = builer.build().toString();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, imagefetchingURL, (String) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                try {
                    JSONObject imageJsonObject = response.getJSONObject("photos").getJSONArray("photo").getJSONObject(0);
                    String farmId = imageJsonObject.getString("farm");
                    String serverId= imageJsonObject.getString("server");
                    String Id = imageJsonObject.getString("id");
                    String secret = imageJsonObject.getString("secret");
                    String imageURL = "http://farm"+farmId+".staticflickr.com/"+serverId
                            +"/"+Id+"_"+secret+".jpg";

                    imageLoader.get(imageURL,ImageLoader.getImageListener(headerImageView,0,0));
                    headerImageView.setImageUrl(imageURL,imageLoader);
                    headerImageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                            if (headerImageView.getDrawable()!=null){
                                if (weatherIcon.getDrawable()!=null){
                                    isCreated = true;
                                }
                            }
                        }
                    });





                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(),e.toString()+"4444",Toast.LENGTH_LONG).show();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(),error.toString()+"tcc",Toast.LENGTH_LONG).show();

            }
        });
        requestQueue.add(request);

    }





    private String getCityName(Location location){

        try {

            Geocoder geocoder =new Geocoder(getActivity(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            String cityName = addresses.get(0).getLocality();
            return  cityName;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;


    }
    private String buildWeatherURL(Location location){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("api.openweathermap.org")
                .path("data/2.5/weather")
                .appendQueryParameter("lat", Double.toString(location.getLatitude()))
                .appendQueryParameter("lon", Double.toString(location.getLongitude()))
                .appendQueryParameter("cnt", "1");
        if (PreferenceManager.getDefaultSharedPreferences(WeatherApplication.getAppContext()).getString("temperatureUnits", "Celsius").equals("Celsius")) {
            builder.appendQueryParameter("units", "metric");
        }
        return builder.build().toString();

    }


    private String getTags(JSONObject response){

        try {
            String weatherDescription = response.getJSONArray(Keys.KEY_DAY_WEATHER).getJSONObject(0).getString(Keys.KEY_TODAY_MAIN);
            Long timeStamp = response.getLong(Keys.KEY_TIME_STAMP);

            DateTimeZone timeZone  = DateTimeZone.forID(WeatherApplication.getInstance().getTimeZone());
            DateTime dateTime = new DateTime(timeStamp*1000l,timeZone);
            int hour = dateTime.getHourOfDay();
            String partOfDay = getPartofDay(hour);
            String tags = weatherDescription+COMMA+partOfDay+COMMA+"outdoors";
            return tags;



        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;



    }
    private String getPartofDay(int hour){

        if(hour >= 0 && hour < 12){
           return  "Morning";
        }else if(hour >= 12 && hour < 16){
            return "Afternoon";
        }else if(hour >= 16 && hour < 21){
            return "Evening";
        }else if(hour >= 21 && hour < 24){
            return "Night";
        }
        return null;
    }

    @Override
    public void onStop() {
        super.onStop();
        requestQueue.cancelAll(TAG);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requestQueue.cancelAll(TAG);

    }
    private String degreetoDirection(double degree){
        int degreeSection = (int)(degree/22.5 + 0.5);
        String[] directions = {"N","NNE","NE","ENE","E","ESE", "SE", "SSE","S","SSW","SW","WSW","W","WNW","NW","NNW"};

        return directions[degreeSection%16];

    }

    @Override
    public void onConnected(Bundle bundle) {
        mUserLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mUserLocation!=null){
            if(!isCreated){
                sendJsonRequest(mUserLocation);
            }
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


}
