package com.facebookhackathon.trashreport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class CameraView {
	private static double lat_ne;
	private static double long_ne;
	private static double lat_sw;
	private static double long_sw;
	private Thread camera;
	private Context context;
	private GoogleMap map;
	private boolean dataWasChanged;
	private boolean isRunning;
	
	public CameraView(Context context, GoogleMap map){
		this.context = context;
		this.map = map;
		isRunning = true;
		dataWasChanged = true;
		setCamera();
	}
	
	private void setCamera(){
		camera = new Thread(new Runnable() {

			@Override
			public void run() {
				while (isRunning){
					if (dataWasChanged){
						dataWasChanged = false;
						HttpResponse resp = null;
						DefaultHttpClient mHttpClient = new DefaultHttpClient();

						List<NameValuePair> data = new ArrayList<NameValuePair>(4);
						data.add(new BasicNameValuePair("long_ne", long_ne + ""));
						data.add(new BasicNameValuePair("lat_ne", lat_ne + ""));
						data.add(new BasicNameValuePair("long_sw", long_sw + ""));
						data.add(new BasicNameValuePair("lat_sw", lat_sw + ""));

						String paramString = URLEncodedUtils.format(data, "utf-8");
						Log.d("TrashReport", ApiRequestConstants.TRASH_URL + "?" + paramString);
						HttpGet httpGet = new HttpGet(ApiRequestConstants.TRASH_URL + "?"+ paramString);

						try {
							resp = mHttpClient.execute(httpGet);
						} catch (ClientProtocolException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

						BufferedReader reader = null;
						try {
							reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), "UTF-8"));
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} catch (IllegalStateException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						StringBuilder builder = new StringBuilder();
						try {
							for (String line = null; (line = reader.readLine()) != null;) {
								builder.append(line).append("\n");
							}
						} catch (IOException e) {
							e.printStackTrace();
						}

						JSONTokener tokener = new JSONTokener(builder.toString());
						JSONArray finalResult = null;
						try {
							finalResult = new JSONArray(tokener);
							Log.d("TrashReport", finalResult.toString());
						} catch (JSONException e) {
							e.printStackTrace();
						}

						if (finalResult != null){
							for (int i=0; i<finalResult.length(); i++){
								try {
									Log.d("TrashReport", "Here");
									JSONObject jObj = finalResult.getJSONObject(i);
									final String id = jObj.getString("_id");
									if (!Markers.markersHashTable.contains(id)){
										Markers.markersHashTable.add(id);
										final LatLng ll = new LatLng(jObj.getDouble("lat"), jObj.getDouble("long"));
										final int magnitude = jObj.getInt("magn");
										final CircleOptions co = new CircleOptions()
															.center(ll)
															.radius(magnitude * 20);
										if (magnitude == 3){
											co.fillColor(Color.argb(0, 180, 20, 0));
										} else if (magnitude == 2) {
											co.fillColor(Color.argb(0, 130, 20, 0));
										} else {
											co.fillColor(Color.argb(0, 130, 20, 0));
										}
										
										((Activity) context).runOnUiThread( new Runnable() {

											@Override
											public void run() {
												Markers.markerArray.add( 
														new MarkersAttributes(
																map.addMarker(new MarkerOptions().position(ll)),
																ll, magnitude, id, map.addCircle(co)));
											}
										});
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}
					}

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	public Thread getCamera() {
		return camera;
	}
	
	public void setDataWasChanged(boolean dataWasChanged) {
		this.dataWasChanged = dataWasChanged;
	}
	
	public void setLat_ne(double lat_ne) {
		CameraView.lat_ne = lat_ne;
	}
	
	public void setLat_sw(double lat_sw) {
		CameraView.lat_sw = lat_sw;
	}
	
	public void setLong_ne(double long_ne) {
		CameraView.long_ne = long_ne;
	}
	
	public void setLong_sw(double long_sw) {
		CameraView.long_sw = long_sw;
	}
	
	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
}
