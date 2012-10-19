package com.erakk.lnreader.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import com.erakk.lnreader.Constants;
import com.erakk.lnreader.R;
import com.erakk.lnreader.UIHelper;
import com.erakk.lnreader.callback.DownloadCallbackEventData;
import com.erakk.lnreader.callback.ICallbackEventData;
import com.erakk.lnreader.helper.AsyncTaskResult;
import com.erakk.lnreader.model.ImageModel;
import com.erakk.lnreader.task.IAsyncTaskOwner;
import com.erakk.lnreader.task.LoadImageTask;

public class DisplayImageActivity extends Activity implements IAsyncTaskOwner{
	//private NovelsDao dao = NovelsDao.getInstance(this);
	private WebView imgWebView;
	private LoadImageTask task;
	//private boolean refresh = false;
	private String url;
	private ProgressDialog dialog;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	UIHelper.SetTheme(this, R.layout.activity_display_image);
        UIHelper.SetActionBarDisplayHomeAsUp(this, true);
        
        imgWebView = (WebView) findViewById(R.id.webView1);
        imgWebView.getSettings().setAllowFileAccess(true);
        imgWebView.getSettings().setBuiltInZoomControls(true);
        imgWebView.getSettings().setLoadWithOverviewMode(true);
        imgWebView.getSettings().setUseWideViewPort(true);
        imgWebView.setBackgroundColor(0);
        
        Intent intent = getIntent();
        url = intent.getStringExtra(Constants.EXTRA_IMAGE_URL);
        executeTask(url, false);
    }
	
	@SuppressLint("NewApi")
	private void executeTask(String url, boolean refresh) {        
		task = new LoadImageTask(refresh, this);        
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[] {url});
		else
			task.execute(new String[] {url});
	}
	
    @Override
    protected void onStop() {
    	// check running task
    	if(task != null){
    		if(!(task.getStatus() == Status.FINISHED)) {
    			Toast.makeText(this, "Canceling task: " + task.toString(), Toast.LENGTH_SHORT).show();
    			task.cancel(true);    			
    		}
    	}
    	super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_display_image, menu);
        return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent launchNewIntent = new Intent(this, DisplaySettingsActivity.class);
			startActivity(launchNewIntent);
			return true;
		case R.id.menu_refresh_image:			
			/*
			 * Implement code to refresh image content
			 */
			//refresh = true;
			executeTask(url, true);			
			return true;
		case android.R.id.home:
			super.onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
    
    @Override
	public void toggleProgressBar(boolean show) {
		if(show) {
			dialog = ProgressDialog.show(this, "Display Image", "Loading Image...", false);
			dialog.getWindow().setGravity(Gravity.CENTER);
			dialog.setCanceledOnTouchOutside(true);
		}
		else {
			dialog.dismiss();
		}
	}

	@Override
	public void setMessageDialog(ICallbackEventData message) {
		if(dialog != null && dialog.isShowing()){
			ICallbackEventData data = message;
			dialog.setMessage(data.getMessage());

			if(data.getClass() == DownloadCallbackEventData.class) {
				DownloadCallbackEventData downloadData = (DownloadCallbackEventData) data;
				int percent = downloadData.getPercentage();
				synchronized (dialog) {
					if(percent > -1) {
						// somehow doesn't works....
						dialog.setIndeterminate(false);
						dialog.setSecondaryProgress(percent);
						dialog.setMax(100);
						dialog.setProgress(percent);
						dialog.setMessage(data.getMessage());
					}
					else {
						dialog.setIndeterminate(true);
						dialog.setMessage(data.getMessage());
					}
				}
			}
		}
	}

	@Override
	public void getResult(AsyncTaskResult<?> result) {
		Exception e = result.getError();
		if(e == null) {
			ImageModel imageModel = (ImageModel) result.getResult();
			imgWebView = (WebView) findViewById(R.id.webView1);
			String imageUrl = "file:///" + imageModel.getPath(); 
			imgWebView.loadUrl(imageUrl);
			String title = imageModel.getName();
			setTitle(title.substring(title.lastIndexOf("/")));
			Log.d("LoadImageTask", "Loaded: " + imageUrl);
		}
		else{
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), e.getClass() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
}
