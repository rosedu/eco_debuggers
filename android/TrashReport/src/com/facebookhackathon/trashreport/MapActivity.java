package com.facebookhackathon.trashreport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
	
	private ImageView img;
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private final static int BROWSE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
	private Uri fileUri;
	private final String JPEG_FILE_PREFIX = "TrashReport";
	private final String JPEG_FILE_SUFFIX = ".jpg";
	private String mCurrentPhotoPath;
	private final static int MEDIA_TYPE_IMAGE = 1;
	private Bitmap bitmap;
	private ReportAllertDialog reportAllertDialog;
	private UpdateAlertDialog updateAlertDialog;
	private int screenWidth;
	private int screenHeight;
	private LocationFinder locationFinder;
	private final LatLng ROMANIA = new LatLng(46, 25);
	private CameraView cameraView;
	private String request;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_map);
		
		img = (ImageView) findViewById(R.id.image_test);
		
		context = this;
		setScreenDimensions();
		
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
		        .getMap();
		map.getUiSettings().setZoomControlsEnabled(false);
		map.getUiSettings().setMyLocationButtonEnabled(true);
		map.setMyLocationEnabled(true);
		
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(ROMANIA, 5));
		
		locationFinder = new LocationFinder(context, map);
		locationFinder.getSyncPosition().start();

		//cameraView
		cameraView = new CameraView(context, map);
		cameraView.getCamera().start();
		
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
		
		map.setOnMarkerClickListener(new OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker marker) {
				Log.d("TrashReport", "Go for Dragos");
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

		//img = (ImageView) findViewById(R.id.image_test);
		reportButton = (Button) findViewById(R.id.button_report);
		reportButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if ( locationFinder.getLocation() != null) {
					MyLocation.myLat = locationFinder.getLastKnownLocation().getLatitude();
					MyLocation.myLong = locationFinder.getLastKnownLocation().getLongitude();
					reportAllertDialog = new ReportAllertDialog(context, false);
					AlertDialog alertDialog = reportAllertDialog.createDialog();
					request = "report";
				}
				else {
					Toast.makeText(context, "Your location is not set. Please wait", Toast.LENGTH_LONG).show();
				}
				 //alertDialog.show();
				/*AlertDialog.Builder builder = new AlertDialog.Builder(context);
				//builder.setTitle("Report area");
				//builder.setMessage("You want to report a messy area. In order to do this you have " +
				//		"to upload a photo.\nPlease select one of the obtion below:");
				builder.setPositiveButton("Take a picture", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// create Intent to take a picture and return control to the calling application
					    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					    
					    //fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
					    //intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

					    // start the image capture Intent
					    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
					}
				});
				builder.setNegativeButton("Browse a picture", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						startActivityForResult(intent, REQUEST_CODE);
					}
				});
				
				
				LayoutInflater inflater = getLayoutInflater();
				View view = inflater.inflate(R.layout.dialog_report,null);
				ImageView iv = (ImageView) view.findViewById(R.id.imageview_dialog_report);
				//RelativeLayout rl1 = (RelativeLayout) view.findViewById(R.id.relative_layout_report_buttons);
				iv.setVisibility(View.GONE);
				builder.setView(view);
				
				AlertDialog alertDialog;
				alertDialog = builder.create();
				alertDialog.show();
				
				//button for browsing local photos
				Button buttonBrowsePicture = (Button) alertDialog.findViewById(R.id.button_report_browse_picture);
				buttonBrowsePicture.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						//startActivityForResult(intent, REQUEST_CODE);
					}
				});
				
				//button for taking pictures
				Button buttonTakePicture = (Button) alertDialog.findViewById(R.id.button_report_take_picture);
				buttonTakePicture.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// create Intent to take a picture and return control to the calling application
					    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					    
					    // start the image capture Intent
					    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
					}
				});
			*/			
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
	
/*	public File getAlbumStorageDir(Context context, String albumName) {
	    // Get the directory for the app's private pictures directory. 
	    File file = new File(context.getExternalFilesDir(
	            Environment.DIRECTORY_PICTURES), albumName);
	    if (!file.mkdirs()) {
	        Log.e("TrashReport", "Directory not created");
	    }
	    return file;
	}*/
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				if (data != null){
				// Image captured and saved to fileUri specified in the Intent
					//img.setImageBitmap( (Bitmap) data.getExtras().get("data"));
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
						//I will be back to hardcode 7
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
						//Don't know. Does not look good. Try later!
						/*ImageView iv = (ImageView) alertDialog.findViewById(R.id.imageview_dialog_report);
						int h = bitmap.getHeight();
						int w = bitmap.getWidth();
						int H = dpToPx(200);
						int W = screenWidth - 20;
						if ( h * W > H * w ){
							bitmap = Bitmap.createScaledBitmap(bitmap, (int) Math.ceil((float)w * (float)H / (float)h), H, true);
						}
						else {
							Log.d("TrashReport", "" + h + " " + w + " " + H + " " + W + " " + (int) Math.ceil((float)h * (float)W / (float)w));
							bitmap = Bitmap.createScaledBitmap(bitmap, W, (int) Math.ceil((float)h * (float)W / (float)w), true);
						}
						iv.setImageBitmap(bitmap);*/
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
	
	@SuppressWarnings("deprecation")
	private void setScreenDimensions(){
		Display display = getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();
	}
	
	private int dpToPx(int dp) {
	    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
	    return px;
	}
	
	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
	    // Check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "TrashReport");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("TrashReport", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "TrashReport_" + ".jpg");
	    }  
	    else {
	        return null;
	    }

	    return mediaFile;
	}
	
	//action will be define like this
	// 0 - report & no picture
	// 1 - get back from browsing/taking with picture
	private void createDialog(Context context, int action){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_report,null);
		AlertDialog alertDialog;
		
		if ( action == 0){
			ImageView iv = (ImageView) view.findViewById(R.id.imageview_dialog_report);
			iv.setVisibility(View.GONE);
			builder.setView(view);

			alertDialog = builder.create();
			alertDialog.show();

			//button for browsing local photos
			Button buttonBrowsePicture = (Button) alertDialog.findViewById(R.id.button_report_browse_picture);
			buttonBrowsePicture.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					startActivityForResult(intent, BROWSE_IMAGE_ACTIVITY_REQUEST_CODE);
				}
			});

			//button for taking pictures
			Button buttonTakePicture = (Button) alertDialog.findViewById(R.id.button_report_take_picture);
			buttonTakePicture.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// create Intent to take a picture and return control to the calling application
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

					// start the image capture Intent
					startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
				}
			});
		}
		else if ( action == 1){		
				builder.setView(view);
				alertDialog = builder.create();
				alertDialog.show();

				//button for browsing local photos
				Button buttonBrowsePicture = (Button) alertDialog.findViewById(R.id.button_report_browse_picture);
				buttonBrowsePicture.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						startActivityForResult(intent, BROWSE_IMAGE_ACTIVITY_REQUEST_CODE);
					}
				});

				//button for taking pictures
				Button buttonTakePicture = (Button) alertDialog.findViewById(R.id.button_report_take_picture);
				buttonTakePicture.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// create Intent to take a picture and return control to the calling application
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

						// start the image capture Intent
						startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
					}
				});
		}
	}
	
	
/*	//Check if an intent is available
	private boolean isIntentAvailable(Context context, String action) {
	    final PackageManager packageManager = context.getPackageManager();
	    final Intent intent = new Intent(action);
	    List<ResolveInfo> list =
	            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}
	
	//method for invoking an Intent to capture a photo
	private void dispatchTakePictureIntent(int actionCode) {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    File f = null;
		try {
			f = createImageFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
	    startActivityForResult(takePictureIntent, actionCode);
	} */
	
	/*private File createImageFile() throws IOException {
	    // Create an image file name
	    String timeStamp = 
	        new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
	    File image = File.createTempFile(
	        imageFileName, 
	        JPEG_FILE_SUFFIX, 
	        getAlbumStorageDir(context, "TrashReport")
	    );
	    mCurrentPhotoPath = image.getAbsolutePath();
	    return image;
	}*/
	
	/*
	private void galleryAddPic() {
	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	    File f = new File(mCurrentPhotoPath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    this.sendBroadcast(mediaScanIntent);
	}*/
	
}
