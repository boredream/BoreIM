package com.boredream.im.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.v3.listener.UpdateListener;

import com.boredream.im.BaseApplication;
import com.boredream.im.R;
import com.boredream.im.utils.CollectionUtils;
import com.boredream.im.utils.ImageOptHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * �µĺ�������
 */
public class NewFriendAdapter extends BaseAdapter {

	private Context context;
	private List<BmobInvitation> chatUsers;
	private ImageLoader imageLoader;

	public NewFriendAdapter(Context context, List<BmobInvitation> list) {
		this.context = context;
		imageLoader = ImageLoader.getInstance();
		
		// ���������ظ�����,������ʱ�䵹��,�����ظ�������ֻ���ȡ��һ�����µ�
		chatUsers = new ArrayList<BmobInvitation>();
		for(BmobInvitation invitation : list) {
			if(!containInvitation(chatUsers, invitation)) {
				chatUsers.add(invitation);
			}
		}
	}
	
	// �Ƿ��������,��id�ж�
	private boolean containInvitation(List<BmobInvitation> invitaions, BmobInvitation invitation) {
		for(BmobInvitation iv : invitaions) {
			if(iv.getFromid().equals(invitation.getFromid())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getCount() {
		return chatUsers.size();
	}

	@Override
	public BmobInvitation getItem(int position) {
		return chatUsers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.item_add_friend, null);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.iv_avatar = (ImageView) convertView
					.findViewById(R.id.iv_avatar);
			holder.btn_add = (Button) convertView.findViewById(R.id.btn_add);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final BmobInvitation item = getItem(position);

		holder.tv_name.setText(item.getFromname());
		imageLoader.displayImage(item.getAvatar(), holder.iv_avatar,
				ImageOptHelper.getAvatarOptions());

		int status = item.getStatus();
		// TODO ͬ��Ϊ����,�������������������
		if (status == BmobConfig.INVITE_ADD_NO_VALIDATION
				|| status == BmobConfig.INVITE_ADD_NO_VALI_RECEIVED) {
			holder.btn_add.setText("ͬ��");
			holder.btn_add.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Log.i("DDD", "���ͬ�ⰴť:" + item.getFromid());
					agressAdd(holder.btn_add, item);
				}
			});
		} else if (status == BmobConfig.INVITE_ADD_AGREE) {
			holder.btn_add.setText("��ͬ��");
			holder.btn_add.setEnabled(false);
		}
		return convertView;
	}

	public void remove(int position) {
		chatUsers.remove(position);
		notifyDataSetChanged();
	}
	
	/**
	 * ��Ӻ��� agressAdd
	 */
	private void agressAdd(final Button btn_add, final BmobInvitation msg) {
		final ProgressDialog progress = new ProgressDialog(context);
		progress.setMessage("�������...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		// ͬ����Ӻ���
		BmobUserManager.getInstance(context).agreeAddContact(msg,
				new UpdateListener() {

					@Override
					public void onSuccess() {
						progress.dismiss();
						btn_add.setText("��ͬ��");
						btn_add.setEnabled(false);
						// ���浽application�з���Ƚ�
						BaseApplication.mInstance.setContactList(CollectionUtils.list2map(
								BmobDB.create(context).getContactList()));
					}

					@Override
					public void onFailure(int arg0, final String arg1) {
						progress.dismiss();
						showToast("���ʧ��: " + arg1);
					}
				});
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
