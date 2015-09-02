package com.boredream.im.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.bmob.im.BmobDownloadManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.inteface.DownloadListener;

import com.boredream.im.R;
import com.boredream.im.activity.UserInfoActivity;
import com.boredream.im.listener.NewRecordPlayClickListener;
import com.boredream.im.utils.DateUtils;
import com.boredream.im.utils.DisplayUtils;
import com.boredream.im.utils.EmotionUtils;
import com.boredream.im.utils.ImageOptHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * ����������
 */
public class MessageChatAdapter extends BaseAdapter {

	// �ı�
	public final int TYPE_SEND_TXT = 0;
	public final int TYPE_RECEIVER_TXT = 1;
	// ͼƬ
	public final int TYPE_SEND_IMAGE = 2;
	public final int TYPE_RECEIVER_IMAGE = 3;
	// ����λ��
	public final int TYPE_SEND_LOCATION = 4;
	public final int TYPE_RECEIVER_LOCATION = 5;
	// ����
	public final int TYPE_SEND_VOICE = 6;
	public final int TYPE_RECEIVER_VOICE = 7;
	
	private Context context;
	private List<BmobMsg> list;
	
	private String curUserId;
	
	public List<BmobMsg> getList() {
		return list;
	}
	
	public void setList(List<BmobMsg> list) {
		this.list = list;
	}
	
	public void add(BmobMsg msg) {
		list.add(msg);
		notifyDataSetChanged();
	}
	
	public MessageChatAdapter(Context context, List<BmobMsg> msgList) {
		this.context = context;
		this.list = msgList;
		
		curUserId = BmobUserManager.getInstance(context).getCurrentUserObjectId();
	}

	@Override
	public int getItemViewType(int position) {
		BmobMsg msg = list.get(position);
		// msgType 1-text,2-img,3-location,4-voice
		int msgType = msg.getMsgType();
		// ��Ϣ����id����ǵ�ǰ�û�,������ϢΪsend����,����Ϊreceiver����
		boolean isSend = msg.getBelongId().equals(curUserId);
		// ż��Ϊsend����,����Ϊreceive����
		int type = msgType * 2 - (isSend ? 2 : 1);
		return type;
	}

