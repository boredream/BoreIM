package com.boredream.im.utils;

import android.text.format.DateFormat;

public class DateUtils {
	
	public static CharSequence formatTime(long seconds) {
		return DateFormat.format("yyyy-MM-dd HH:mm:ss", seconds * 1000);
	}

}
