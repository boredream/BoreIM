package com.boredream.im.activity;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import com.boredream.im.BaseApplication;
import com.boredream.im.R;
import com.boredream.im.entity.User;
import com.boredream.im.utils.CollectionUtils;
import com.boredream.im.utils.CommonConstants;
import com.boredream.im.utils.TitleBuilder;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;

public abstract class BaseActivity extends Activity {

	protected String TAG;

	protected BaseApplication application;
	protected SharedPreferences sp;
	protected Dialog progressDialog;

	protected ImageLoader imageLoader;
	protected Gson gson;

	// bmob
	protected BmobUserManager userManager;
	protected BmobChatManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TAG = this.getClass().getSimpleName();
		showLog("onCreate()");

		application = (BaseApplication) getApplication();
		sp = getSharedPreferences(CommonConstants.SP_NAME, MODE_PRIVATE);
		progressDialog = new ProgressDialog(this);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		imageLoader = ImageLoader.getInstance();
		gson = new Gson();

		// bmob
		userManager = BmobUserManager.getInstance(this);
		manager = BmobChatManager.getInstance(this);
	}

	protected void intent2Activity(Class<?> tarActivity) {
		Intent intent = new Intent(this, tarActivity);
		startActivity(intent);
	}

	/**
	 * 用于登陆或者自动登陆情况下的用户资料及好友资料的检测更新
	 */
	public void updateUserInfos() {
		// 更新地理位置信息
		updateUserLocation();
		// 查询该用户的好友列表(这个好友列表是去除黑名单用户的哦),目前支持的查询好友个数为100，如需修改请在调用这个方法前设置BmobConfig.LIMIT_CONTACTS即可。
		// 这里默认采取的是登陆成功之后即将好于列表存储到数据库中，并更新到当前内存中,
		userManager.queryCurrentContactList(new FindListener<BmobChatUser>() {

			@Override
			public void onError(int arg0, String arg1) {
				if (arg0 == BmobConfig.CODE_COMMON_NONE) {
					showLog(arg1);
				} else {
					showLog("查询好友列表失败：" + arg1);
				}
			}

			@Override
			public void onSuccess(List<BmobChatUser> arg0) {
				// 保存到application中方便比较
				application.setContactList(CollectionUtils.list2map(arg0));
			}
		});
	}

	/**
	 * 更新用户的经纬度信息
	 */
	public void updateUserLocation() {
		if (BaseApplication.lastPoint != null) {
			String saveLatitude = application.getLatitude();
			String saveLongtitude = application.getLongtitude();
			String newLat = String.valueOf(BaseApplication.lastPoint.getLatitude());
			String newLong = String.valueOf(BaseApplication.lastPoint.getLongitude());
			showLog("saveLatitude =" + saveLatitude + ",saveLongtitude = " + saveLongtitude);
			showLog("newLat =" + newLat + ",newLong = " + newLong);
			if (!saveLatitude.equals(newLat) || !saveLongtitude.equals(newLong)) {// 只有位置有变化就更新当前位置，达到实时更新的目的
				final User user = (User) userManager.getCurrentUser(User.class);
				user.setLocation(BaseApplication.lastPoint);
				user.update(this, new UpdateListener() {
					@Override
					public void onSuccess() {
						application.setLatitude(String.valueOf(user.getLocation().getLatitude()));
						application.setLongtitude(String.valueOf(user.getLocation().getLongitude()));
						// ShowLog("经纬度更新成功");
					}

					@Override
					public void onFailure(int code, String msg) {
						// ShowLog("经纬度更新 失败:"+msg);
					}
				});
			} else {
				// ShowLog("用户位置未发生过变化");
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		showLog("onStart()");
	}

	@Override
	protected void onResume() {
		super.onResume();
		showLog("onResume()");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		showLog("onDestroy()");
	}

	@Override
	protected void onStop() {
		super.onStop();
		showLog("onStop()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		showLog("onPause()");
	}
	
	protected void initBackTitle(String title) {
		new TitleBuilder(this)
			.setTitleText(title)
			.setLeftImage(R.drawable.back_icon)
			.setLeftOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
	}

	protected void finishActivity() {
		this.finish();
	}

	protected void showToast(int msgResId) {
		showToast(getResources().getString(msgResId));
	}

	protected void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	protected void showLog(String msg) {
		Log.i(TAG, msg);
	}

}
