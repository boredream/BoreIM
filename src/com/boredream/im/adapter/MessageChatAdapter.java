package com.boredream.im.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.SpannableString;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.bmob.im.BmobDownloadManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.inteface.DownloadListener;

import com.boredream.im.R;
import com.boredream.im.adapter.ChatUserAdapter.ViewHolder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BmobMsg msg = getItem(position);
		// 信息所属id如果是当前用户,即该信息为send发送,否则为receiver接收
		boolean isSend = msg.getBelongId().equals(curUserId);
		int layoutId = isSend ? R.layout.item_chat_sent : R.layout.item_chat_received;
		
		// 填充布局
		ViewHolder sendHolder = null;
		ViewHolder receiverHolder = null;
		
		ViewHolder holder = isSend ? sendHolder : receiverHolder;
		
		if(convertView == null || convertView.getTag() ) {
			
		}
		
		if(convertView != null && (holder=(ViewHolder) convertView.getTag()) != null) {
			// do nothing
		} else {
			holder = new ViewHolder();
			convertView = View.inflate(context, layoutId, null);
			holder.iv_friend_avatar = (ImageView) convertView.findViewById(R.id.iv_friend_avatar);
			convertView.setTag(holder);
		}
		
//		if(isSend) {
//			if(convertView == null) {
//				sendHolder = new ViewHolder();
//				convertView = View.inflate(context, layoutId , null);
//				sendHolder.iv_friend_avatar = (ImageView) convertView.findViewById(R.id.iv_friend_avatar);
//				convertView.setTag(sendHolder);
//			} else {
//				sendHolder = (ViewHolder) convertView.getTag();
//			}
//		} else {
//			if(convertView == null) {
//				receiverHolder = new ViewHolder();
//				convertView = View.inflate(context, layoutId , null);
//				receiverHolder.iv_friend_avatar = (ImageView) convertView.findViewById(R.id.iv_friend_avatar);
//				convertView.setTag(receiverHolder);
//			} else {
//				receiverHolder = (ViewHolder) convertView.getTag();
//			}
//		}
		
		return convertView;
	}
	
	static class ViewHolder {
		
	}

	private View createViewByType(Context context, BmobMsg msg, int position) {
		// 信息所属id如果是当前用户,即该信息为send发送,否则为receiver接收
		boolean isSend = msg.getBelongId().equals(curUserId);
		int layoutId = isSend ? R.layout.item_chat_sent : R.layout.item_chat_received;
		return View.inflate(context, layoutId , null);
	}

	@Override
	public View bindView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final BmobMsg item = list.get(position);
		if (convertView == null) {
			convertView = createViewByType(item, position);
		}
		// 文本类型
		ImageView iv_avatar = ViewHolder.get(convertView, R.id.iv_avatar);
		final ImageView iv_fail_resend = ViewHolder.get(convertView, R.id.iv_fail_resend);// 失败重发
		final TextView tv_send_status = ViewHolder.get(convertView, R.id.tv_send_status);// 发送状态
		TextView tv_time = ViewHolder.get(convertView, R.id.tv_time);
		TextView tv_message = ViewHolder.get(convertView, R.id.tv_message);
		// 图片
		ImageView iv_picture = ViewHolder.get(convertView, R.id.iv_picture);
		final ProgressBar progress_load = ViewHolder.get(convertView, R.id.progress_load);// 进度条
		// 位置
		TextView tv_location = ViewHolder.get(convertView, R.id.tv_location);
		// 语音
		final ImageView iv_voice = ViewHolder.get(convertView, R.id.iv_voice);
		// 语音长度
		final TextView tv_voice_length = ViewHolder.get(convertView, R.id.tv_voice_length);

		// 点击头像进入个人资料
		String avatar = item.getBelongAvatar();
		if (avatar != null && !avatar.equals("")) {// 加载头像-为了不每次都加载头像
			ImageLoader.getInstance().displayImage(avatar, iv_avatar, ImageLoadOptions.getOptions(), animateFirstListener);
		} else {
			iv_avatar.setImageResource(R.drawable.head);
		}

		iv_avatar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, SetMyInfoActivity.class);
				if (getItemViewType(position) == TYPE_RECEIVER_TXT
						|| getItemViewType(position) == TYPE_RECEIVER_IMAGE
						|| getItemViewType(position) == TYPE_RECEIVER_LOCATION
						|| getItemViewType(position) == TYPE_RECEIVER_VOICE) {
					intent.putExtra("from", "other");
					intent.putExtra("username", item.getBelongUsername());
				} else {
					intent.putExtra("from", "me");
				}
				mContext.startActivity(intent);
			}
		});

		tv_time.setText(TimeUtil.getChatTime(Long.parseLong(item.getMsgTime())));

		if (getItemViewType(position) == TYPE_SEND_TXT
				// ||getItemViewType(position)==TYPE_SEND_IMAGE//图片单独处理
				|| getItemViewType(position) == TYPE_SEND_LOCATION
				|| getItemViewType(position) == TYPE_SEND_VOICE) {// 只有自己发送的消息才有重发机制
			// 状态描述
			if (item.getStatus() == BmobConfig.STATUS_SEND_SUCCESS) {// 发送成功
				progress_load.setVisibility(View.INVISIBLE);
				iv_fail_resend.setVisibility(View.INVISIBLE);
				if (item.getMsgType() == BmobConfig.TYPE_VOICE) {
					tv_send_status.setVisibility(View.GONE);
					tv_voice_length.setVisibility(View.VISIBLE);
				} else {
					tv_send_status.setVisibility(View.VISIBLE);
					tv_send_status.setText("已发送");
				}
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_FAIL) {// 服务器无响应或者查询失败等原因造成的发送失败，均需要重发
				progress_load.setVisibility(View.INVISIBLE);
				iv_fail_resend.setVisibility(View.VISIBLE);
				tv_send_status.setVisibility(View.INVISIBLE);
				if (item.getMsgType() == BmobConfig.TYPE_VOICE) {
					tv_voice_length.setVisibility(View.GONE);
				}
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_RECEIVERED) {// 对方已接收到
				progress_load.setVisibility(View.INVISIBLE);
				iv_fail_resend.setVisibility(View.INVISIBLE);
				if (item.getMsgType() == BmobConfig.TYPE_VOICE) {
					tv_send_status.setVisibility(View.GONE);
					tv_voice_length.setVisibility(View.VISIBLE);
				} else {
					tv_send_status.setVisibility(View.VISIBLE);
					tv_send_status.setText("已阅读");
				}
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_START) {// 开始上传
				progress_load.setVisibility(View.VISIBLE);
				iv_fail_resend.setVisibility(View.INVISIBLE);
				tv_send_status.setVisibility(View.INVISIBLE);
				if (item.getMsgType() == BmobConfig.TYPE_VOICE) {
					tv_voice_length.setVisibility(View.GONE);
				}
			}
		}
		// 根据类型显示内容
		final String text = item.getContent();
		switch (item.getMsgType()) {
		case BmobConfig.TYPE_TEXT:
			try {
				SpannableString spannableString = FaceTextUtils
						.toSpannableString(mContext, text);
				tv_message.setText(spannableString);
			} catch (Exception e) {
			}
			break;

		case BmobConfig.TYPE_IMAGE:// 图片类
			try {
				if (text != null && !text.equals("")) {// 发送成功之后存储的图片类型的content和接收到的是不一样的
					dealWithImage(position, progress_load, iv_fail_resend, tv_send_status, iv_picture, item);
				}
				iv_picture.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(mContext, ImageBrowserActivity.class);
						ArrayList<String> photos = new ArrayList<String>();
						photos.add(getImageUrl(item));
						intent.putStringArrayListExtra("photos", photos);
						intent.putExtra("position", 0);
						mContext.startActivity(intent);
					}
				});

			} catch (Exception e) {
			}
			break;

		case BmobConfig.TYPE_LOCATION:// 位置信息
			try {
				if (text != null && !text.equals("")) {
					String address = text.split("&")[0];
					final String latitude = text.split("&")[1];// 维度
					final String longtitude = text.split("&")[2];// 经度
					tv_location.setText(address);
					tv_location.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(mContext, LocationActivity.class);
							intent.putExtra("type", "scan");
							intent.putExtra("latitude", Double.parseDouble(latitude));// 维度
							intent.putExtra("longtitude", Double.parseDouble(longtitude));// 经度
							mContext.startActivity(intent);
						}
					});
				}
			} catch (Exception e) {

			}
			break;
		case BmobConfig.TYPE_VOICE:// 语音消息
			try {
				if (text != null && !text.equals("")) {
					tv_voice_length.setVisibility(View.VISIBLE);
					String content = item.getContent();
					if (item.getBelongId().equals(curUserId)) {// 发送的消息
						if (item.getStatus() == BmobConfig.STATUS_SEND_RECEIVERED
								|| item.getStatus() == BmobConfig.STATUS_SEND_SUCCESS) {// 当发送成功或者发送已阅读的时候，则显示语音长度
							tv_voice_length.setVisibility(View.VISIBLE);
							String length = content.split("&")[2];
							tv_voice_length.setText(length + "\''");
						} else {
							tv_voice_length.setVisibility(View.INVISIBLE);
						}
					} else {// 收到的消息
						boolean isExists = BmobDownloadManager.checkTargetPathExist(curUserId, item);
						if (!isExists) {// 若指定格式的录音文件不存在，则需要下载，因为其文件比较小，故放在此下载
							String netUrl = content.split("&")[0];
							final String length = content.split("&")[1];
							BmobDownloadManager downloadTask = new BmobDownloadManager(mContext, item, new DownloadListener() {

								@Override
								public void onStart() {
									// TODO Auto-generated method stub
									progress_load.setVisibility(View.VISIBLE);
									tv_voice_length.setVisibility(View.GONE);
									iv_voice.setVisibility(View.INVISIBLE);// 只有下载完成才显示播放的按钮
								}

								@Override
								public void onSuccess() {
									// TODO Auto-generated method stub
									progress_load.setVisibility(View.GONE);
									tv_voice_length.setVisibility(View.VISIBLE);
									tv_voice_length.setText(length + "\''");
									iv_voice.setVisibility(View.VISIBLE);
								}

								@Override
								public void onError(String error) {
									// TODO Auto-generated method stub
									progress_load.setVisibility(View.GONE);
									tv_voice_length.setVisibility(View.GONE);
									iv_voice.setVisibility(View.INVISIBLE);
								}
							});
							downloadTask.execute(netUrl);
						} else {
							String length = content.split("&")[2];
							tv_voice_length.setText(length + "\''");
						}
					}
				}
				// 播放语音文件
				iv_voice.setOnClickListener(new NewRecordPlayClickListener(mContext, item, iv_voice));
			} catch (Exception e) {

			}

			break;
		default:
			break;
		}
		return convertView;
	}

	/**
	 * 获取图片的地址--
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
			ImageLoader.getInstance().displayImage(text, iv_picture, options, new ImageLoadingListener() {

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

	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

}
