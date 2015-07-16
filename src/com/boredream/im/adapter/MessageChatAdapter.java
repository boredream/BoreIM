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
 * ����������
 * 
 * @ClassName: MessageChatAdapter
 * @Description: TODO
 * @author smile
 * @date 2014-5-28 ����5:34:07
 */
public class MessageChatAdapter extends BaseAdapter {

	// 8��Item������
	// �ı�
	private final int TYPE_SEND_TXT = 0;
	private final int TYPE_RECEIVER_TXT = 1;
	// ͼƬ
	private final int TYPE_SEND_IMAGE = 2;
	private final int TYPE_RECEIVER_IMAGE = 3;
	// λ��
	private final int TYPE_SEND_LOCATION = 4;
	private final int TYPE_RECEIVER_LOCATION = 5;
	// ����
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
		// ��Ϣ����id����ǵ�ǰ�û�,������ϢΪsend����,����Ϊreceiver����
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
		// ��Ϣ����id����ǵ�ǰ�û�,������ϢΪsend����,����Ϊreceiver����
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
		// ���ͷ������������
		String avatar = item.getBelongAvatar();
		ImageLoader.getInstance().displayImage(avatar, holder.iv_avatar);

//		tv_time.setText(TimeUtil.getChatTime(Long.parseLong(item.getMsgTime())));
		holder.tv_time.setText(item.getMsgTime());

		if (getItemViewType(position) == TYPE_SEND_TXT
				// ||getItemViewType(position)==TYPE_SEND_IMAGE//ͼƬ��������
				|| getItemViewType(position) == TYPE_SEND_LOCATION
				|| getItemViewType(position) == TYPE_SEND_VOICE) {// ֻ���Լ����͵���Ϣ�����ط�����
			// ״̬����
			if (item.getStatus() == BmobConfig.STATUS_SEND_SUCCESS) {// ���ͳɹ�
				holder.progress_load.setVisibility(View.INVISIBLE);
				holder.iv_fail_resend.setVisibility(View.INVISIBLE);
				if (item.getMsgType() == BmobConfig.TYPE_VOICE) {
					holder.tv_send_status.setVisibility(View.GONE);
					holder.tv_voice_length.setVisibility(View.VISIBLE);
				} else {
					holder.tv_send_status.setVisibility(View.VISIBLE);
					holder.tv_send_status.setText("�ѷ���");
				}
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_FAIL) {// ����������Ӧ���߲�ѯʧ�ܵ�ԭ����ɵķ���ʧ�ܣ�����Ҫ�ط�
				holder.progress_load.setVisibility(View.INVISIBLE);
				holder.iv_fail_resend.setVisibility(View.VISIBLE);
				holder.tv_send_status.setVisibility(View.INVISIBLE);
				if (item.getMsgType() == BmobConfig.TYPE_VOICE) {
					holder.tv_voice_length.setVisibility(View.GONE);
				}
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_RECEIVERED) {// �Է��ѽ��յ�
				holder.progress_load.setVisibility(View.INVISIBLE);
				holder.iv_fail_resend.setVisibility(View.INVISIBLE);
				if (item.getMsgType() == BmobConfig.TYPE_VOICE) {
					holder.tv_send_status.setVisibility(View.GONE);
					holder.tv_voice_length.setVisibility(View.VISIBLE);
				} else {
					holder.tv_send_status.setVisibility(View.VISIBLE);
					holder.tv_send_status.setText("���Ķ�");
				}
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_START) {// ��ʼ�ϴ�
				holder.progress_load.setVisibility(View.VISIBLE);
				holder.iv_fail_resend.setVisibility(View.INVISIBLE);
				holder.tv_send_status.setVisibility(View.INVISIBLE);
				if (item.getMsgType() == BmobConfig.TYPE_VOICE) {
					holder.tv_voice_length.setVisibility(View.GONE);
				}
			}
		}
		
