package com.facebookhackathon.trashreport;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

public class LocationFinder extends Service implements LocationListener{
	
	private LocationManager locationManager;
	private Context context;
	private boolean gpsEnabled;
	private Location lastKnownLocation;
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 metrii
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 minut
	
	public LocationFinder(Context context){
		this.context = context;
		locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		this.getLocation();
	}
	
	private void showSettingsAlert(){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("GPS settings");
		builder.setMessage("GPS is not enabled. Do you want to go to settings menu?");
		builder.setPositiveButton("Settings", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_SETTINGS);
                context.startActivity(intent);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
        });
		builder.create().show();
	}
	
	public Location getLocation(){
		gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		if (!gpsEnabled){
			showSettingsAlert();
			return null;
		}
		else {
			if (lastKnownLocation == null){
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
						MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
			}
			if (locationManager != null){
				lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
		}
		
		return lastKnownLocation;
	}

	public Location getLastKnownLocation() {
		return lastKnownLocation;
	}
	
	@Override
	public void onLocationChanged(Location arg0) {
		lastKnownLocation = arg0;
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
	
}
