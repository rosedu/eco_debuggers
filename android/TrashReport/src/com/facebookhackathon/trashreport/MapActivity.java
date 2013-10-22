package com.facebookhackathon.trashreport;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.facebookhackathon.alertdialogs.ReportAlertDialog;
import com.facebookhackathon.alertdialogs.UpdateAlertDialog;
import com.facebookhackathon.gps.LocationFinder;
import com.facebookhackathon.gps.MyLocation;
import com.facebookhackathon.mapobjects.CameraView;
import com.facebookhackathon.mapobjects.Markers;
import com.facebookhackathon.serverrequests.DownloadImage;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

public class MapActivity extends Activity {
	private GoogleMap map;
	private Context context;
	private Button reportButton;
	private Bitmap bitmap;
	private ReportAlertDialog reportAllertDialog;
	private UpdateAlertDialog updateAlertDialog;
	private LocationFinder locationFinder;
	private final LatLng ROMANIA = new LatLng(46, 25);
	private CameraView cameraView;
	private String request;
	
	//Intent starting and Result getting constants
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private final static int BROWSE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_map);
		
		//save the context
		context = this;
		
		//set map
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
		        .getMap();
		map.getUiSettings().setZoomControlsEnabled(false);
		map.getUiSettings().setMyLocationButtonEnabled(true);
		map.setMyLocationEnabled(true);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(ROMANIA, 5));

		//activate the location finder
		locationFinder = new LocationFinder(context, map);
		locationFinder.getSyncPosition().start();

		//cameraView
		cameraView = new CameraView(context, map);
		cameraView.getCamera().start();
		
		//cameraView must be set before you call setCameraListener()
		setCameraListener();
		setClickListenerOnMarker();
		
		//report button
		reportButton = (Button) findViewById(R.id.button_report);
		reportButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if ( locationFinder.getLocation() != null) {
					MyLocation.myLat = locationFinder.getLastKnownLocation().getLatitude();
					MyLocation.myLong = locationFinder.getLastKnownLocation().getLongitude();
					//build the report dialog
					reportAllertDialog = new ReportAlertDialog(context, false);
					AlertDialog alertDialog = reportAllertDialog.createDialog();
					request = "report";
				}
				else {
					Toast.makeText(context, "Your location is not set. Please wait", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	
	private void setCameraListener(){
		map.setOnCameraChangeListener(new OnCameraChangeListener() {
			
			@Override
			public void onCameraChange(CameraPosition position) {
				LatLngBounds curScreen = map.getProjection().getVisibleRegion().latLngBounds;
				cameraView.setLat_ne(curScreen.northeast.latitude);
				cameraView.setLat_sw(curScreen.southwest.latitude);
				cameraView.setLong_ne(curScreen.northeast.longitude);
				cameraView.setLong_sw(curScreen.southwest.longitude);
				cameraView.setDataWasChanged(true);
			}
		});
	}
	
	private void setClickListenerOnMarker(){
		map.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				for (int i=0; i<Markers.markerArray.size(); i++){
					if ( marker.hashCode() == Markers.markerArray.get(i).getMarker().hashCode()){
						DownloadImage downloadImg = new DownloadImage(context, Markers.markerArray.get(i).getImageId());
						downloadImg.getDownloader().start();
						while ( downloadImg.getBitmap() == null){}
						updateAlertDialog = new UpdateAlertDialog(context, true, Markers.markerArray.get(i).getImageId());
						updateAlertDialog.setBitmap(downloadImg.getBitmap());
						AlertDialog alertDialog = updateAlertDialog.createDialog();
						request = "update";
					}
				}
				return false;
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		cameraView.setRunning(false);
		try {
			cameraView.getCamera().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				if (data != null){
					// Image captured and saved to fileUri specified in the Intent
					if ( request.contentEquals("report")){
						bitmap = (Bitmap) data.getExtras().get("data");
						reportAllertDialog.setBitmap(bitmap);
						reportAllertDialog.setWithPicture(true);
						AlertDialog alertDialog = reportAllertDialog.createDialog();
					}
					else {
						bitmap = (Bitmap) data.getExtras().get("data");
						updateAlertDialog.setBitmap(bitmap);
						updateAlertDialog.setTheImageIsChanged(true);
						AlertDialog alertDialog = updateAlertDialog.createDialog();
					}
				}
				else {
					Log.d("TrashReport","Error");
				}
			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the image capture
				Toast.makeText(context, "Image capturing canceled", Toast.LENGTH_LONG).show();
			} else {
				// Image capture failed, advise user
				Toast.makeText(context, "Failed! Please try again", Toast.LENGTH_LONG).show();
			}
		}
		else if (requestCode == BROWSE_IMAGE_ACTIVITY_REQUEST_CODE){
			if (resultCode == RESULT_OK){
				if ( data != null){
					try {
						if (bitmap != null) {
							bitmap.recycle();
						}
						InputStream stream = getContentResolver().openInputStream(
								data.getData());
						bitmap = BitmapFactory.decodeStream(stream);
						stream.close();
						//7 the magic number?! apparently it works fine with 7. 
						//don't believe in magic and come back
						bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 7, 
								bitmap.getHeight() / 7, true);
						if ( request.contentEquals("report")){
							reportAllertDialog.setBitmap(bitmap);
							reportAllertDialog.setWithPicture(true);
							AlertDialog alertDialog = reportAllertDialog.createDialog();
						}
						else {
							bitmap = (Bitmap) data.getExtras().get("data");
							updateAlertDialog.setBitmap(bitmap);
							updateAlertDialog.setTheImageIsChanged(true);
						}
					} catch (FileNotFoundException e) {
		                e.printStackTrace();
		            } catch (IOException e) {
		                e.printStackTrace();
		            }
				}
				else {
					Log.d("TrashReport","Error");
				}
			} else if (resultCode == RESULT_CANCELED) {
			// User cancelled the image capture
			Toast.makeText(context, "Image browsing canceled", Toast.LENGTH_LONG).show();
			}
		}
	}

}
