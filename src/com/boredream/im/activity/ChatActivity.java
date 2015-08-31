package com.boredream.im.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobNotifyManager;
import cn.bmob.im.BmobRecordManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.inteface.EventListener;
import cn.bmob.im.inteface.OnRecordChangeListener;
import cn.bmob.im.inteface.UploadListener;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.listener.PushListener;

import com.boredream.im.R;
import com.boredream.im.adapter.EmotionGvAdapter;
import com.boredream.im.adapter.EmotionPagerAdapter;
import com.boredream.im.adapter.MessageChatAdapter;
import com.boredream.im.listener.NewRecordPlayClickListener;
import com.boredream.im.receiver.MyMessageReceiver;
import com.boredream.im.utils.CommonUtils;
import com.boredream.im.utils.DisplayUtils;
import com.boredream.im.utils.EmotionUtils;
import com.boredream.im.utils.ImageUtils;

/**
 * 聊天界面
 */
@SuppressLint("ClickableViewAccessibility")
public class ChatActivity extends BaseActivity implements OnClickListener, EventListener {

	private Button btn_chat_emo, btn_chat_send, btn_chat_add, btn_chat_keyboard, btn_speak, btn_chat_voice;

	private ListView mListView;

	private MessageChatAdapter mAdapter;
	
	private EditText edit_user_comment;

	private String targetId = "";

	private BmobChatUser targetUser;

	private static int MsgPagerNum;

	private LinearLayout layout_more, layout_emo, layout_add;

	private TextView tv_picture, tv_camera, tv_location;
	
	private ViewPager vp_emotion_dashboard;
	
	private EmotionPagerAdapter emotionPagerGvAdapter;

	// 语音有关
	private BmobRecordManager recordManager;
	private Drawable[] drawable_Anims;// 话筒动画
	RelativeLayout layout_record;
	TextView tv_voice_tips;
	ImageView iv_record;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		manager = BmobChatManager.getInstance(this);
		MsgPagerNum = 0;
		
		// 组装聊天对象
		targetUser = (BmobChatUser) getIntent().getSerializableExtra("user");
		targetId = targetUser.getObjectId();
		
		// 注册广播接收器
		initNewMessageBroadCast();
		
