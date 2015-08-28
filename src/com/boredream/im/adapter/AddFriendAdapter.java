package com.boredream.im.adapter;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.inteface.MsgTag;
import cn.bmob.v3.listener.PushListener;

import com.boredream.im.R;
import com.boredream.im.utils.ImageOptHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 查找好友
 */
public class AddFriendAdapter extends BaseAdapter {

	private Context context;
	private List<BmobChatUser> chatUsers;
	private ImageLoader imageLoader;

	public AddFriendAdapter(Context context, List<BmobChatUser> list) {
		this.context = context;
		this.chatUsers = list;
		imageLoader = ImageLoader.getInstance();
	}

	@Override
	public int getCount() {
		return chatUsers.size();
	}

	@Override
	public BmobChatUser getItem(int position) {
		return chatUsers.get(position);
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
			convertView = View.inflate(context, R.layout.item_add_friend, null);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
			holder.btn_add = (Button) convertView.findViewById(R.id.btn_add);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final BmobChatUser item = getItem(position);
		
		holder.tv_name.setText(item.getUsername());
		imageLoader.displayImage(item.getAvatar(), holder.iv_avatar, ImageOptHelper.getAvatarOptions());
		
		holder.btn_add.setText("添加");
		holder.btn_add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final ProgressDialog progress = new ProgressDialog(context);
				progress.setMessage("正在添加...");
				progress.setCanceledOnTouchOutside(false);
				progress.show();
				// 发送tag请求
				BmobChatManager.getInstance(context).sendTagMessage(MsgTag.ADD_CONTACT, item.getObjectId(),
						new PushListener() {

							@Override
							public void onSuccess() {
								progress.dismiss();
								showToast("发送请求成功，等待对方验证!");
							}

							@Override
							public void onFailure(int arg0, final String arg1) {
								progress.dismiss();
								showToast("发送请求失败，请重新添加!");
							}
						});
			}
		});
		return convertView;
	}

	static class ViewHolder {
		TextView tv_name;
		ImageView iv_avatar;
		Button btn_add;
	}

	private void showToast(String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}
}
