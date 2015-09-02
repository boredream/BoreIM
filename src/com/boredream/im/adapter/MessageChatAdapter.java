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
 * 聊天适配器
 */
public class MessageChatAdapter extends BaseAdapter {

	// 文本
	public final int TYPE_SEND_TXT = 0;
	public final int TYPE_RECEIVER_TXT = 1;
	// 图片
	public final int TYPE_SEND_IMAGE = 2;
	public final int TYPE_RECEIVER_IMAGE = 3;
	// 地理位置
	public final int TYPE_SEND_LOCATION = 4;
	public final int TYPE_RECEIVER_LOCATION = 5;
	// 语音
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
		// 信息所属id如果是当前用户,即该信息为send发送,否则为receiver接收
		boolean isSend = msg.getBelongId().equals(curUserId);
		// 偶数为send发送,奇数为receive接收
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
		// 信息所属id如果是当前用户,即该信息为send发送,否则为receiver接收
		final boolean isSend = msg.getBelongId().equals(curUserId);
		
		// 填充的布局不一致,布局中的内容都一样
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
		// 点击头像进入个人资料
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

		// bmob上时间戳是秒为单位
		holder.tv_time.setText(DateUtils.formatTime(Long.parseLong(item.getMsgTime())));

		// 如果是发送则需要处理不同发送状态问题,其中图片单独处理
		if(isSend && getItemViewType(position) != TYPE_SEND_IMAGE) {
			switch (item.getStatus()) {
			case BmobConfig.STATUS_SEND_START:// 开始发送
				Log.i("adapter", "开始发送 " + item.getContent());
				
				holder.progress_load.setVisibility(View.VISIBLE);
				holder.iv_fail_resend.setVisibility(View.GONE);
				holder.tv_send_status.setVisibility(View.GONE);
				break;
			case BmobConfig.STATUS_SEND_FAIL:// 服务器无响应或者查询失败等原因造成的发送失败，均需要重发
				Log.i("adapter", "发送失败 " + item.getContent());
				
				holder.progress_load.setVisibility(View.GONE);
				holder.iv_fail_resend.setVisibility(View.VISIBLE);
				holder.tv_send_status.setVisibility(View.GONE);
				break;
			case BmobConfig.STATUS_SEND_SUCCESS:// 发送成功
				Log.i("adapter", "发送成功" + item.getContent());
				
				holder.progress_load.setVisibility(View.GONE);
				holder.iv_fail_resend.setVisibility(View.GONE);
				holder.tv_send_status.setVisibility(View.VISIBLE);
				
				holder.tv_send_status.setText("已发送");
				break;
			case BmobConfig.STATUS_SEND_RECEIVERED:// 对方已接收到
				Log.i("adapter", "对方已接收" + item.getContent());
				
				holder.progress_load.setVisibility(View.GONE);
				holder.iv_fail_resend.setVisibility(View.GONE);
				holder.tv_send_status.setVisibility(View.VISIBLE);
				
				holder.tv_send_status.setText("已阅读");
				break;
			}
		}
		
		// 发送,且是失败的,可以点击重发
		if(isSend && item.getStatus() == BmobConfig.STATUS_SEND_FAIL) {
			// 重发回调
			holder.iv_fail_resend.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(onItemMultiClickListener != null) {
						onItemMultiClickListener.onItemResendClick(item);
					}
				}
			});
		}
		
		// 根据类型显示内容
		final String content = item.getContent();
		switch (item.getMsgType()) {
		case BmobConfig.TYPE_TEXT:
			holder.item_chat_message.setVisibility(View.VISIBLE);
			holder.tv_message.setText(EmotionUtils.getEmotionContent(context, holder.tv_message, content));
			break;
		case BmobConfig.TYPE_IMAGE:// 图片类
			holder.item_chat_image.setVisibility(View.VISIBLE);
			if (!TextUtils.isEmpty(content)) {// 发送成功之后存储的图片类型的content和接收到的是不一样的
				dealWithImage(position, 
						holder.progress_load, 
						holder.iv_fail_resend, 
						holder.tv_send_status, 
						holder.iv_picture, 
						item);
			}
			break;
		case BmobConfig.TYPE_VOICE:// 语音消息
			holder.item_chat_voice.setVisibility(View.VISIBLE);
			// 如果当前正在播放语音,且正在播放,且正在播放的是当前item对应的数据,则继续显示动画
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
			
			// 播放语音文件
			holder.item_chat_voice.setOnClickListener(new NewRecordPlayClickListener(
					context, msg, holder.iv_voice, isSend));
			
			if (!TextUtils.isEmpty(content)) {
				// 判断音频文件是否已经下载保存至文件中
				boolean isExists = BmobDownloadManager.checkTargetPathExist(curUserId, item);
				if (!isSend && !isExists) {
					// 是接收方,且音频文件未下载过,则开始下载之
					downloadAudio(item);
				} else {
					// 其他情况,即发送的音频或者已经下载的音频直接显示语音长度
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
	 * 下载音频
	 */
	private void downloadAudio(final BmobMsg item) {
		String netUrl = item.getContent().split("&")[0];
		final String length = item.getContent().split("&")[1];
		// 开始下载任务
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
				// 只有下载完成才显示播放的按钮
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
	 * 处理图片
	 */
	private void dealWithImage(int position, final ProgressBar progress_load, ImageView iv_fail_resend, 
			TextView tv_send_status, ImageView iv_picture, BmobMsg item) {
		String text = item.getContent();
		if (getItemViewType(position) == TYPE_SEND_IMAGE) {// 发送的消息
			if (item.getStatus() == BmobConfig.STATUS_SEND_START) {
				progress_load.setVisibility(View.VISIBLE);
				iv_fail_resend.setVisibility(View.GONE);
				tv_send_status.setVisibility(View.GONE);
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_SUCCESS) {
				progress_load.setVisibility(View.GONE);
				iv_fail_resend.setVisibility(View.GONE);
				tv_send_status.setVisibility(View.VISIBLE);
				tv_send_status.setText("已发送");
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_FAIL) {
				progress_load.setVisibility(View.GONE);
				iv_fail_resend.setVisibility(View.VISIBLE);
				tv_send_status.setVisibility(View.GONE);
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_RECEIVERED) {
				progress_load.setVisibility(View.GONE);
				iv_fail_resend.setVisibility(View.GONE);
				tv_send_status.setVisibility(View.VISIBLE);
				tv_send_status.setText("已阅读");
			}
			
			// 如果是发送的图片的话，因为开始发送存储的地址是本地地址，发送成功之后存储的是本地地址+"&"+网络地址，因此需要判断下
			String showUrl = "";
			if (text.contains("&")) {
				showUrl = text.split("&")[0];
			} else {
				showUrl = text;
			}
			// 为了方便每次都是取本地图片显示
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
		 * 整个item被点击
		 */
		void onItemClick(BmobMsg msg);
		/**
		 * 重发按钮被点击
		 */
		void onItemResendClick(BmobMsg msg);
	}

}
