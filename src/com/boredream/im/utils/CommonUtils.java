package com.boredream.im.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class CommonUtils {

	/** 检查是否有网络 */
	public static boolean isNetworkAvailable(Context context) {
		NetworkInfo info = getNetworkInfo(context);
		return info != null && info.isAvailable();
	}

	/** 检查是否是WIFI */
	public static boolean isWifi(Context context) {
		NetworkInfo info = getNetworkInfo(context);
		return info != null && info.getType() == ConnectivityManager.TYPE_WIFI;
	}

	/** 检查是否是移动网络 */
	public static boolean isMobile(Context context) {
		NetworkInfo info = getNetworkInfo(context);
		return info != null && info.getType() == ConnectivityManager.TYPE_MOBILE;
	}

	/** 获取网络信息 */
	private static NetworkInfo getNetworkInfo(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo();
	}

	/** 检查SD卡是否存在 */
	public static boolean checkSdCard() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}
	
	/** 隐藏软键盘 */
	public static void hideSoftInputView(Activity context, EditText et) {
		if (context.getWindow().getAttributes().softInputMode == 
				WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE) {
			if (context.getCurrentFocus() != null)
				((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(et.getWindowToken(), 0);
		}
	}

	/** 显示软键盘 */
	public static void showSoftInputView(Activity context, EditText et) {
		if (context.getWindow().getAttributes().softInputMode == 
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (context.getCurrentFocus() != null)
				((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
					.showSoftInput(et, 0);
		}
	}

}
