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
import org.json.JSONTokener;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

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
						try {
							JSONArray finalResult = new JSONArray(tokener);
							Log.d("TrashReport", finalResult.toString());
						} catch (JSONException e) {
							e.printStackTrace();
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
