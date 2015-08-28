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
	 * ���ڵ�½�����Զ���½����µ��û����ϼ��������ϵļ�����
	 */
	public void updateUserInfos() {
		// ���µ���λ����Ϣ
		updateUserLocation();
		// ��ѯ���û��ĺ����б�(��������б���ȥ���������û���Ŷ),Ŀǰ֧�ֵĲ�ѯ���Ѹ���Ϊ100�������޸����ڵ����������ǰ����BmobConfig.LIMIT_CONTACTS���ɡ�
		// ����Ĭ�ϲ�ȡ���ǵ�½�ɹ�֮�󼴽������б�洢�����ݿ��У������µ���ǰ�ڴ���,
		userManager.queryCurrentContactList(new FindListener<BmobChatUser>() {

			@Override
			public void onError(int arg0, String arg1) {
				if (arg0 == BmobConfig.CODE_COMMON_NONE) {
					showLog(arg1);
				} else {
					showLog("��ѯ�����б�ʧ�ܣ�" + arg1);
				}
			}

			@Override
			public void onSuccess(List<BmobChatUser> arg0) {
				// ���浽application�з���Ƚ�
				application.setContactList(CollectionUtils.list2map(arg0));
			}
		});
	}

	/**
	 * �����û��ľ�γ����Ϣ
	 */
	public void updateUserLocation() {
		if (BaseApplication.lastPoint != null) {
			String saveLatitude = application.getLatitude();
			String saveLongtitude = application.getLongtitude();
			String newLat = String.valueOf(BaseApplication.lastPoint.getLatitude());
			String newLong = String.valueOf(BaseApplication.lastPoint.getLongitude());
			showLog("saveLatitude =" + saveLatitude + ",saveLongtitude = " + saveLongtitude);
			showLog("newLat =" + newLat + ",newLong = " + newLong);
			if (!saveLatitude.equals(newLat) || !saveLongtitude.equals(newLong)) {// ֻ��λ���б仯�͸��µ�ǰλ�ã��ﵽʵʱ���µ�Ŀ��
				final User user = (User) userManager.getCurrentUser(User.class);
				user.setLocation(BaseApplication.lastPoint);
				user.update(this, new UpdateListener() {
					@Override
					public void onSuccess() {
						application.setLatitude(String.valueOf(user.getLocation().getLatitude()));
						application.setLongtitude(String.valueOf(user.getLocation().getLongitude()));
						// ShowLog("��γ�ȸ��³ɹ�");
					}

					@Override
					public void onFailure(int code, String msg) {
						// ShowLog("��γ�ȸ��� ʧ��:"+msg);
					}
				});
			} else {
				// ShowLog("�û�λ��δ�������仯");
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
