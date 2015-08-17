package com.dnsoftware.android.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.dnsoftware.android.R;
import com.dnsoftware.android.VolleySingleton;
import com.dnsoftware.android.WeatherApplication;
import com.dnsoftware.android.extras.FragmentsHolder;
import com.dnsoftware.android.extras.NetworkUtils;
import com.dnsoftware.android.fragment.ForecastFragment;
import com.dnsoftware.android.fragment.TodayWeatherFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {


    private DrawerLayout mDrawerLayout;
    private GoogleApiClient mGoogleApiClient;
    private int currentFragmentId;
    private Location location;
    private RequestQueue requestQueue;
    private ForecastFragment forecastFragment;
    private TodayWeatherFragment todayWeatherFragment;
    NavigationView navigationView;
    private int navId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

            forecastFragment = FragmentsHolder.getmForecastFragment();
            todayWeatherFragment = FragmentsHolder.getmTodayWeatherFragment();


        String units = preferences.getString("lengthUnits",null);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Weather");
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();


        requestQueue = VolleySingleton.getInstance().getRequestQueue();
        navId = R.id.nav_forcast;
        if (savedInstanceState!=null){
            navId= savedInstanceState.getInt("currentFragmentId");
            currentFragmentId = navId;
            Menu menu = navigationView.getMenu();
            MenuItem item = menu.findItem(navId);
            item.setChecked(true);
        }else {
            start();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId()==R.id.action_settings){
            startActivity(new Intent(this,SettingsActivity.class));
        }else if (item.getItemId()==R.id.action_about){
            showAbout();

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConnected(Bundle bundle) {

        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location!=null) {
            WeatherApplication.getInstance().setLocation(location);
            navigateTo(navId);
        }
        else {
            Toast.makeText(this,"Couldn't Retrieve Location",Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Snackbar.make(findViewById(R.id.main),"No Connection",Snackbar.LENGTH_LONG).show();

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }




    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        int id= menuItem.getItemId();
                        navigateTo(id);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });

    }

    private void navigateTo(int fragmentID){
        if (fragmentID==currentFragmentId){
            return;
        }
        navigationView.getMenu().findItem(fragmentID).setChecked(true);
        if (fragmentID==R.id.nav_forcast){
            if (forecastFragment==null){
                forecastFragment = new ForecastFragment();
                FragmentsHolder.setmForecastFragment(forecastFragment);

            }
            getFragmentManager().beginTransaction().replace(R.id.forcast_content,forecastFragment).commit();

        }else if (fragmentID==R.id.nav_Today){
            if (todayWeatherFragment ==null){
                todayWeatherFragment = new TodayWeatherFragment();
                FragmentsHolder.setmTodayWeatherFragment(todayWeatherFragment);
            }
            getFragmentManager().beginTransaction().replace(R.id.forcast_content, todayWeatherFragment).commit();
        }
        currentFragmentId = fragmentID;

    }
    private void showAbout(){
        new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage("Weather App made by Basil Mustafa")
                .setNeutralButton("OK",null)
                .show();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentFragmentId",currentFragmentId);


    }

    private void showOffline(){
        new  AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Check Your Internet Connection")
                .setNegativeButton("exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        start();
                    }
                }).show();


    }
    private void start(){
        if(NetworkUtils.isConnectedToInternet(this)){
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }else {
            showOffline();
        }

    }


}
