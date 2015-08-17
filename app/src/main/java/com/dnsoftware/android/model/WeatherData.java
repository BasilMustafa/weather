package com.dnsoftware.android.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Basil on 8/15/2015.
 */
public class WeatherData {
    private ArrayList<OneDayForecast> forecastList;
    private String cityName;
    private String timeZone;
    private LatLng location;


    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public ArrayList<OneDayForecast> getForecastList() {
        return forecastList;
    }

    public void setForecastList(ArrayList<OneDayForecast> forecastList) {
        this.forecastList = forecastList;
    }
}
