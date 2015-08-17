package com.dnsoftware.android;

import android.app.Application;
import android.content.Context;
import android.location.Location;


public class WeatherApplication extends Application {
    public static final String API_KEY = "0f029a50f9297f980d75c2a242de47a2";
    private static WeatherApplication sInstance;
    private Location location;
    private String timeZone;
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }
    public static WeatherApplication getInstance(){
        return sInstance;
    }
    public static Context getAppContext(){
        return  sInstance.getApplicationContext();
    }
    public void setLocation(Location location){
        this.location = location;
    }
    public   Location getLocation(){
        return location;
    }


    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    public String getTimeZone(){
        return timeZone;
    }
}
