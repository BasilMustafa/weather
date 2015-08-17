package com.dnsoftware.android.extras;

import com.dnsoftware.android.fragment.ForecastFragment;
import com.dnsoftware.android.fragment.TodayWeatherFragment;

/**
 * Created by Basil on 8/15/2015.
 */
public class FragmentsHolder {

    private static ForecastFragment mForecastFragment;
    private static TodayWeatherFragment mTodayWeatherFragment;

    public static ForecastFragment getmForecastFragment() {
        return mForecastFragment;
    }

    public static void setmForecastFragment(ForecastFragment forecastFragment) {
        mForecastFragment = forecastFragment;
    }

    public static TodayWeatherFragment getmTodayWeatherFragment() {
        return mTodayWeatherFragment;
    }

    public static void setmTodayWeatherFragment(TodayWeatherFragment todayWeatherFragment) {
        mTodayWeatherFragment = todayWeatherFragment;
    }
}
