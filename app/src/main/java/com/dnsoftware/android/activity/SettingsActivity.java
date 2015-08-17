package com.dnsoftware.android.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.dnsoftware.android.R;
import com.dnsoftware.android.WeatherApplication;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getFragmentManager().beginTransaction().replace(R.id.pref_content,new SettingsFragment()).commit();
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar_innr);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
             SharedPreferences sharedPref = getPreferenceManager().getDefaultSharedPreferences(WeatherApplication.getAppContext());


                addPreferencesFromResource(R.xml.preferences);
                onSharedPreferenceChanged(sharedPref,"lengthUnits");
                onSharedPreferenceChanged(sharedPref,"temperatureUnits");

            getPreferenceManager().getDefaultSharedPreferences(WeatherApplication.getAppContext()).registerOnSharedPreferenceChangeListener(this);


        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            if (pref instanceof ListPreference){
                pref.setSummary(((ListPreference)pref).getEntry());
            }

        }
    }



}
