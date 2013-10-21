package com.facebookhackathon.serverrequests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

@SuppressWarnings("deprecation")
public class SendImageHttpPost {
	
	public void sendPost(String url, Bitmap bitmap, String magnitude, String lat, String lng){
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost postRequest = new HttpPost(url);
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			reqEntity.addPart("lat", new StringBody(lat));
			reqEntity.addPart("long", new StringBody(lng));
			reqEntity.addPart("magn", new StringBody(magnitude));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		
		try{
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        bitmap.compress(CompressFormat.JPEG, 75, bos);
	        byte[] data = bos.toByteArray();
	        ByteArrayBody bab = new ByteArrayBody(data, "forest.jpg");
	        reqEntity.addPart("img", bab);
	    }
		catch(Exception e){
	        try {
				reqEntity.addPart("img", new StringBody(""));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
	    }
		postRequest.setEntity(reqEntity);
		try {
			HttpResponse response = httpClient.execute(postRequest);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
