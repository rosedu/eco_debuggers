package com.facebookhackathon.trashreport;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class MarkersAttributes {
	private Marker marker;
	private LatLng latLng;
	private int magnitude;
	private String imageId;
	private Bitmap bitmap;
	private Circle circle;
	
	public MarkersAttributes(Marker marker){
		this.marker = marker;
	}
	
	public MarkersAttributes(Marker marker, LatLng latLng){
		this.marker = marker;
		this.latLng = latLng;
	}
	
	public MarkersAttributes(Marker marker, LatLng latLng, int magnitude){
		this.marker = marker;
		this.latLng = latLng;
		this.magnitude = magnitude;
	}
	
	public MarkersAttributes(Marker marker, LatLng latLng, int magnitude, String imageId){
		this.marker = marker;
		this.latLng = latLng;
		this.magnitude = magnitude;
		this.imageId = imageId;
	}
	
	public MarkersAttributes(Marker marker, LatLng latLng, int magnitude, String imageId, Circle circle){
		this.marker = marker;
		this.latLng = latLng;
		this.magnitude = magnitude;
		this.imageId = imageId;
		this.circle = circle;
	}
	
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	public void setLatLng(LatLng latLng) {
		this.latLng = latLng;
	}
	
	public void setMagnitude(int magnitude) {
		this.magnitude = magnitude;
	}
	
	public void setMarker(Marker marker) {
		this.marker = marker;
	}
	
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	
	public void setCircle(Circle circle) {
		this.circle = circle;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}
	
	public LatLng getLatLng() {
		return latLng;
	}
	
	public int getMagnitude() {
		return magnitude;
	}
	
	public Marker getMarker() {
		return marker;
	}
	
	public String getImageId() {
		return imageId;
	}
	
	public Circle getCircle() {
		return circle;
	}
}