		initView();
	}

	private void initView() {
		initBackTitle("与" + targetUser.getUsername() + "对话");
		initBottomView();
		initXListView();
		initVoiceView();
		initEmotion();
	}
	
	/**
	 *  初始化表情面板内容
	 */
	private void initEmotion() {
		int screenWidth = DisplayUtils.getScreenWidthPixels(this);
		int spacing = DisplayUtils.dp2px(this, 8);
		
		int itemWidth = (screenWidth - spacing * 8) / 7;
		int gvHeight = itemWidth * 3 + spacing * 4;
		
		List<GridView> gvs = new ArrayList<GridView>();
		List<String> emotionNames = new ArrayList<String>();
		for(String emojiName : EmotionUtils.emojiMap.keySet()) {
			emotionNames.add(emojiName);
			
			if(emotionNames.size() == 20) {
				GridView gv = createEmotionGridView(emotionNames, screenWidth, spacing, itemWidth, gvHeight);
				gvs.add(gv);
				
				emotionNames = new ArrayList<String>();
			}
		}
		
		if(emotionNames.size() > 0) {
			GridView gv = createEmotionGridView(emotionNames, screenWidth, spacing, itemWidth, gvHeight);
			gvs.add(gv);
		}
		
		emotionPagerGvAdapter = new EmotionPagerAdapter(gvs);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth, gvHeight);
		vp_emotion_dashboard.setLayoutParams(params);
		vp_emotion_dashboard.setAdapter(emotionPagerGvAdapter);
	}

	/**
	 * 创建显示表情的GridView
	 */
	private GridView createEmotionGridView(List<String> emotionNames, int gvWidth, int padding, int itemWidth, int gvHeight) {
		GridView gv = new GridView(this);
		gv.setBackgroundResource(R.color.bg_gray);
		gv.setSelector(R.color.transparent);
		gv.setNumColumns(7);
		gv.setPadding(padding, padding, padding, padding);
		gv.setHorizontalSpacing(padding);
		gv.setVerticalSpacing(padding);
		
		LayoutParams params = new LayoutParams(gvWidth, gvHeight);
		gv.setLayoutParams(params);
		
		final EmotionGvAdapter adapter = new EmotionGvAdapter(this, emotionNames, itemWidth);
		gv.setAdapter(adapter);
		gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
					
				if(position == adapter.getCount() - 1) {
					edit_user_comment.dispatchKeyEvent(
							new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
				} else {
					String emotionName = adapter.getItem(position);
					
					int curPosition = edit_user_comment.getSelectionStart();
					StringBuilder sb = new StringBuilder(edit_user_comment.getText().toString());
					sb.insert(curPosition, emotionName);
					
					SpannableString weiboContent = EmotionUtils.getEmotionContent(
							ChatActivity.this, edit_user_comment, sb.toString());
					edit_user_comment.setText(weiboContent);
					
					edit_user_comment.setSelection(curPosition + emotionName.length());
				}
					
			}
		});
		
		return gv;
	}
	
	/**
	 * 初始化语音部分
	 */
	private void initVoiceView() {
		layout_record = (RelativeLayout) findViewById(R.id.layout_record);
		tv_voice_tips = (TextView) findViewById(R.id.tv_voice_tips);
		iv_record = (ImageView) findViewById(R.id.iv_record);
		btn_speak.setOnTouchListener(new VoiceTouchListen());
		initVoiceAnimRes();
		initRecordManager();
	}

	/**
	 * 加载消息历史，从数据库中读出
	 */
	private List<BmobMsg> initMsgData() {
		List<BmobMsg> list = BmobDB.create(this).queryMessages(targetId, MsgPagerNum);
		return list;
	}

	/**
	 * 界面刷新
	 */
	private void initOrRefresh() {
		if (mAdapter != null) {
			if (MyMessageReceiver.mNewNum != 0) {// 用于更新当在聊天界面锁屏期间来了消息，这时再回到聊天页面的时候需要显示新来的消息
				int news = MyMessageReceiver.mNewNum;// 有可能锁屏期间，来了N条消息,因此需要倒叙显示在界面上
				int size = initMsgData().size();
				for (int i = (news - 1); i >= 0; i--) {
					mAdapter.add(initMsgData().get(size - (i + 1)));// 添加最后一条消息到界面显示
				}
				mListView.setSelection(mAdapter.getCount() - 1);
			} else {
				mAdapter.notifyDataSetChanged();
			}
		} else {
			mAdapter = new MessageChatAdapter(this, initMsgData());
			mListView.setAdapter(mAdapter);
		}
	}

	private void initAddView() {
		tv_picture = (TextView) findViewById(R.id.tv_picture);
		tv_camera = (TextView) findViewById(R.id.tv_camera);
		tv_location = (TextView) findViewById(R.id.tv_location);
		tv_picture.setOnClickListener(this);
		tv_location.setOnClickListener(this);
		tv_camera.setOnClickListener(this);
	}

	private void initBottomView() {
		// 最左边
		btn_chat_add = (Button) findViewById(R.id.btn_chat_add);
		btn_chat_emo = (Button) findViewById(R.id.btn_chat_emo);
		btn_chat_add.setOnClickListener(this);
		btn_chat_emo.setOnClickListener(this);
		// 最右边
		btn_chat_keyboard = (Button) findViewById(R.id.btn_chat_keyboard);
		btn_chat_voice = (Button) findViewById(R.id.btn_chat_voice);
		btn_chat_voice.setOnClickListener(this);
		btn_chat_keyboard.setOnClickListener(this);
		btn_chat_send = (Button) findViewById(R.id.btn_chat_send);
		btn_chat_send.setOnClickListener(this);
		// 最下面
		layout_more = (LinearLayout) findViewById(R.id.layout_more);
		layout_emo = (LinearLayout) findViewById(R.id.ll_emotion_dashboard);
		vp_emotion_dashboard = (ViewPager) findViewById(R.id.vp_emotion_dashboard);
		layout_add = (LinearLayout) findViewById(R.id.layout_add);
		initAddView();

		// 最中间
		// 语音框
		btn_speak = (Button) findViewById(R.id.btn_speak);
		// 输入框
		edit_user_comment = (EditText) findViewById(R.id.edit_user_comment);
		edit_user_comment.setOnClickListener(this);
		edit_user_comment.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				if (!TextUtils.isEmpty(s)) {
					btn_chat_send.setVisibility(View.VISIBLE);
					btn_chat_keyboard.setVisibility(View.GONE);
					btn_chat_voice.setVisibility(View.GONE);
				} else {
					if (btn_chat_voice.getVisibility() != View.VISIBLE) {
						btn_chat_voice.setVisibility(View.VISIBLE);
						btn_chat_send.setVisibility(View.GONE);
						btn_chat_keyboard.setVisibility(View.GONE);
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

	}

	private void initXListView() {
		mListView = (ListView) findViewById(R.id.mListView);
		// 加载数据
		initOrRefresh();
		mListView.setSelection(mAdapter.getCount() - 1);
		mListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				CommonUtils.hideSoftInputView(ChatActivity.this, edit_user_comment);
				layout_more.setVisibility(View.GONE);
				layout_add.setVisibility(View.GONE);
				btn_chat_voice.setVisibility(View.VISIBLE);
				btn_chat_keyboard.setVisibility(View.GONE);
				btn_chat_send.setVisibility(View.GONE);
				return false;
			}
		});
	}

	/**
	 * 显示重发按钮 showResendDialog
	 */
	public void showResendDialog(final View parentV, View v, final Object values) {
		new AlertDialog.Builder(this)
			.setTitle("提示")
			.setMessage("确定重发该消息?")
			.setNegativeButton("取消", null)
			.setPositiveButton("确定", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (((BmobMsg) values).getMsgType() == BmobConfig.TYPE_IMAGE
							|| ((BmobMsg) values).getMsgType() == BmobConfig.TYPE_VOICE) {// 图片和语音类型的采用
						resendFileMsg(parentV, values);
					} else {
						resendTextMsg(parentV, values);
					}
				}
			})
			.show();
	}

	/**
	 * 重发文本消息
	 */
	private void resendTextMsg(final View parentV, final Object values) {
		BmobChatManager.getInstance(ChatActivity.this).resendTextMessage(
				targetUser, (BmobMsg) values, new PushListener() {

					@Override
					public void onSuccess() {

						showLog("发送成功");
						((BmobMsg) values)
								.setStatus(BmobConfig.STATUS_SEND_SUCCESS);
						parentV.findViewById(R.id.progress_load).setVisibility(
								View.INVISIBLE);
						parentV.findViewById(R.id.iv_fail_resend)
								.setVisibility(View.INVISIBLE);
						parentV.findViewById(R.id.tv_send_status)
								.setVisibility(View.VISIBLE);
						((TextView) parentV.findViewById(R.id.tv_send_status))
								.setText("已发送");
					}

					@Override
					public void onFailure(int arg0, String arg1) {

						showLog("发送失败:" + arg1);
						((BmobMsg) values)
								.setStatus(BmobConfig.STATUS_SEND_FAIL);
						parentV.findViewById(R.id.progress_load).setVisibility(
								View.INVISIBLE);
						parentV.findViewById(R.id.iv_fail_resend)
								.setVisibility(View.VISIBLE);
						parentV.findViewById(R.id.tv_send_status)
								.setVisibility(View.INVISIBLE);
					}
				});
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * 重发图片消息
	 */
	private void resendFileMsg(final View parentV, final Object values) {
		BmobChatManager.getInstance(ChatActivity.this).resendFileMessage(
				targetUser, (BmobMsg) values, new UploadListener() {

					@Override
					public void onStart(BmobMsg msg) {

					}

					@Override
					public void onSuccess() {

						((BmobMsg) values)
								.setStatus(BmobConfig.STATUS_SEND_SUCCESS);
						parentV.findViewById(R.id.progress_load).setVisibility(
								View.INVISIBLE);
						parentV.findViewById(R.id.iv_fail_resend)
								.setVisibility(View.INVISIBLE);
						if (((BmobMsg) values).getMsgType() == BmobConfig.TYPE_VOICE) {
							parentV.findViewById(R.id.tv_send_status)
									.setVisibility(View.GONE);
							parentV.findViewById(R.id.tv_voice_length)
									.setVisibility(View.VISIBLE);
						} else {
							parentV.findViewById(R.id.tv_send_status)
									.setVisibility(View.VISIBLE);
							((TextView) parentV
									.findViewById(R.id.tv_send_status))
									.setText("已发送");
						}
					}

					@Override
					public void onFailure(int arg0, String arg1) {

						((BmobMsg) values)
								.setStatus(BmobConfig.STATUS_SEND_FAIL);
						parentV.findViewById(R.id.progress_load).setVisibility(
								View.INVISIBLE);
						parentV.findViewById(R.id.iv_fail_resend)
								.setVisibility(View.VISIBLE);
						parentV.findViewById(R.id.tv_send_status)
								.setVisibility(View.INVISIBLE);
					}
				});
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.edit_user_comment:// 点击文本输入框
			mListView.setSelection(mListView.getCount() - 1);
			if (layout_more.getVisibility() == View.VISIBLE) {
				layout_add.setVisibility(View.GONE);
				layout_emo.setVisibility(View.GONE);
				layout_more.setVisibility(View.GONE);
			}
			break;
		case R.id.btn_chat_emo:// 点击笑脸图标
			if (layout_more.getVisibility() == View.GONE) {
				showEditState(true);
			} else {
				if (layout_add.getVisibility() == View.VISIBLE) {
					layout_add.setVisibility(View.GONE);
					layout_emo.setVisibility(View.VISIBLE);
				} else {
					layout_more.setVisibility(View.GONE);
				}
			}

			break;
		case R.id.btn_chat_add:// 添加按钮-显示图片、拍照、位置
			if (layout_more.getVisibility() == View.GONE) {
				layout_more.setVisibility(View.VISIBLE);
				layout_add.setVisibility(View.VISIBLE);
				layout_emo.setVisibility(View.GONE);
				CommonUtils.hideSoftInputView(ChatActivity.this, edit_user_comment);
			} else {
				if (layout_emo.getVisibility() == View.VISIBLE) {
					layout_emo.setVisibility(View.GONE);
					layout_add.setVisibility(View.VISIBLE);
				} else {
					layout_more.setVisibility(View.GONE);
				}
			}

			break;
		case R.id.btn_chat_voice:// 语音按钮
			edit_user_comment.setVisibility(View.GONE);
			layout_more.setVisibility(View.GONE);
			btn_chat_voice.setVisibility(View.GONE);
			btn_chat_keyboard.setVisibility(View.VISIBLE);
			btn_speak.setVisibility(View.VISIBLE);
			CommonUtils.hideSoftInputView(ChatActivity.this, edit_user_comment);
			break;
		case R.id.btn_chat_keyboard:// 键盘按钮，点击就弹出键盘并隐藏掉声音按钮
			showEditState(false);
			break;
		case R.id.btn_chat_send:// 发送文本
			final String msg = edit_user_comment.getText().toString();
			if (msg.equals("")) {
				showToast("请输入发送消息!");
				return;
			}
//			boolean isNetConnected = CommonUtils.isNetworkAvailable(this);
//			if (!isNetConnected) {
//				showToast(R.string.network_tips);
//				return;
//			}
			// 组装BmobMessage对象
			BmobMsg message = BmobMsg.createTextSendMsg(this, targetId, msg);
			message.setExtra("Bmob");
			// 默认发送完成，将数据保存到本地消息表和最近会话表中
			manager.sendTextMessage(targetUser, message);
			// 刷新界面
			refreshMessage(message);
			break;
		case R.id.tv_camera:// 拍照
			ImageUtils.openCameraImage(this);
			break;
		case R.id.tv_picture:// 图片
			ImageUtils.openLocalImage(this);
			break;
		case R.id.tv_location:// 位置
			showToast("地理位置");
			break;
		default:
			break;
		}
	}
	
	private String localCameraPath = "";// 拍照后得到的图片地址

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case ImageUtils.GET_IMAGE_BY_CAMERA:
				if (resultCode == RESULT_CANCELED) {
					// 如果拍照取消,将之前新增的图片地址删除
					ImageUtils.deleteImageUri(this, ImageUtils.imageUriFromCamera);
				} else {
					// 拍照后将图片添加到页面上
					localCameraPath = ImageUtils.getImageAbsolutePath(
							this, ImageUtils.imageUriFromCamera);
					sendImageMessage(localCameraPath);
				}
			case ImageUtils.GET_IMAGE_FROM_PHONE:
				if (resultCode != RESULT_CANCELED) {
					// 本地相册选择完后将图片添加到页面上
					localCameraPath = ImageUtils.getImageAbsolutePath(this, data.getData());
					sendImageMessage(localCameraPath);
				}
				break;
			}
		}
	}
	
	/**
	 * 根据是否点击笑脸来显示文本输入框的状态
	 */
	private void showEditState(boolean isEmo) {
		edit_user_comment.setVisibility(View.VISIBLE);
		btn_chat_keyboard.setVisibility(View.GONE);
		btn_chat_voice.setVisibility(View.VISIBLE);
		btn_speak.setVisibility(View.GONE);
		edit_user_comment.requestFocus();
		if (isEmo) {
			layout_more.setVisibility(View.VISIBLE);
			layout_more.setVisibility(View.VISIBLE);
			layout_emo.setVisibility(View.VISIBLE);
			layout_add.setVisibility(View.GONE);
			CommonUtils.hideSoftInputView(ChatActivity.this, edit_user_comment);
		} else {
			layout_more.setVisibility(View.GONE);
			CommonUtils.showSoftInputView(ChatActivity.this, edit_user_comment);
		}
	}

	/**
	 * 默认先上传本地图片，之后才显示出来 sendImageMessage
	 */
	private void sendImageMessage(String local) {
		if (layout_more.getVisibility() == View.VISIBLE) {
			layout_more.setVisibility(View.GONE);
			layout_add.setVisibility(View.GONE);
			layout_emo.setVisibility(View.GONE);
		}
		
		manager.sendImageMessage(targetUser, local, new UploadListener() {

			@Override
			public void onStart(BmobMsg msg) {
				showLog("开始上传onStart：" + msg.getContent() + ",状态：" + msg.getStatus());
				refreshMessage(msg);
			}

			@Override
			public void onSuccess() {
				mAdapter.notifyDataSetChanged();
			}

			@Override
			public void onFailure(int error, String arg1) {
				showLog("上传失败 -->arg1：" + arg1);
				mAdapter.notifyDataSetChanged();
			}
		});
	}
	
	/**
	 * 发送语音消息
	 */
	private void sendVoiceMessage(String local, int length) {
		manager.sendVoiceMessage(targetUser, local, length,
				new UploadListener() {

					@Override
					public void onStart(BmobMsg msg) {
						refreshMessage(msg);
					}

					@Override
					public void onSuccess() {
						mAdapter.notifyDataSetChanged();
					}

					@Override
					public void onFailure(int error, String arg1) {
						showLog("上传语音失败 -->arg1：" + arg1);
						mAdapter.notifyDataSetChanged();
					}
				});
	}
	
	/**
	 * 长按说话
	 */
	class VoiceTouchListen implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (!CommonUtils.checkSdCard()) {
					showToast("发送语音需要sdcard支持！");
					return false;
				}
				
				try {
					v.setPressed(true);
					layout_record.setVisibility(View.VISIBLE);
					tv_voice_tips.setText(getString(R.string.voice_cancel_tips));
					// 开始录音
					recordManager.startRecording(targetId);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			case MotionEvent.ACTION_MOVE: {
				if (event.getY() < 0) {
					tv_voice_tips.setText(getString(R.string.voice_cancel_tips));
					tv_voice_tips.setTextColor(Color.RED);
				} else {
					tv_voice_tips.setText(getString(R.string.voice_up_tips));
					tv_voice_tips.setTextColor(Color.WHITE);
				}
				return true;
			}
			case MotionEvent.ACTION_UP:
				v.setPressed(false);
				layout_record.setVisibility(View.INVISIBLE);
				try {
					if (event.getY() < 0) {// 放弃录音
						recordManager.cancelRecording();
						BmobLog.i("voice", "放弃发送语音");
					} else {
						int recordTime = recordManager.stopRecording();
						if (recordTime > 1) {
							// 发送语音文件
							BmobLog.i("voice", "发送语音");
							sendVoiceMessage(
									recordManager.getRecordFilePath(targetId),
									recordTime);
						} else {// 录音时间过短，则提示录音过短的提示
							layout_record.setVisibility(View.GONE);
							showToast("录音时间过短");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			default:
				return false;
			}
		}
	}

	/**
	 * 初始化语音录制动画资源
	 */
	private void initVoiceAnimRes() {
		drawable_Anims = new Drawable[] {
				getResources().getDrawable(R.drawable.chat_icon_voice2),
				getResources().getDrawable(R.drawable.chat_icon_voice3),
				getResources().getDrawable(R.drawable.chat_icon_voice4),
				getResources().getDrawable(R.drawable.chat_icon_voice5),
				getResources().getDrawable(R.drawable.chat_icon_voice6) };
	}
	
	private void initRecordManager() {
		// 语音相关管理器
		recordManager = BmobRecordManager.getInstance(this);
		// 设置音量大小监听--在这里开发者可以自己实现：当剩余10秒情况下的给用户的提示，类似微信的语音那样
		recordManager.setOnRecordChangeListener(new OnRecordChangeListener() {
	
			@Override
			public void onVolumnChanged(int value) {
				iv_record.setImageDrawable(drawable_Anims[value]);
			}
	
			@Override
			public void onTimeChanged(int recordTime, String localPath) {
				BmobLog.i("voice", "已录音长度:" + recordTime);
				if (recordTime >= BmobRecordManager.MAX_RECORD_TIME) {// 1分钟结束，发送消息
					// 需要重置按钮
					btn_speak.setPressed(false);
					btn_speak.setClickable(false);
					// 取消录音框
					layout_record.setVisibility(View.INVISIBLE);
					// 发送语音消息
					sendVoiceMessage(localPath, recordTime);
					//是为了防止过了录音时间后，会多发一条语音出去的情况。
					handler.postDelayed(new Runnable() {
	
						@Override
						public void run() {
							btn_speak.setClickable(true);
						}
					}, 1000);
				}
			}
		});
	}
	
	@Override
	protected void onResume() {

		super.onResume();
		// 新消息到达，重新刷新界面
		initOrRefresh();
		MyMessageReceiver.ehList.add(this);// 监听推送的消息
		// 有可能锁屏期间，在聊天界面出现通知栏，这时候需要清除通知和清空未读消息数
		BmobNotifyManager.getInstance(this).cancelNotify();
		BmobDB.create(this).resetUnread(targetId);
		// 清空消息未读数-这个要在刷新之后
		MyMessageReceiver.mNewNum = 0;
	}

	@Override
	protected void onPause() {
		super.onPause();
		MyMessageReceiver.ehList.remove(this);// 监听推送的消息
		
		if(NewRecordPlayClickListener.currentPlayListener != null
				&& NewRecordPlayClickListener.isPlaying) {
			NewRecordPlayClickListener.currentPlayListener.stopPlayRecord();
			NewRecordPlayClickListener.currentPlayListener = null;
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == NEW_MESSAGE) {
				BmobMsg message = (BmobMsg) msg.obj;
				String uid = message.getBelongId();
				BmobMsg m = BmobChatManager.getInstance(ChatActivity.this)
						.getMessage(message.getConversationId(), message.getMsgTime());
				// 如果不是当前正在聊天对象的消息，不处理
				if (!uid.equals(targetId))
					return;
				mAdapter.add(m);
				// 定位
				mListView.setSelection(mAdapter.getCount() - 1);
				// 取消当前聊天对象的未读标示
				BmobDB.create(ChatActivity.this).resetUnread(targetId);
			}
		}
	};

	public static final int NEW_MESSAGE = 0x001;// 收到消息

	NewBroadcastReceiver receiver;

	private void initNewMessageBroadCast() {
		// 注册接收消息广播
		receiver = new NewBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(BmobConfig.BROADCAST_NEW_MESSAGE);
		// 设置广播的优先级别大于Mainacitivity,这样如果消息来的时候正好在chat页面，直接显示消息，而不是提示消息未读
		intentFilter.setPriority(5);
		registerReceiver(receiver, intentFilter);
	}

	/**
	 * 新消息广播接收者
	 * 
	 */
	private class NewBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String from = intent.getStringExtra("fromId");
			String msgId = intent.getStringExtra("msgId");
			String msgTime = intent.getStringExtra("msgTime");
			// 收到这个广播的时候，message已经在消息表中，可直接获取
			if (TextUtils.isEmpty(from) && TextUtils.isEmpty(msgId) && TextUtils.isEmpty(msgTime)) {
				BmobMsg msg = BmobChatManager.getInstance(ChatActivity.this).getMessage(msgId, msgTime);
				if (!from.equals(targetId))// 如果不是当前正在聊天对象的消息，不处理
					return;
				// 添加到当前页面
				mAdapter.add(msg);
				// 定位
				mListView.setSelection(mAdapter.getCount() - 1);
				// 取消当前聊天对象的未读标示
				BmobDB.create(ChatActivity.this).resetUnread(targetId);
			}
			// 记得把广播给终结掉
			abortBroadcast();
		}
	}

	/**
	 * 刷新界面
	 */
	private void refreshMessage(BmobMsg msg) {
		// 更新界面
		mAdapter.add(msg);
		mListView.setSelection(mAdapter.getCount() - 1);
		edit_user_comment.setText("");
	}

	@Override
	public void onMessage(BmobMsg message) {
		showLog("onMessage ... message=" + message);
		Message handlerMsg = handler.obtainMessage(NEW_MESSAGE);
		handlerMsg.obj = message;
		handler.sendMessage(handlerMsg);
	}

	@Override
	public void onNetChange(boolean isNetConnected) {
		showLog("onNetChange ... isNetConnected=" + isNetConnected);
		if (!isNetConnected) {
			showToast(R.string.network_tips);
		}
	}

	@Override
	public void onAddUser(BmobInvitation invite) {
		showLog("onAddUser ... invite=" + invite);
	}

	@Override
	public void onOffline() {
		// showOfflineDialog(this);
		showLog("onOffline");
	}

	@Override
	public void onReaded(String conversionId, String msgTime) {
		showLog("onReaded ... conversionId=" + conversionId + " ... msgTime=" + msgTime);
		// 此处应该过滤掉不是和当前用户的聊天的回执消息界面的刷新
		if (conversionId.split("&")[1].equals(targetId)) {
			// 修改界面上指定消息的阅读状态
			for (BmobMsg msg : mAdapter.getList()) {
				if (msg.getConversationId().equals(conversionId)
						&& msg.getMsgTime().equals(msgTime)) {
					msg.setStatus(BmobConfig.STATUS_SEND_RECEIVERED);
				}
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	public void onRefresh() {

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {

				MsgPagerNum++;
				int total = BmobDB.create(ChatActivity.this).queryChatTotalCount(targetId);
				BmobLog.i("记录总数：" + total);
				int currents = mAdapter.getCount();
				if (total <= currents) {
					showToast("聊天记录加载完了哦!");
				} else {
					List<BmobMsg> msgList = initMsgData();
					mAdapter.setList(msgList);
					mListView.setSelection(mAdapter.getCount() - currents - 1);
				}
			}
		}, 1000);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (layout_more.getVisibility() == 0) {
				layout_more.setVisibility(View.GONE);
				return false;
			} else {
				return super.onKeyDown(keyCode, event);
			}
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		CommonUtils.hideSoftInputView(ChatActivity.this, edit_user_comment);
		try {
			unregisterReceiver(receiver);
		} catch (Exception e) {
		}

	}

}
