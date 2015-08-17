package com.dnsoftware.android.fragment;

import android.app.Fragment;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.dnsoftware.android.WeatherApplication;
import com.dnsoftware.android.extras.NetworkUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


public abstract class BaseFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    GoogleApiClient mGoogleApiClient;
    protected synchronized void buildGoogleApiClient() {


        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(WeatherApplication.getAppContext());
        builder.addConnectionCallbacks(this);
        builder.addOnConnectionFailedListener(this);
        builder.addApi(LocationServices.API);
        mGoogleApiClient = builder.build();
    }
    public void showOffline(){
        new  AlertDialog.Builder(getActivity())
                .setTitle("No Internet Connection")
                .setMessage("Check Your Internet Connection")
                .setNegativeButton("exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getActivity().finish();
                    }
                })
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        start();

                    }
                }).show();


    }

    private void start() {
        if (NetworkUtils.isConnectedToInternet(getActivity())) {
            if(mGoogleApiClient==null) {
                buildGoogleApiClient();
            }
            mGoogleApiClient.connect();
        }else {showOffline();}
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        new  AlertDialog.Builder(getActivity())
                .setTitle("Play Services Connection error")
                .setMessage("Couldn't connect to Play Services")
                .setNeutralButton("Cancel",null)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        start();
                    }
                }).show();
    }
}
