package com.boredream.im.utils;

import android.graphics.Bitmap;

import com.boredream.im.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

public class ImageOptHelper {
	
	public static DisplayImageOptions getDefOptions() {
		DisplayImageOptions imgOptions = new DisplayImageOptions.Builder()
			.cacheOnDisk(true)
			.cacheInMemory(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.showImageOnLoading(R.drawable.timeline_image_loading)
			.showImageForEmptyUri(R.drawable.timeline_image_loading)
			.showImageOnFail(R.drawable.timeline_image_failure)
			.build();
		return imgOptions;
	}
	
	public static DisplayImageOptions getAvatarOptions() {
		DisplayImageOptions	avatarOptions = new DisplayImageOptions.Builder()
			.cacheOnDisk(true)
			.cacheInMemory(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.showImageOnLoading(R.drawable.avatar_default)
			.showImageForEmptyUri(R.drawable.avatar_default)
			.showImageOnFail(R.drawable.avatar_default)
			// 只有 ImageAware类型的控件才可以使用
//			.displayer(new RoundedBitmapDisplayer(999))
			.build();
		return avatarOptions;
	}
}
