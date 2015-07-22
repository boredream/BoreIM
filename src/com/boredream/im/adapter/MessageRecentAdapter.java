package com.boredream.im.adapter;

import java.util.List;

import android.content.Context;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.im.bean.BmobRecent;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;

import com.boredream.im.R;
import com.boredream.im.utils.EmotionUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 会话适配器
 */
public class MessageRecentAdapter extends BaseAdapter {

	private Context context;
	private List<BmobRecent> datas;

	public MessageRecentAdapter(Context context, List<BmobRecent> datas) {
		this.context = context;
		this.datas = datas;
	}

	public void remove(BmobRecent recent) {
		datas.remove(recent);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public BmobRecent getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.item_conversation, null);
			holder.iv_recent_avatar = (ImageView) convertView.findViewById(R.id.iv_recent_avatar);
			holder.tv_recent_name = (TextView) convertView.findViewById(R.id.tv_recent_name);
			holder.tv_recent_msg = (TextView) convertView.findViewById(R.id.tv_recent_msg);
			holder.tv_recent_time = (TextView) convertView.findViewById(R.id.tv_recent_time);
			holder.tv_recent_unread = (TextView) convertView.findViewById(R.id.tv_recent_unread);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// set data
		BmobRecent item = getItem(position);
		// 填充数据
		String avatar = item.getAvatar();
		ImageLoader.getInstance().displayImage(avatar, holder.iv_recent_avatar);

		holder.tv_recent_name.setText(item.getUserName());
		holder.tv_recent_time.setText(DateFormat.format("yyyy-MM-dd", item.getTime()));
		// 显示内容
		if (item.getType() == BmobConfig.TYPE_TEXT) {
			SpannableString spannableString = EmotionUtils.getEmotionContent(
					context, holder.tv_recent_msg, item.getMessage());
			holder.tv_recent_msg.setText(spannableString);
		} else if (item.getType() == BmobConfig.TYPE_IMAGE) {
			holder.tv_recent_msg.setText("[图片]");
		} else if (item.getType() == BmobConfig.TYPE_LOCATION) {
			String all = item.getMessage();
			if (all != null && !all.equals("")) {// 位置类型的信息组装格式：地理位置&维度&经度
				String address = all.split("&")[0];
				holder.tv_recent_msg.setText("[位置]" + address);
			}
		} else if (item.getType() == BmobConfig.TYPE_VOICE) {
			holder.tv_recent_msg.setText("[语音]");
		}

		int num = BmobDB.create(context).getUnreadCount(item.getTargetid());
		if (num > 0) {
			holder.tv_recent_unread.setVisibility(View.VISIBLE);
			holder.tv_recent_unread.setText(num + "");
		} else {
			holder.tv_recent_unread.setVisibility(View.GONE);
		}

		return convertView;
	}

	public static class ViewHolder {
		public ImageView iv_recent_avatar;
		public TextView tv_recent_name;
		public TextView tv_recent_msg;
		public TextView tv_recent_time;
		public TextView tv_recent_unread;
	}

}
