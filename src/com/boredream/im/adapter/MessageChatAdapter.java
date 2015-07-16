package com.boredream.im.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * 聊天适配器
 * 
 * @ClassName: MessageChatAdapter
 * @Description: TODO
 * @author smile
 * @date 2014-5-28 下午5:34:07
 */
public class MessageChatAdapter extends BaseAdapter {

	// 8种Item的类型
	// 文本
	private final int TYPE_SEND_TXT = 0;
	private final int TYPE_RECEIVER_TXT = 1;
	// 图片
	private final int TYPE_SEND_IMAGE = 2;
	private final int TYPE_RECEIVER_IMAGE = 3;
	// 位置
	private final int TYPE_SEND_LOCATION = 4;
	private final int TYPE_RECEIVER_LOCATION = 5;
	// 语音
	private final int TYPE_SEND_VOICE = 6;
	private final int TYPE_RECEIVER_VOICE = 7;

	private Context context;
	private List<BmobMsg> list;
	
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
	
	private String curUserId;

	public MessageChatAdapter(Context context, List<BmobMsg> msgList) {
		this.context = context;
		this.list = msgList;
		curUserId = BmobUserManager.getInstance(context).getCurrentUserObjectId();
	}

	@Override
	public int getItemViewType(int position) {
		BmobMsg msg = list.get(position);
		// msgType 1-text,2-img,3-locaion,4-voice
		int msgType = msg.getMsgType();
		// 信息所属id如果是当前用户,即该信息为send发送,否则为receiver接收
		boolean isSend = msg.getBelongId().equals(curUserId);
		
		return msgType * 2 - (isSend ? 2 : 1);
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
		boolean isSend = msg.getBelongId().equals(curUserId);
		
//		ViewHolder receiverHolder;
		
		if (isSend) {
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(context, R.layout.item_chat_sent, null);
				holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
				holder.iv_fail_resend = (ImageView) convertView.findViewById(R.id.iv_fail_resend);
				holder.tv_send_status = (TextView) convertView.findViewById(R.id.tv_send_status);
				holder.progress_load = (ProgressBar) convertView.findViewById(R.id.progress_load);
				holder.iv_picture = (ImageView) convertView.findViewById(R.id.iv_picture);
				holder.layout_location = (LinearLayout) convertView.findViewById(R.id.layout_location);
				holder.tv_location = (TextView) convertView.findViewById(R.id.tv_location);
				holder.tv_message = (TextView) convertView.findViewById(R.id.tv_message);
				holder.layout_voice = (LinearLayout) convertView.findViewById(R.id.layout_voice);
				holder.iv_voice = (ImageView) convertView.findViewById(R.id.iv_voice);
				holder.tv_voice_length = (TextView) convertView.findViewById(R.id.tv_voice_length);
				holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
		} else {
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(context, R.layout.item_chat_received, null);
				holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
				holder.iv_fail_resend = (ImageView) convertView.findViewById(R.id.iv_fail_resend);
				holder.tv_send_status = (TextView) convertView.findViewById(R.id.tv_send_status);
				holder.progress_load = (ProgressBar) convertView.findViewById(R.id.progress_load);
				holder.iv_picture = (ImageView) convertView.findViewById(R.id.iv_picture);
				holder.layout_location = (LinearLayout) convertView.findViewById(R.id.layout_location);
				holder.tv_location = (TextView) convertView.findViewById(R.id.tv_location);
				holder.tv_message = (TextView) convertView.findViewById(R.id.tv_message);
				holder.layout_voice = (LinearLayout) convertView.findViewById(R.id.layout_voice);
				holder.iv_voice = (ImageView) convertView.findViewById(R.id.iv_voice);
				holder.tv_voice_length = (TextView) convertView.findViewById(R.id.tv_voice_length);
				holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
		}
		
		BmobMsg item = getItem(position);
		// 点击头像进入个人资料
		String avatar = item.getBelongAvatar();
		ImageLoader.getInstance().displayImage(avatar, holder.iv_avatar);

//		tv_time.setText(TimeUtil.getChatTime(Long.parseLong(item.getMsgTime())));
		holder.tv_time.setText(item.getMsgTime());

		if (getItemViewType(position) == TYPE_SEND_TXT
				// ||getItemViewType(position)==TYPE_SEND_IMAGE//图片单独处理
				|| getItemViewType(position) == TYPE_SEND_LOCATION
				|| getItemViewType(position) == TYPE_SEND_VOICE) {// 只有自己发送的消息才有重发机制
			// 状态描述
			if (item.getStatus() == BmobConfig.STATUS_SEND_SUCCESS) {// 发送成功
				holder.progress_load.setVisibility(View.INVISIBLE);
				holder.iv_fail_resend.setVisibility(View.INVISIBLE);
				if (item.getMsgType() == BmobConfig.TYPE_VOICE) {
					holder.tv_send_status.setVisibility(View.GONE);
					holder.tv_voice_length.setVisibility(View.VISIBLE);
				} else {
					holder.tv_send_status.setVisibility(View.VISIBLE);
					holder.tv_send_status.setText("已发送");
				}
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_FAIL) {// 服务器无响应或者查询失败等原因造成的发送失败，均需要重发
				holder.progress_load.setVisibility(View.INVISIBLE);
				holder.iv_fail_resend.setVisibility(View.VISIBLE);
				holder.tv_send_status.setVisibility(View.INVISIBLE);
				if (item.getMsgType() == BmobConfig.TYPE_VOICE) {
					holder.tv_voice_length.setVisibility(View.GONE);
				}
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_RECEIVERED) {// 对方已接收到
				holder.progress_load.setVisibility(View.INVISIBLE);
				holder.iv_fail_resend.setVisibility(View.INVISIBLE);
				if (item.getMsgType() == BmobConfig.TYPE_VOICE) {
					holder.tv_send_status.setVisibility(View.GONE);
					holder.tv_voice_length.setVisibility(View.VISIBLE);
				} else {
					holder.tv_send_status.setVisibility(View.VISIBLE);
					holder.tv_send_status.setText("已阅读");
				}
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_START) {// 开始上传
				holder.progress_load.setVisibility(View.VISIBLE);
				holder.iv_fail_resend.setVisibility(View.INVISIBLE);
				holder.tv_send_status.setVisibility(View.INVISIBLE);
				if (item.getMsgType() == BmobConfig.TYPE_VOICE) {
					holder.tv_voice_length.setVisibility(View.GONE);
				}
			}
		}
		
		// 根据类型显示内容
		final String text = item.getContent();
		switch (item.getMsgType()) {
		case BmobConfig.TYPE_TEXT:
			holder.tv_message.setText(text);
			break;
		case BmobConfig.TYPE_IMAGE:// 图片类
			if (text != null && !text.equals("")) {// 发送成功之后存储的图片类型的content和接收到的是不一样的
				dealWithImage(position, 
						holder.progress_load, 
						holder.iv_fail_resend, 
						holder.tv_send_status, 
						holder.iv_picture, 
						item);
			}
			break;
		case BmobConfig.TYPE_LOCATION:// 位置信息
			if (text != null && !text.equals("")) {
				String address = text.split("&")[0];
				final String latitude = text.split("&")[1];// 维度
				final String longtitude = text.split("&")[2];// 经度
				holder.tv_location.setText(address);
			}
			break;
		case BmobConfig.TYPE_VOICE:// 语音消息
			if (text != null && !text.equals("")) {
				holder.tv_voice_length.setVisibility(View.VISIBLE);
				String content = item.getContent();
				if (item.getBelongId().equals(curUserId)) {// 发送的消息
					if (item.getStatus() == BmobConfig.STATUS_SEND_RECEIVERED
							|| item.getStatus() == BmobConfig.STATUS_SEND_SUCCESS) {// 当发送成功或者发送已阅读的时候，则显示语音长度
						holder.tv_voice_length.setVisibility(View.VISIBLE);
						String length = content.split("&")[2];
						holder.tv_voice_length.setText(length + "\''");
					} else {
						holder.tv_voice_length.setVisibility(View.INVISIBLE);
					}
				} else {// 收到的消息
					boolean isExists = BmobDownloadManager.checkTargetPathExist(curUserId, item);
					if (!isExists) {// 若指定格式的录音文件不存在，则需要下载，因为其文件比较小，故放在此下载
						String netUrl = content.split("&")[0];
						final String length = content.split("&")[1];
						BmobDownloadManager downloadTask = new BmobDownloadManager(context, item, 
								new DownloadListener() {

							@Override
							public void onStart() {
								// TODO Auto-generated method stub
								holder.progress_load.setVisibility(View.VISIBLE);
								holder.tv_voice_length.setVisibility(View.GONE);
								holder.iv_voice.setVisibility(View.INVISIBLE);// 只有下载完成才显示播放的按钮
							}

							@Override
							public void onSuccess() {
								// TODO Auto-generated method stub
								holder.progress_load.setVisibility(View.GONE);
								holder.tv_voice_length.setVisibility(View.VISIBLE);
								holder.tv_voice_length.setText(length + "\''");
								holder.iv_voice.setVisibility(View.VISIBLE);
							}

							@Override
							public void onError(String error) {
								// TODO Auto-generated method stub
								holder.progress_load.setVisibility(View.GONE);
								holder.tv_voice_length.setVisibility(View.GONE);
								holder.iv_voice.setVisibility(View.INVISIBLE);
							}
						});
						downloadTask.execute(netUrl);
					} else {
						String length = content.split("&")[2];
						holder.tv_voice_length.setText(length + "\''");
					}
				}
			}
			// 播放语音文件
//			iv_voice.setOnClickListener(new NewRecordPlayClickListener(mContext, item, iv_voice));
			break;
		default:
			break;
		}
		
		return convertView;
	}
	