		// ����������ʾ����
		final String text = item.getContent();
		switch (item.getMsgType()) {
		case BmobConfig.TYPE_TEXT:
			holder.tv_message.setText(text);
			break;
		case BmobConfig.TYPE_IMAGE:// ͼƬ��
			if (text != null && !text.equals("")) {// ���ͳɹ�֮��洢��ͼƬ���͵�content�ͽ��յ����ǲ�һ����
				dealWithImage(position, 
						holder.progress_load, 
						holder.iv_fail_resend, 
						holder.tv_send_status, 
						holder.iv_picture, 
						item);
			}
			break;
		case BmobConfig.TYPE_LOCATION:// λ����Ϣ
			if (text != null && !text.equals("")) {
				String address = text.split("&")[0];
				final String latitude = text.split("&")[1];// ά��
				final String longtitude = text.split("&")[2];// ����
				holder.tv_location.setText(address);
			}
			break;
		case BmobConfig.TYPE_VOICE:// ������Ϣ
			if (text != null && !text.equals("")) {
				holder.tv_voice_length.setVisibility(View.VISIBLE);
				String content = item.getContent();
				if (item.getBelongId().equals(curUserId)) {// ���͵���Ϣ
					if (item.getStatus() == BmobConfig.STATUS_SEND_RECEIVERED
							|| item.getStatus() == BmobConfig.STATUS_SEND_SUCCESS) {// �����ͳɹ����߷������Ķ���ʱ������ʾ��������
						holder.tv_voice_length.setVisibility(View.VISIBLE);
						String length = content.split("&")[2];
						holder.tv_voice_length.setText(length + "\''");
					} else {
						holder.tv_voice_length.setVisibility(View.INVISIBLE);
					}
				} else {// �յ�����Ϣ
					boolean isExists = BmobDownloadManager.checkTargetPathExist(curUserId, item);
					if (!isExists) {// ��ָ����ʽ��¼���ļ������ڣ�����Ҫ���أ���Ϊ���ļ��Ƚ�С���ʷ��ڴ�����
						String netUrl = content.split("&")[0];
						final String length = content.split("&")[1];
						BmobDownloadManager downloadTask = new BmobDownloadManager(context, item, 
								new DownloadListener() {

							@Override
							public void onStart() {
								// TODO Auto-generated method stub
								holder.progress_load.setVisibility(View.VISIBLE);
								holder.tv_voice_length.setVisibility(View.GONE);
								holder.iv_voice.setVisibility(View.INVISIBLE);// ֻ��������ɲ���ʾ���ŵİ�ť
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
			// ���������ļ�
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
	 * ��ȡͼƬ�ĵ�ַ
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
		} else {// ������յ�����Ϣ������Ҫ����������
			showUrl = text;
		}
		return showUrl;
	}

	/**
	 * ����ͼƬ
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
		if (getItemViewType(position) == TYPE_SEND_IMAGE) {// ���͵���Ϣ
			if (item.getStatus() == BmobConfig.STATUS_SEND_START) {
				progress_load.setVisibility(View.VISIBLE);
				iv_fail_resend.setVisibility(View.INVISIBLE);
				tv_send_status.setVisibility(View.INVISIBLE);
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_SUCCESS) {
				progress_load.setVisibility(View.INVISIBLE);
				iv_fail_resend.setVisibility(View.INVISIBLE);
				tv_send_status.setVisibility(View.VISIBLE);
				tv_send_status.setText("�ѷ���");
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_FAIL) {
				progress_load.setVisibility(View.INVISIBLE);
				iv_fail_resend.setVisibility(View.VISIBLE);
				tv_send_status.setVisibility(View.INVISIBLE);
			} else if (item.getStatus() == BmobConfig.STATUS_SEND_RECEIVERED) {
				progress_load.setVisibility(View.INVISIBLE);
				iv_fail_resend.setVisibility(View.INVISIBLE);
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
