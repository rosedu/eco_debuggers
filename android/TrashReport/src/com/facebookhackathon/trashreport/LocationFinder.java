package com.facebookhackathon.trashreport;

import android.app.Activity;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class LocationFinder extends Service implements LocationListener{
	
	private LocationManager locationManager;
	private Context context;
	private boolean gpsEnabled;
	private Location lastKnownLocation;
	private final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 metrii
    private final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 minut
    private Thread syncPosition; 
    private GoogleMap map;
	
	public LocationFinder(Context context, GoogleMap map){
		this.context = context;
		this.map = map;
		locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		this.getLocation();
		setSyncThread();
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
	
	private void setSyncThread(){
		syncPosition = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (getLocation() == null){
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				((Activity) context).runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						LatLng myPosition = new LatLng(lastKnownLocation.getLatitude(),	lastKnownLocation.getLongitude());
						map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 15));
					}
				});
			}
		});
	}
	
	public Thread getSyncPosition() {
		return syncPosition;
	}
	
}
