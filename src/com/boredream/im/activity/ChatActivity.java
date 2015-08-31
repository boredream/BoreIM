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
 * �������
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

	// �����й�
	private BmobRecordManager recordManager;
	private Drawable[] drawable_Anims;// ��Ͳ����
	RelativeLayout layout_record;
	TextView tv_voice_tips;
	ImageView iv_record;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		manager = BmobChatManager.getInstance(this);
		MsgPagerNum = 0;
		
		// ��װ�������
		targetUser = (BmobChatUser) getIntent().getSerializableExtra("user");
		targetId = targetUser.getObjectId();
		
		// ע��㲥������
		initNewMessageBroadCast();
		
		initView();
	}

	private void initView() {
		initBackTitle("��" + targetUser.getUsername() + "�Ի�");
		initBottomView();
		initXListView();
		initVoiceView();
		initEmotion();
	}
	
	/**
	 *  ��ʼ�������������
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
	 * ������ʾ�����GridView
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
	 * ��ʼ����������
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
	 * ������Ϣ��ʷ�������ݿ��ж���
	 */
	private List<BmobMsg> initMsgData() {
		List<BmobMsg> list = BmobDB.create(this).queryMessages(targetId, MsgPagerNum);
		return list;
	}

	/**
	 * ����ˢ��
	 */
	private void initOrRefresh() {
		if (mAdapter != null) {
			if (MyMessageReceiver.mNewNum != 0) {// ���ڸ��µ���������������ڼ�������Ϣ����ʱ�ٻص�����ҳ���ʱ����Ҫ��ʾ��������Ϣ
				int news = MyMessageReceiver.mNewNum;// �п��������ڼ䣬����N����Ϣ,�����Ҫ������ʾ�ڽ�����
				int size = initMsgData().size();
				for (int i = (news - 1); i >= 0; i--) {
					mAdapter.add(initMsgData().get(size - (i + 1)));// ������һ����Ϣ��������ʾ
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
		// �����
		btn_chat_add = (Button) findViewById(R.id.btn_chat_add);
		btn_chat_emo = (Button) findViewById(R.id.btn_chat_emo);
		btn_chat_add.setOnClickListener(this);
		btn_chat_emo.setOnClickListener(this);
		// ���ұ�
		btn_chat_keyboard = (Button) findViewById(R.id.btn_chat_keyboard);
		btn_chat_voice = (Button) findViewById(R.id.btn_chat_voice);
		btn_chat_voice.setOnClickListener(this);
		btn_chat_keyboard.setOnClickListener(this);
		btn_chat_send = (Button) findViewById(R.id.btn_chat_send);
		btn_chat_send.setOnClickListener(this);
		// ������
		layout_more = (LinearLayout) findViewById(R.id.layout_more);
		layout_emo = (LinearLayout) findViewById(R.id.ll_emotion_dashboard);
		vp_emotion_dashboard = (ViewPager) findViewById(R.id.vp_emotion_dashboard);
		layout_add = (LinearLayout) findViewById(R.id.layout_add);
		initAddView();

		// ���м�
		// ������
		btn_speak = (Button) findViewById(R.id.btn_speak);
		// �����
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
		// ��������
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
	 * ��ʾ�ط���ť showResendDialog
	 */
	public void showResendDialog(final View parentV, View v, final Object values) {
		new AlertDialog.Builder(this)
			.setTitle("��ʾ")
			.setMessage("ȷ���ط�����Ϣ?")
			.setNegativeButton("ȡ��", null)
			.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (((BmobMsg) values).getMsgType() == BmobConfig.TYPE_IMAGE
							|| ((BmobMsg) values).getMsgType() == BmobConfig.TYPE_VOICE) {// ͼƬ���������͵Ĳ���
						resendFileMsg(parentV, values);
					} else {
						resendTextMsg(parentV, values);
					}
				}
			})
			.show();
	}

	/**
	 * �ط��ı���Ϣ
	 */
	private void resendTextMsg(final View parentV, final Object values) {
		BmobChatManager.getInstance(ChatActivity.this).resendTextMessage(
				targetUser, (BmobMsg) values, new PushListener() {

					@Override
					public void onSuccess() {

						showLog("���ͳɹ�");
						((BmobMsg) values)
								.setStatus(BmobConfig.STATUS_SEND_SUCCESS);
						parentV.findViewById(R.id.progress_load).setVisibility(
								View.INVISIBLE);
						parentV.findViewById(R.id.iv_fail_resend)
								.setVisibility(View.INVISIBLE);
						parentV.findViewById(R.id.tv_send_status)
								.setVisibility(View.VISIBLE);
						((TextView) parentV.findViewById(R.id.tv_send_status))
								.setText("�ѷ���");
					}

					@Override
					public void onFailure(int arg0, String arg1) {

						showLog("����ʧ��:" + arg1);
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
	 * �ط�ͼƬ��Ϣ
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
									.setText("�ѷ���");
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
		case R.id.edit_user_comment:// ����ı������
			mListView.setSelection(mListView.getCount() - 1);
			if (layout_more.getVisibility() == View.VISIBLE) {
				layout_add.setVisibility(View.GONE);
				layout_emo.setVisibility(View.GONE);
				layout_more.setVisibility(View.GONE);
			}
			break;
		case R.id.btn_chat_emo:// ���Ц��ͼ��
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
		case R.id.btn_chat_add:// ��Ӱ�ť-��ʾͼƬ�����ա�λ��
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
		case R.id.btn_chat_voice:// ������ť
			edit_user_comment.setVisibility(View.GONE);
			layout_more.setVisibility(View.GONE);
			btn_chat_voice.setVisibility(View.GONE);
			btn_chat_keyboard.setVisibility(View.VISIBLE);
			btn_speak.setVisibility(View.VISIBLE);
			CommonUtils.hideSoftInputView(ChatActivity.this, edit_user_comment);
			break;
		case R.id.btn_chat_keyboard:// ���̰�ť������͵������̲����ص�������ť
			showEditState(false);
			break;
		case R.id.btn_chat_send:// �����ı�
			final String msg = edit_user_comment.getText().toString();
			if (msg.equals("")) {
				showToast("�����뷢����Ϣ!");
				return;
			}
//			boolean isNetConnected = CommonUtils.isNetworkAvailable(this);
//			if (!isNetConnected) {
//				showToast(R.string.network_tips);
//				return;
//			}
			// ��װBmobMessage����
			BmobMsg message = BmobMsg.createTextSendMsg(this, targetId, msg);
			message.setExtra("Bmob");
			// Ĭ�Ϸ�����ɣ������ݱ��浽������Ϣ�������Ự����
			manager.sendTextMessage(targetUser, message);
			// ˢ�½���
			refreshMessage(message);
			break;
		case R.id.tv_camera:// ����
			ImageUtils.openCameraImage(this);
			break;
		case R.id.tv_picture:// ͼƬ
			ImageUtils.openLocalImage(this);
			break;
		case R.id.tv_location:// λ��
			showToast("����λ��");
			break;
		default:
			break;
		}
	}
	
	private String localCameraPath = "";// ���պ�õ���ͼƬ��ַ

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case ImageUtils.GET_IMAGE_BY_CAMERA:
				if (resultCode == RESULT_CANCELED) {
					// �������ȡ��,��֮ǰ������ͼƬ��ַɾ��
					ImageUtils.deleteImageUri(this, ImageUtils.imageUriFromCamera);
				} else {
					// ���պ�ͼƬ��ӵ�ҳ����
					localCameraPath = ImageUtils.getImageAbsolutePath(
							this, ImageUtils.imageUriFromCamera);
					sendImageMessage(localCameraPath);
				}
			case ImageUtils.GET_IMAGE_FROM_PHONE:
				if (resultCode != RESULT_CANCELED) {
					// �������ѡ�����ͼƬ��ӵ�ҳ����
					localCameraPath = ImageUtils.getImageAbsolutePath(this, data.getData());
					sendImageMessage(localCameraPath);
				}
				break;
			}
		}
	}
	
	/**
	 * �����Ƿ���Ц������ʾ�ı�������״̬
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
	 * Ĭ�����ϴ�����ͼƬ��֮�����ʾ���� sendImageMessage
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
				showLog("��ʼ�ϴ�onStart��" + msg.getContent() + ",״̬��" + msg.getStatus());
				refreshMessage(msg);
			}

			@Override
			public void onSuccess() {
				mAdapter.notifyDataSetChanged();
			}

			@Override
			public void onFailure(int error, String arg1) {
				showLog("�ϴ�ʧ�� -->arg1��" + arg1);
				mAdapter.notifyDataSetChanged();
			}
		});
	}
	
	/**
	 * ����������Ϣ
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
						showLog("�ϴ�����ʧ�� -->arg1��" + arg1);
						mAdapter.notifyDataSetChanged();
					}
				});
	}
	
	/**
	 * ����˵��
	 */
	class VoiceTouchListen implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (!CommonUtils.checkSdCard()) {
					showToast("����������Ҫsdcard֧�֣�");
					return false;
				}
				
				try {
					v.setPressed(true);
					layout_record.setVisibility(View.VISIBLE);
					tv_voice_tips.setText(getString(R.string.voice_cancel_tips));
					// ��ʼ¼��
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
					if (event.getY() < 0) {// ����¼��
						recordManager.cancelRecording();
						BmobLog.i("voice", "������������");
					} else {
						int recordTime = recordManager.stopRecording();
						if (recordTime > 1) {
							// ���������ļ�
							BmobLog.i("voice", "��������");
							sendVoiceMessage(
									recordManager.getRecordFilePath(targetId),
									recordTime);
						} else {// ¼��ʱ����̣�����ʾ¼�����̵���ʾ
							layout_record.setVisibility(View.GONE);
							showToast("¼��ʱ�����");
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
	 * ��ʼ������¼�ƶ�����Դ
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
		// ������ع�����
		recordManager = BmobRecordManager.getInstance(this);
		// ����������С����--�����￪���߿����Լ�ʵ�֣���ʣ��10������µĸ��û�����ʾ������΢�ŵ���������
		recordManager.setOnRecordChangeListener(new OnRecordChangeListener() {
	
			@Override
			public void onVolumnChanged(int value) {
				iv_record.setImageDrawable(drawable_Anims[value]);
			}
	
			@Override
			public void onTimeChanged(int recordTime, String localPath) {
				BmobLog.i("voice", "��¼������:" + recordTime);
				if (recordTime >= BmobRecordManager.MAX_RECORD_TIME) {// 1���ӽ�����������Ϣ
					// ��Ҫ���ð�ť
					btn_speak.setPressed(false);
					btn_speak.setClickable(false);
					// ȡ��¼����
					layout_record.setVisibility(View.INVISIBLE);
					// ����������Ϣ
					sendVoiceMessage(localPath, recordTime);
					//��Ϊ�˷�ֹ����¼��ʱ��󣬻�෢һ��������ȥ�������
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
		// ����Ϣ�������ˢ�½���
		initOrRefresh();
		MyMessageReceiver.ehList.add(this);// �������͵���Ϣ
		// �п��������ڼ䣬������������֪ͨ������ʱ����Ҫ���֪ͨ�����δ����Ϣ��
		BmobNotifyManager.getInstance(this).cancelNotify();
		BmobDB.create(this).resetUnread(targetId);
		// �����Ϣδ����-���Ҫ��ˢ��֮��
		MyMessageReceiver.mNewNum = 0;
	}

	@Override
	protected void onPause() {
		super.onPause();
		MyMessageReceiver.ehList.remove(this);// �������͵���Ϣ
		
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
				// ������ǵ�ǰ��������������Ϣ��������
				if (!uid.equals(targetId))
					return;
				mAdapter.add(m);
				// ��λ
				mListView.setSelection(mAdapter.getCount() - 1);
				// ȡ����ǰ��������δ����ʾ
				BmobDB.create(ChatActivity.this).resetUnread(targetId);
			}
		}
	};

	public static final int NEW_MESSAGE = 0x001;// �յ���Ϣ

	NewBroadcastReceiver receiver;

	private void initNewMessageBroadCast() {
		// ע�������Ϣ�㲥
		receiver = new NewBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(BmobConfig.BROADCAST_NEW_MESSAGE);
		// ���ù㲥�����ȼ������Mainacitivity,���������Ϣ����ʱ��������chatҳ�棬ֱ����ʾ��Ϣ����������ʾ��Ϣδ��
		intentFilter.setPriority(5);
		registerReceiver(receiver, intentFilter);
	}

	/**
	 * ����Ϣ�㲥������
	 * 
	 */
	private class NewBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String from = intent.getStringExtra("fromId");
			String msgId = intent.getStringExtra("msgId");
			String msgTime = intent.getStringExtra("msgTime");
			// �յ�����㲥��ʱ��message�Ѿ�����Ϣ���У���ֱ�ӻ�ȡ
			if (TextUtils.isEmpty(from) && TextUtils.isEmpty(msgId) && TextUtils.isEmpty(msgTime)) {
				BmobMsg msg = BmobChatManager.getInstance(ChatActivity.this).getMessage(msgId, msgTime);
				if (!from.equals(targetId))// ������ǵ�ǰ��������������Ϣ��������
					return;
				// ��ӵ���ǰҳ��
				mAdapter.add(msg);
				// ��λ
				mListView.setSelection(mAdapter.getCount() - 1);
				// ȡ����ǰ��������δ����ʾ
				BmobDB.create(ChatActivity.this).resetUnread(targetId);
			}
			// �ǵðѹ㲥���ս��
			abortBroadcast();
		}
	}

	/**
	 * ˢ�½���
	 */
	private void refreshMessage(BmobMsg msg) {
		// ���½���
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
		// �˴�Ӧ�ù��˵����Ǻ͵�ǰ�û�������Ļ�ִ��Ϣ�����ˢ��
		if (conversionId.split("&")[1].equals(targetId)) {
			// �޸Ľ�����ָ����Ϣ���Ķ�״̬
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
				BmobLog.i("��¼������" + total);
				int currents = mAdapter.getCount();
				if (total <= currents) {
					showToast("�����¼��������Ŷ!");
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
