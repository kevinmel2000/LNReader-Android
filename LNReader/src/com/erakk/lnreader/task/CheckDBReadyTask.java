package com.erakk.lnreader.task;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.erakk.lnreader.callback.CallbackEventData;
import com.erakk.lnreader.callback.ICallbackEventData;
import com.erakk.lnreader.callback.IExtendedCallbackNotifier;

public class CheckDBReadyTask extends AsyncTask<Void, ICallbackEventData, AsyncTaskResult<Boolean>> {
	private static final String TAG = CheckDBReadyTask.class.toString();
	public volatile IExtendedCallbackNotifier<AsyncTaskResult<Boolean>> owner;

	public CheckDBReadyTask(IExtendedCallbackNotifier<AsyncTaskResult<Boolean>> owner) {
		this.owner = owner;
	}

	@Override
	protected AsyncTaskResult<Boolean> doInBackground(Void... params) {
		String message = "Checking access to DB";
		int dot = 0;
		try {
			boolean flag = true;
			while (flag) {
				String suffix = "";
				if (dot == 0) {
					suffix = ".";
					dot = 1;
				} else if (dot == 1) {
					suffix = "..";
					dot = 2;
				} else if (dot == 1) {
					suffix = "...";
					dot = 0;
				}

				publishProgress(new CallbackEventData(message + suffix, TAG));
				Thread.sleep(500);

				flag = (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED);
				if(this.isCancelled()) {
					return new AsyncTaskResult<Boolean>(flag, Boolean.class);
				}
			}

			return new AsyncTaskResult<Boolean>(true, Boolean.class);
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage(), ex);
			return new AsyncTaskResult<Boolean>(false, Boolean.class);
		}
	}

	@Override
	protected void onPostExecute(AsyncTaskResult<Boolean> result) {
		owner.onCompleteCallback(new CallbackEventData("Result: " + result.getResult(), TAG), result);
	}
}
