package com.facebookhackathon.trashreport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class DownloadImage {
	private Thread downloader;
	private Context context;
	private Bitmap bitmap;
	private String imageId;
	
	public DownloadImage(Context context, String imageId){
		this.context = context;
		this.imageId = imageId;
		this.bitmap = null;
		setDowloader();
	}
	
	private void setDowloader(){
		downloader = new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpResponse resp = null;
				DefaultHttpClient mHttpClient = new DefaultHttpClient();

				List<NameValuePair> data = new ArrayList<NameValuePair>(1);
				data.add(new BasicNameValuePair("_id", imageId));

				String paramString = URLEncodedUtils.format(data, "utf-8");
				Log.d("TrashReport", ApiRequestConstants.IMAGE_URL + "?" + paramString);
				HttpGet httpGet = new HttpGet(ApiRequestConstants.IMAGE_URL + "?"+ paramString);

				try {
					resp = mHttpClient.execute(httpGet);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				try {
					InputStream is = resp.getEntity().getContent();
					bitmap = BitmapFactory.decodeStream(is); 
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}
	
	public Thread getDownloader() {
		return downloader;
	}
	
	public String getImageId() {
		return imageId;
	}
}
