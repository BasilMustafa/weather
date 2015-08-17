package com.dnsoftware.android.model;

/**
 * Created by Basil on 8/6/2015.
 */
public class OneDayForecast {
    private String day;
    private double temperature;
    private String weatherCondition;
    private String iconURL;
    private String weatherDayDescrition;

    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }

    public OneDayForecast(){

    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }
    public void buildDescrition(){
        weatherDayDescrition = weatherCondition.concat(" on ").concat(day);

    }

    public String getWeatherDayDescrition() {
        return weatherDayDescrition;
    }
}