	@Override
	public int getViewTypeCount() {
		return 8;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public BmobMsg getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	ViewHolder holder;
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BmobMsg msg = getItem(position);
		// ��Ϣ����id����ǵ�ǰ�û�,������ϢΪsend����,����Ϊreceiver����
		final boolean isSend = msg.getBelongId().equals(curUserId);
		
		// ���Ĳ��ֲ�һ��,�����е����ݶ�һ��
		if (isSend) {
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(context, R.layout.item_chat_sent, null);
				findViewById(convertView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
		} else {
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(context, R.layout.item_chat_received, null);
				findViewById(convertView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
		}
		
		final BmobMsg item = getItem(position);
		// ���ͷ������������
		String avatar = item.getBelongAvatar();
		ImageLoader.getInstance().displayImage(avatar, holder.iv_avatar, 
				ImageOptHelper.getAvatarOptions());
		holder.iv_avatar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, UserInfoActivity.class);
				if(isSend) {
					intent.putExtra("from", "me");
				} else {
					intent.putExtra("from", "other");
					intent.putExtra("username", item.getBelongUsername());
				}
				context.startActivity(intent);
			}
		});

		// bmob��ʱ�������Ϊ��λ
		holder.tv_time.setText(DateUtils.formatTime(Long.parseLong(item.getMsgTime())));

		// ����Ƿ�������Ҫ����ͬ����״̬����,����ͼƬ��������
		if(isSend && getItemViewType(position) != TYPE_SEND_IMAGE) {
			switch (item.getStatus()) {
			case BmobConfig.STATUS_SEND_START:// ��ʼ����
				Log.i("adapter", "��ʼ���� " + item.getContent());
				
				holder.progress_load.setVisibility(View.VISIBLE);
				holder.iv_fail_resend.setVisibility(View.GONE);
				holder.tv_send_status.setVisibility(View.GONE);
				break;
			case BmobConfig.STATUS_SEND_FAIL:// ����������Ӧ���߲�ѯʧ�ܵ�ԭ����ɵķ���ʧ�ܣ�����Ҫ�ط�
				Log.i("adapter", "����ʧ�� " + item.getContent());
				
				holder.progress_load.setVisibility(View.GONE);
				holder.iv_fail_resend.setVisibility(View.VISIBLE);
				holder.tv_send_status.setVisibility(View.GONE);
				break;
			case BmobConfig.STATUS_SEND_SUCCESS:// ���ͳɹ�
				Log.i("adapter", "���ͳɹ�" + item.getContent());
				
				holder.progress_load.setVisibility(View.GONE);
				holder.iv_fail_resend.setVisibility(View.GONE);
				holder.tv_send_status.setVisibility(View.VISIBLE);
				
				holder.tv_send_status.setText("�ѷ���");
				break;
			case BmobConfig.STATUS_SEND_RECEIVERED:// �Է��ѽ��յ�
				Log.i("adapter", "�Է��ѽ���" + item.getContent());
				
				holder.progress_load.setVisibility(View.GONE);
				holder.iv_fail_resend.setVisibility(View.GONE);
				holder.tv_send_status.setVisibility(View.VISIBLE);
				
				holder.tv_send_status.setText("���Ķ�");
				break;
			}
		}
		
		// ����,����ʧ�ܵ�,���Ե���ط�
		if(isSend && item.getStatus() == BmobConfig.STATUS_SEND_FAIL) {
			// �ط��ص�
			holder.iv_fail_resend.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(onItemMultiClickListener != null) {
						onItemMultiClickListener.onItemResendClick(item);
					}
				}
			});
		}
		
		// ����������ʾ����
		final String content = item.getContent();
		switch (item.getMsgType()) {
		case BmobConfig.TYPE_TEXT:
			holder.item_chat_message.setVisibility(View.VISIBLE);
			holder.tv_message.setText(EmotionUtils.getEmotionContent(context, holder.tv_message, content));
			break;
		case BmobConfig.TYPE_IMAGE:// ͼƬ��
			holder.item_chat_image.setVisibility(View.VISIBLE);
			if (!TextUtils.isEmpty(content)) {// ���ͳɹ�֮��洢��ͼƬ���͵�content�ͽ��յ����ǲ�һ����
				dealWithImage(position, 
						holder.progress_load, 
						holder.iv_fail_resend, 
						holder.tv_send_status, 
						holder.iv_picture, 
						item);
			}
			break;
		case BmobConfig.TYPE_VOICE:// ������Ϣ
			holder.item_chat_voice.setVisibility(View.VISIBLE);
			// �����ǰ���ڲ�������,�����ڲ���,�����ڲ��ŵ��ǵ�ǰitem��Ӧ������,�������ʾ����
			if(NewRecordPlayClickListener.currentMsg != null 
					&& NewRecordPlayClickListener.isPlaying
					&& NewRecordPlayClickListener.currentMsg.hashCode() == item.hashCode()) {
				holder.iv_voice.setImageResource(isSend ? R.anim.anim_chat_voice_right
						: R.anim.anim_chat_voice_left);
				AnimationDrawable anim = (AnimationDrawable) holder.iv_voice.getDrawable();
				if (anim != null) {
					anim.start();
				}
			} else {
				holder.iv_voice.setImageResource(isSend ? R.drawable.voice_right_3 : R.drawable.voice_left_3);
			}
			
			// ���������ļ�
			holder.item_chat_voice.setOnClickListener(new NewRecordPlayClickListener(
					context, msg, holder.iv_voice, isSend));
			
			if (!TextUtils.isEmpty(content)) {
				// �ж���Ƶ�ļ��Ƿ��Ѿ����ر������ļ���
				boolean isExists = BmobDownloadManager.checkTargetPathExist(curUserId, item);
				if (!isSend && !isExists) {
					// �ǽ��շ�,����Ƶ�ļ�δ���ع�,��ʼ����֮
					downloadAudio(item);
				} else {
					// �������,�����͵���Ƶ�����Ѿ����ص���Ƶֱ����ʾ��������
					String[] voiceStrs = content.split("&");
					String length = voiceStrs[voiceStrs.length - 1];
					holder.tv_voice_length.setText(length + "\''");
					setAudioWidth(length);
				}
			}
			break;
		default:
			break;
		}
		
		holder.ll_msg_body.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(onItemMultiClickListener != null) {
					onItemMultiClickListener.onItemClick(item);
				}
			}
		});
		
		return convertView;
	}
	
	private void setAudioWidth(String secondStr) {
		try {
			int seconds = Integer.parseInt(secondStr);
			int width = seconds * 50;
			// min width
			if(width < DisplayUtils.dp2px(context, 50)) {
				width = DisplayUtils.dp2px(context, 50);
			}
			holder.item_chat_voice.getLayoutParams().width = width;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ������Ƶ
	 */
	private void downloadAudio(final BmobMsg item) {
		String netUrl = item.getContent().split("&")[0];
		final String length = item.getContent().split("&")[1];
		// ��ʼ��������
		BmobDownloadManager downloadTask = new BmobDownloadManager(context, item, 
				new DownloadListener() {

			@Override
			public void onStart() {
				holder.progress_load.setVisibility(View.VISIBLE);
				holder.tv_voice_length.setVisibility(View.GONE);
				holder.iv_voice.setVisibility(View.GONE);
			}

			@Override
			public void onSuccess() {
				holder.progress_load.setVisibility(View.GONE);
				holder.tv_voice_length.setVisibility(View.VISIBLE);
				holder.tv_voice_length.setText(length + "\''");
				// ֻ��������ɲ���ʾ���ŵİ�ť
				holder.iv_voice.setVisibility(View.VISIBLE);
			}

			@Override
			public void onError(String error) {
				holder.progress_load.setVisibility(View.GONE);
				holder.tv_voice_length.setVisibility(View.GONE);
				holder.iv_voice.setVisibility(View.GONE);
			}
		});
		downloadTask.execute(netUrl);
	}

	/**
	 * ����ͼƬ
	 */
	private void dealWithImage(int position, final ProgressBar progress_load, ImageView iv_fail_resend, 
			TextView tv_send_status, ImageView iv_picture, BmobMsg item) {
		String text = item.getContent();
		if (getItemViewType(position) == TYPE_SEND_IMAGE) {// ���͵���Ϣ
			if (item.getStatus() == BmobConfig.STATUS_SEND_START) {
				progress_load.setVisibility(View.VISIBLE);
				iv_fail_resend.setVisibility(View.GONE);
				tv_send_status.setVisibility(View.GONE);
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_SUCCESS) {
				progress_load.setVisibility(View.GONE);
				iv_fail_resend.setVisibility(View.GONE);
				tv_send_status.setVisibility(View.VISIBLE);
				tv_send_status.setText("�ѷ���");
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_FAIL) {
				progress_load.setVisibility(View.GONE);
				iv_fail_resend.setVisibility(View.VISIBLE);
				tv_send_status.setVisibility(View.GONE);
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_RECEIVERED) {
				progress_load.setVisibility(View.GONE);
				iv_fail_resend.setVisibility(View.GONE);
				tv_send_status.setVisibility(View.VISIBLE);
				tv_send_status.setText("���Ķ�");
			}
			
			// ����Ƿ��͵�ͼƬ�Ļ�����Ϊ��ʼ���ʹ洢�ĵ�ַ�Ǳ��ص�ַ�����ͳɹ�֮��洢���Ǳ��ص�ַ+"&"+�����ַ�������Ҫ�ж���
			String showUrl = "";
			if (text.contains("&")) {
				showUrl = text.split("&")[0];
			} else {
				showUrl = text;
			}
			// Ϊ�˷���ÿ�ζ���ȡ����ͼƬ��ʾ
			ImageLoader.getInstance().displayImage(showUrl, iv_picture);
		} else {
			ImageLoader.getInstance().displayImage(text, iv_picture, new ImageLoadingListener() {

				@Override
				public void onLoadingStarted(String imageUri, View view) {
					progress_load.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadingFailed(String imageUri, View view,
						FailReason failReason) {
					progress_load.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					progress_load.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingCancelled(String imageUri, View view) {
					progress_load.setVisibility(View.GONE);
				}
			});
		}
	}

	private void findViewById(View convertView) {
		holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
		holder.iv_fail_resend = (ImageView) convertView.findViewById(R.id.iv_fail_resend);
		holder.tv_send_status = (TextView) convertView.findViewById(R.id.tv_send_status);
		holder.progress_load = (ProgressBar) convertView.findViewById(R.id.progress_load);
		holder.ll_msg_body = (LinearLayout) convertView.findViewById(R.id.ll_msg_body);
		holder.item_chat_image = convertView.findViewById(R.id.item_chat_image);
		holder.iv_picture = (ImageView) convertView.findViewById(R.id.iv_picture);
		holder.item_chat_message = convertView.findViewById(R.id.item_chat_message);
		holder.tv_message = (TextView) convertView.findViewById(R.id.tv_message);
		holder.item_chat_voice = convertView.findViewById(R.id.item_chat_voice);
		holder.iv_voice = (ImageView) convertView.findViewById(R.id.iv_voice);
		holder.tv_voice_length = (TextView) convertView.findViewById(R.id.tv_voice_length);
		holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
	}
	
	static class ViewHolder {
		public TextView tv_time;
		public ImageView iv_fail_resend;
		public TextView tv_send_status;
		public ProgressBar progress_load;
		public LinearLayout ll_msg_body;
		public View item_chat_image;
		public ImageView iv_picture;
		public View item_chat_message;
		public TextView tv_message;
		public View item_chat_voice;
		public ImageView iv_voice;
		public TextView tv_voice_length;
		public ImageView iv_avatar;
	}
	
	
	private OnItemMultiClickListener onItemMultiClickListener;
	
	public void setOnItemMultiClickListener(
			OnItemMultiClickListener onItemMultiClickListener) {
		this.onItemMultiClickListener = onItemMultiClickListener;
	}

	public interface OnItemMultiClickListener {
		/**
		 * ����item�����
		 */
		void onItemClick(BmobMsg msg);
		/**
		 * �ط���ť�����
		 */
		void onItemResendClick(BmobMsg msg);
	}

}
