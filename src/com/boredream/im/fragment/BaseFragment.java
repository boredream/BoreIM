package com.boredream.im.fragment;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.boredream.im.BaseApplication;
import com.boredream.im.utils.CommonConstants;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;

public abstract class BaseFragment extends Fragment {

	protected String TAG;

	protected Activity activity;
	protected BaseApplication application;
	protected SharedPreferences sp;
	protected Dialog progressDialog;
	
	protected ImageLoader imageLoader;
	protected Gson gson;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TAG = this.getClass().getSimpleName();
		showLog("onCreate()");
		
		activity = getActivity();
		application = (BaseApplication) activity.getApplication();
		sp = activity.getSharedPreferences(CommonConstants.SP_NAME, Context.MODE_PRIVATE);
		progressDialog = new ProgressDialog(activity);
		
		imageLoader = ImageLoader.getInstance();
		gson = new Gson();
	}
	
	protected void intent2Activity(Class<?> tarActivity) {
		Intent intent = new Intent(activity, tarActivity);
		startActivity(intent);
	}
	
	protected void showToast(int msgResId) {
		showToast(getResources().getString(msgResId));
	}

	protected void showToast(String msg) {
		Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
	}

	protected void showLog(String msg) {
		Log.i(TAG, msg);
	}

}
