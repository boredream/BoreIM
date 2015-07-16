package com.boredream.im.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.im.bean.BmobChatUser;

import com.boredream.im.R;
import com.boredream.im.utils.ImageOptHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ChatUserAdapter extends BaseAdapter {
	private Context context;
	private List<BmobChatUser> users;

	private ImageLoader imageLoader;

	public ChatUserAdapter(Context context, List<BmobChatUser> datas) {
		this.context = context;
		this.users = datas;
		imageLoader = ImageLoader.getInstance();
	}

	public void updateListView(List<BmobChatUser> list) {
		this.users = list;
		notifyDataSetChanged();
	}

	public void remove(BmobChatUser user) {
		this.users.remove(user);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return users.size();
	}

	@Override
	public BmobChatUser getItem(int position) {
		return users.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = View.inflate(context, R.layout.item_user_friend, null);
			holder = new ViewHolder();
			holder.tv_alpha = (TextView) convertView.findViewById(R.id.tv_alpha);
			holder.tv_friend_name = (TextView) convertView.findViewById(R.id.tv_friend_name);
			holder.iv_friend_avatar = (ImageView) convertView.findViewById(R.id.iv_friend_avatar);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		BmobChatUser user = getItem(position);
		final String name = user.getUsername();
		final String avatar = user.getAvatar();

		imageLoader.displayImage(avatar, holder.iv_friend_avatar, 
				ImageOptHelper.getAvatarOptions());
		holder.tv_friend_name.setText(name);

		return convertView;
	}

	static class ViewHolder {
		TextView tv_alpha;
		ImageView iv_friend_avatar;
		TextView tv_friend_name;
	}

}