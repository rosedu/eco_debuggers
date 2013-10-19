package com.facebookhackathon.trashreport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

public class MapActivity extends Activity {
	private GoogleMap map;
	private Context context;
	private Button reportButton;
	
	private ImageView img;
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri fileUri;
	private final String JPEG_FILE_PREFIX = "TrashReport";
	private final String JPEG_FILE_SUFFIX = ".jpg";
	private String mCurrentPhotoPath;
	private final static int MEDIA_TYPE_IMAGE = 1;
	private final static int REQUEST_CODE = 1;
	private Bitmap bitmap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_map);
		
		context = this;
		
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
		        .getMap();
		map.getUiSettings().setZoomControlsEnabled(false);
		map.getUiSettings().setMyLocationButtonEnabled(true);
		
		img = (ImageView) findViewById(R.id.image_test);
		reportButton = (Button) findViewById(R.id.button_report);
		reportButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				//builder.setTitle("Report area");
				//builder.setMessage("You want to report a messy area. In order to do this you have " +
				//		"to upload a photo.\nPlease select one of the obtion below:");
				/*builder.setPositiveButton("Take a picture", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// create Intent to take a picture and return control to the calling application
					    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					    
					    //fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
					    //intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

					    // start the image capture Intent
					    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
					}
				});*/
				/*builder.setNegativeButton("Browse a picture", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						startActivityForResult(intent, REQUEST_CODE);
					}
				});*/
				
				
				LayoutInflater inflater = getLayoutInflater();
				View view = inflater.inflate(R.layout.dialog_report,null);
				ImageView iv = (ImageView) view.findViewById(R.id.imageview_dialog_report);
				//RelativeLayout rl1 = (RelativeLayout) view.findViewById(R.id.relative_layout_report_buttons);
				iv.setVisibility(View.GONE);
				builder.setView(view);
				
				AlertDialog alertDialog;
				alertDialog = builder.create();
				alertDialog.show();
				
				
			}
		});
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
					img.setImageBitmap( (Bitmap) data.getExtras().get("data"));
					Toast.makeText(context, "Image saved to:\n" +
						data.getData(), Toast.LENGTH_LONG).show();
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
		else if (requestCode == REQUEST_CODE){
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
						bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
						img.setImageBitmap(bitmap);
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
