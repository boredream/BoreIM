package com.boredream.im.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.boredream.im.R;

@SuppressWarnings("serial")
public class EmotionUtils implements Serializable {

	public static Map<String, Integer> emojiMap;

	static {
		emojiMap = new HashMap<String, Integer>();
		 emojiMap.put("[ºÇºÇ]", R.drawable.d_hehe);
		 emojiMap.put("[ÎûÎû]", R.drawable.d_xixi);
		 emojiMap.put("[¹þ¹þ]", R.drawable.d_haha);
		 emojiMap.put("[°®Äã]", R.drawable.d_aini);
		 emojiMap.put("[ÍÚ±ÇÊº]", R.drawable.d_wabishi);
		 emojiMap.put("[³Ô¾ª]", R.drawable.d_chijing);
		 emojiMap.put("[ÔÎ]", R.drawable.d_yun);
		 emojiMap.put("[Àá]", R.drawable.d_lei);
		 emojiMap.put("[²ö×ì]", R.drawable.d_chanzui);
		 emojiMap.put("[×¥¿ñ]", R.drawable.d_zhuakuang);
		 emojiMap.put("[ºß]", R.drawable.d_heng);
		 emojiMap.put("[¿É°®]", R.drawable.d_keai);
		 emojiMap.put("[Å­]", R.drawable.d_nu);
		 emojiMap.put("[º¹]", R.drawable.d_han);
		 emojiMap.put("[º¦Ðß]", R.drawable.d_haixiu);
		 emojiMap.put("[Ë¯¾õ]", R.drawable.d_shuijiao);
		 emojiMap.put("[Ç®]", R.drawable.d_qian);
		 emojiMap.put("[ÍµÐ¦]", R.drawable.d_touxiao);
		 emojiMap.put("[Ð¦cry]", R.drawable.d_xiaoku);
		 emojiMap.put("[doge]", R.drawable.d_doge);
		 emojiMap.put("[ß÷ß÷]", R.drawable.d_miao);
		 emojiMap.put("[¿á]", R.drawable.d_ku);
		 emojiMap.put("[Ë¥]", R.drawable.d_shuai);
		 emojiMap.put("[±Õ×ì]", R.drawable.d_bizui);
		 emojiMap.put("[±ÉÊÓ]", R.drawable.d_bishi);
		 emojiMap.put("[»¨ÐÄ]", R.drawable.d_huaxin);
		 emojiMap.put("[¹ÄÕÆ]", R.drawable.d_guzhang);
		 emojiMap.put("[±¯ÉË]", R.drawable.d_beishang);
		 emojiMap.put("[Ë¼¿¼]", R.drawable.d_sikao);
		 emojiMap.put("[Éú²¡]", R.drawable.d_shengbing);
		 emojiMap.put("[Ç×Ç×]", R.drawable.d_qinqin);
		 emojiMap.put("[Å­Âî]", R.drawable.d_numa);
		 emojiMap.put("[Ì«¿ªÐÄ]", R.drawable.d_taikaixin);
		 emojiMap.put("[ÀÁµÃÀíÄã]", R.drawable.d_landelini);
		 emojiMap.put("[ÓÒºßºß]", R.drawable.d_youhengheng);
		 emojiMap.put("[×óºßºß]", R.drawable.d_zuohengheng);
		 emojiMap.put("[Ðê]", R.drawable.d_xu);
		 emojiMap.put("[Î¯Çü]", R.drawable.d_weiqu);
		 emojiMap.put("[ÍÂ]", R.drawable.d_tu);
		 emojiMap.put("[¿ÉÁ¯]", R.drawable.d_kelian);
		 emojiMap.put("[´ò¹þÆø]", R.drawable.d_dahaqi);
		 emojiMap.put("[¼·ÑÛ]", R.drawable.d_jiyan);
		 emojiMap.put("[Ê§Íû]", R.drawable.d_shiwang);
		 emojiMap.put("[¶¥]", R.drawable.d_ding);
		 emojiMap.put("[ÒÉÎÊ]", R.drawable.d_yiwen);
		 emojiMap.put("[À§]", R.drawable.d_kun);
		 emojiMap.put("[¸ÐÃ°]", R.drawable.d_ganmao);
		 emojiMap.put("[°Ý°Ý]", R.drawable.d_baibai);
		 emojiMap.put("[ºÚÏß]", R.drawable.d_heixian);
		 emojiMap.put("[ÒõÏÕ]", R.drawable.d_yinxian);
		 emojiMap.put("[´òÁ³]", R.drawable.d_dalian);
		 emojiMap.put("[ÉµÑÛ]", R.drawable.d_shayan);
		 emojiMap.put("[ÖíÍ·]", R.drawable.d_zhutou);
		 emojiMap.put("[ÐÜÃ¨]", R.drawable.d_xiongmao);
		 emojiMap.put("[ÍÃ×Ó]", R.drawable.d_tuzi);
	}

	public static int getImgByName(String imgName) {
		Integer integer = emojiMap.get(imgName);
		return integer == null ? -1 : integer;
	}

	public static SpannableString getEmotionContent(final Context context, final TextView tv, String source) {
		String regexEmoji = "\\[[\u4e00-\u9fa5\\w]+\\]";

		SpannableString spannableString = new SpannableString(source);

		Pattern pattern = Pattern.compile(regexEmoji);
		Matcher matcher = pattern.matcher(spannableString);

		while (matcher.find()) {
			String emojiStr = matcher.group();
			int start = matcher.start();

			int imgRes = EmotionUtils.getImgByName(emojiStr);
			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imgRes);

			if (bitmap != null) {
				int size = (int) tv.getTextSize();
				bitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);

				ImageSpan imageSpan = new ImageSpan(context, bitmap);
				spannableString.setSpan(imageSpan, start, start + emojiStr.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}

		return spannableString;
	}
}
