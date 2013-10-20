package com.facebookhackathon.trashreport;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class UpdateAlertDialog extends ReportAllertDialog {

	private static boolean theImageIsChanged;
	private static boolean theMagnitudeIsChanged;
	private String imageId;
	
	public UpdateAlertDialog(Context context, boolean withPicture) {
		super(context, withPicture);
		theImageIsChanged = false;
		theMagnitudeIsChanged = false;
	}
	
	public UpdateAlertDialog(Context context, boolean withPicture, String imageId){
		super(context, withPicture);
		this.imageId = imageId;
	}
	
	@Override
	public AlertDialog createDialog() {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		View view = inflater.inflate(R.layout.dialog_report,null);
		ImageView iv = (ImageView) view.findViewById(R.id.imageview_dialog_report);
		
		iv.setVisibility(View.VISIBLE);
		iv.setImageBitmap(bitmap);
		builder.setView(view);
		
		alertDialog = builder.create();
		alertDialog.show();
		super.setPictureButtons();
		setUpdateButton();
		setSeekBar();
		
		return alertDialog;
	}
	
	public String getImageId() {
		return imageId;
	}
	
	public static void setTheImageIsChanged(boolean theImageIsChanged) {
		UpdateAlertDialog.theImageIsChanged = theImageIsChanged;
	}
	
	@Override
	protected void setSeekBar() {
		SeekBar seekBar = (SeekBar) alertDialog.findViewById(R.id.seek_bar_report);
		seekBar.setMax(3);
		final TextView messMagnitude = (TextView) alertDialog.findViewById(R.id.text_view_report_magnitude_status);
		for (int i=0; i<Markers.markerArray.size();i++){
			if (Markers.markerArray.get(i).getImageId().contentEquals(imageId)){
				int magn = Markers.markerArray.get(i).getMagnitude();
				if ( magn == 0){
					messMagnitude.setText("Clean");
				}
				else if ( magn == 1){
					messMagnitude.setText("Dirty");
				}
				else if ( magn == 2){
					messMagnitude.setText("Dirtier");
				}
				else {
					messMagnitude.setText("Dirtiest");
				}
				seekBar.setProgress(Markers.markerArray.get(i).getMagnitude());
				break;
			}
		}
		
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				//Do nothing
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				//Do nothing
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				seekBarProgress = progress;
				theMagnitudeIsChanged = true;
				if ( progress == 0){
					messMagnitude.setText("Clean");
				}
				else if ( progress == 1){
					messMagnitude.setText("Dirty");
				}
				else if ( progress == 2){
					messMagnitude.setText("Dirtier");
				}
				else {
					messMagnitude.setText("Dirtiest");
				}
			}
		});
	}
	
	private void setUpdateButton(){
		Button buttonSubmit = (Button) alertDialog.findViewById(R.id.button_report_submit);
		buttonSubmit.setText("Update");
		buttonSubmit.setEnabled(true);
		buttonSubmit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				for (int i=0; i<Markers.markerArray.size(); i++){
					if (imageId.contentEquals(Markers.markerArray.get(i).getImageId())){
						Markers.markerArray.get(i).setMagnitude(seekBarProgress);
						break;
					}
				}
				if (theImageIsChanged) {
					Thread t = new Thread(new Runnable() {

						@Override
						public void run() {
							SendImageHttpPut sender = new SendImageHttpPut();
							sender.sendPut(ApiRequestConstants.UPDATE_URL, bitmap, imageId, seekBarProgress + "");
						}
					});
					t.start();
					alertDialog.dismiss();
				}
				else {
					Thread t = new Thread(new Runnable() {

						@Override
						public void run() {
							HttpResponse resp = null;
							HttpPut httpPut = new HttpPut(ApiRequestConstants.UPDATE_URL );
							DefaultHttpClient mHttpClient = new DefaultHttpClient();
							
							List<NameValuePair> data = new ArrayList<NameValuePair>(2);

							data.add(new BasicNameValuePair("_id", imageId));
							data.add(new BasicNameValuePair("magn", seekBarProgress + ""));
							
							try {
								httpPut.setEntity(new UrlEncodedFormEntity(data));
							} catch (UnsupportedEncodingException e) {
								Log.d("TrashReport", "Error set entity");
								e.printStackTrace();
							}

							try {
								resp = mHttpClient.execute(httpPut);
							} catch (ClientProtocolException e) {
								Log.d("TrashReport", "Error execute 1");
								e.printStackTrace();
							} catch (IOException e) {
								Log.d("DEBUG", "Error execute 2");
								e.printStackTrace();
							}
						}
					});
					t.start();
					alertDialog.dismiss();
					Toast.makeText(context, "Update sent successful", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
}
