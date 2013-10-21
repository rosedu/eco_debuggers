package com.facebookhackathon.alertdialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.facebookhackathon.gps.MyLocation;
import com.facebookhackathon.serverrequests.ApiRequestConstants;
import com.facebookhackathon.serverrequests.SendImageHttpPost;
import com.facebookhackathon.trashreport.R;

public class ReportAlertDialog {
	protected AlertDialog alertDialog;
	protected AlertDialog.Builder builder;
	protected boolean withPicture;
	protected Context context;
	protected Button buttonTakePicture;
	protected Button buttonBrowsePicture;
	protected final int BROWSE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
	protected final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	protected Bitmap bitmap;
	protected int seekBarProgress;
	
	public ReportAlertDialog() {
	}
	
	public ReportAlertDialog(Context context, boolean withPicture){
		this.withPicture = withPicture;
		this.context = context;
		this.builder = new AlertDialog.Builder(this.context);
	}	
	
	public AlertDialog createDialog(){
		LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		View view = inflater.inflate(R.layout.dialog_report,null);
		ImageView iv = (ImageView) view.findViewById(R.id.imageview_dialog_report);
		
		if (withPicture == false){
			iv.setVisibility(View.GONE);
			builder.setView(view);

			alertDialog = builder.create();
			alertDialog.show();
			setPictureButtons();
			setSeekBar();
			Button buttonSubmit = (Button) alertDialog.findViewById(R.id.button_report_submit);
			buttonSubmit.setEnabled(false);
			return alertDialog;
		}
		else {
			iv.setVisibility(View.VISIBLE);
			iv.setImageBitmap(bitmap);
			builder.setView(view);

			alertDialog = builder.create();
			alertDialog.show();
			setPictureButtons();
			setSubmitButton();
			setSeekBar();
			
			return alertDialog;
		}
	}
	
	protected void setSeekBar(){
		SeekBar seekBar = (SeekBar) alertDialog.findViewById(R.id.seek_bar_report);
		final TextView messMagnitude = (TextView) alertDialog.findViewById(R.id.text_view_report_magnitude_status);
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
				if ( progress == 0){
					messMagnitude.setText("Dirty");
				}
				else if ( progress == 1){
					messMagnitude.setText("Dirtier");
				}
				else {
					messMagnitude.setText("Dirtiest");
				}
			}
		});
	}
	
	protected void setSubmitButton(){
		Button buttonSubmit = (Button) alertDialog.findViewById(R.id.button_report_submit);
		buttonSubmit.setEnabled(true);
		buttonSubmit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Thread t = new Thread(new Runnable() {
					
					@Override
					public void run() {
						SendImageHttpPost sender = new SendImageHttpPost();
						sender.sendPost(ApiRequestConstants.REPORT_URL, bitmap, (seekBarProgress + 1) + "",
										MyLocation.myLat + "", MyLocation.myLong + "");
					}
				});
				t.start();
				alertDialog.dismiss();
				Toast.makeText(context, "Report sent successful", Toast.LENGTH_LONG).show();
			}
		});
	}
	
	protected void setPictureButtons(){
		buttonBrowsePicture = (Button) alertDialog.findViewById(R.id.button_report_browse_picture);
		buttonBrowsePicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				((Activity) context).startActivityForResult(intent, BROWSE_IMAGE_ACTIVITY_REQUEST_CODE);
				alertDialog.dismiss();
			}
		});
		
		buttonTakePicture = (Button) alertDialog.findViewById(R.id.button_report_take_picture);
		buttonTakePicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// create Intent to take a picture and return control to the calling application
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				// start the image capture Intent
				((Activity) context).startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
				alertDialog.dismiss();
			}
		});
	}
	
	//Getters and Setters
	public void setContext(Context context) {
		this.context = context;
	}
	
	public void setWithPicture(boolean withPicture) {
		this.withPicture = withPicture;
	}
	
	public boolean isWithPicture() {
		return withPicture;
	}
	
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}
}