	static class ViewHolder {
		public TextView tv_time;
		public ImageView iv_fail_resend;
		public TextView tv_send_status;
		public ProgressBar progress_load;
		public ImageView iv_picture;
		public LinearLayout layout_location;
		public TextView tv_location;
		public TextView tv_message;
		public LinearLayout layout_voice;
		public ImageView iv_voice;
		public TextView tv_voice_length;
		public ImageView iv_avatar;
	}
	
	static class ViewHolderSend extends ViewHolder {
		
	}
	
	static class ViewHolderReceiver extends ViewHolder {
		
	}

	/**
	 * 获取图片的地址
	 * 
	 * @Description: TODO
	 * @param @param item
	 * @param @return
	 * @return String
	 * @throws
	 */
	private String getImageUrl(BmobMsg item) {
		String showUrl = "";
		String text = item.getContent();
		if (item.getBelongId().equals(curUserId)) {//
			if (text.contains("&")) {
				showUrl = text.split("&")[0];
			} else {
				showUrl = text;
			}
		} else {// 如果是收到的消息，则需要从网络下载
			showUrl = text;
		}
		return showUrl;
	}

	/**
	 * 处理图片
	 * 
	 * @Description: TODO
	 * @param @param position
	 * @param @param progress_load
	 * @param @param iv_fail_resend
	 * @param @param tv_send_status
	 * @param @param iv_picture
	 * @param @param item
	 * @return void
	 * @throws
	 */
	private void dealWithImage(int position, final ProgressBar progress_load, ImageView iv_fail_resend, TextView tv_send_status, ImageView iv_picture, BmobMsg item) {
		String text = item.getContent();
		if (getItemViewType(position) == TYPE_SEND_IMAGE) {// 发送的消息
			if (item.getStatus() == BmobConfig.STATUS_SEND_START) {
				progress_load.setVisibility(View.VISIBLE);
				iv_fail_resend.setVisibility(View.INVISIBLE);
				tv_send_status.setVisibility(View.INVISIBLE);
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_SUCCESS) {
				progress_load.setVisibility(View.INVISIBLE);
				iv_fail_resend.setVisibility(View.INVISIBLE);
				tv_send_status.setVisibility(View.VISIBLE);
				tv_send_status.setText("已发送");
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_FAIL) {
				progress_load.setVisibility(View.INVISIBLE);
				iv_fail_resend.setVisibility(View.VISIBLE);
				tv_send_status.setVisibility(View.INVISIBLE);
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_RECEIVERED) {
				progress_load.setVisibility(View.INVISIBLE);
				iv_fail_resend.setVisibility(View.INVISIBLE);
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
					// TODO Auto-generated method stub
					progress_load.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadingFailed(String imageUri, View view,
						FailReason failReason) {
					// TODO Auto-generated method stub
					progress_load.setVisibility(View.INVISIBLE);
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					// TODO Auto-generated method stub
					progress_load.setVisibility(View.INVISIBLE);
				}

				@Override
				public void onLoadingCancelled(String imageUri, View view) {
					// TODO Auto-generated method stub
					progress_load.setVisibility(View.INVISIBLE);
				}
			});
		}
	}

}
