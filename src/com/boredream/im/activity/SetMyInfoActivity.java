package com.boredream.im.activity;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.inteface.MsgTag;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.PushListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import com.boredream.im.R;
import com.boredream.im.entity.User;
import com.boredream.im.utils.ImageOptHelper;
import com.boredream.im.utils.ImageUtils;

/**
 * ��������ҳ��
 */
public class SetMyInfoActivity extends BaseActivity implements OnClickListener {

	private TextView tv_set_name, tv_set_nick, tv_set_gender;
	private ImageView iv_set_avator, iv_arraw, iv_nickarraw;

	private Button btn_chat, btn_add_friend;
	private RelativeLayout layout_head, layout_nick, layout_gender;

	private String from = "";
	private String username = "";
	private User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_info);
		from = getIntent().getStringExtra("from");//me add other
		username = getIntent().getStringExtra("username");
		initView();
	}

	private void initView() {
		iv_set_avator = (ImageView) findViewById(R.id.iv_set_avator);
		iv_arraw = (ImageView) findViewById(R.id.iv_arraw);
		iv_nickarraw = (ImageView) findViewById(R.id.iv_nickarraw);
		tv_set_name = (TextView) findViewById(R.id.tv_set_name);
		tv_set_nick = (TextView) findViewById(R.id.tv_set_nick);
		layout_head = (RelativeLayout) findViewById(R.id.layout_head);
		layout_nick = (RelativeLayout) findViewById(R.id.layout_nick);
		layout_gender = (RelativeLayout) findViewById(R.id.layout_gender);
		tv_set_gender = (TextView) findViewById(R.id.tv_set_gender);
		btn_chat = (Button) findViewById(R.id.btn_chat);
		btn_add_friend = (Button) findViewById(R.id.btn_add_friend);
		btn_add_friend.setEnabled(false);
		btn_chat.setEnabled(false);
		if (from.equals("me")) {
			initBackTitle("��������");
			layout_head.setOnClickListener(this);
			layout_nick.setOnClickListener(this);
			layout_gender.setOnClickListener(this);
			iv_nickarraw.setVisibility(View.VISIBLE);
			iv_arraw.setVisibility(View.VISIBLE);
			btn_chat.setVisibility(View.GONE);
			btn_add_friend.setVisibility(View.GONE);
		} else {
			initBackTitle("��ϸ����");
			iv_nickarraw.setVisibility(View.INVISIBLE);
			iv_arraw.setVisibility(View.INVISIBLE);
			//���ܶԷ��ǲ�����ĺ��ѣ������Է�����Ϣ--BmobIM_V1.1.2�޸�
			btn_chat.setVisibility(View.VISIBLE);
			btn_chat.setOnClickListener(this);
			if (from.equals("add")) {// �Ӹ��������б���Ӻ���--��Ϊ��ȡ�������˵ķ����������Ƿ���ʾ���ѵ�����������������Ҫ�ж�������û��Ƿ����Լ��ĺ���
				if (!application.getContactList().containsKey(username)) {// �Ǻ���
					btn_add_friend.setVisibility(View.VISIBLE);
					btn_add_friend.setOnClickListener(this);
				}
			}
			initOtherData(username);
		}
	}

	private void initMeData() {
		User user = userManager.getCurrentUser(User.class);
		initOtherData(user.getUsername());
	}

	private void initOtherData(String name) {
		userManager.queryUser(name, new FindListener<User>() {

			@Override
			public void onError(int arg0, String arg1) {
				showLog("onError onError:" + arg1);
			}

			@Override
			public void onSuccess(List<User> arg0) {
				if (arg0 != null && arg0.size() > 0) {
					user = arg0.get(0);
					btn_chat.setEnabled(true);
					btn_add_friend.setEnabled(true);
					updateUser(user);
				} else {
					showLog("onSuccess ���޴���");
				}
			}
		});
	}

	private void updateUser(User user) {
		// ����
		imageLoader.displayImage(user.getAvatar(), iv_set_avator, ImageOptHelper.getAvatarOptions());
		tv_set_name.setText(user.getUsername());
		tv_set_nick.setText(user.getNick());
		tv_set_gender.setText(user.getSex() == true ? "��" : "Ů");
	}

	@Override
	public void onResume() {
		super.onResume();
		if (from.equals("me")) {
			initMeData();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_chat:// ��������
			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtra("user", user);
			startActivity(intent);
			finish();
			break;
		case R.id.layout_head:
			showImgPickDialog();
			break;
		case R.id.layout_nick:
			// TODO intent2Activity(UpdateInfoActivity.class);
			break;
		case R.id.layout_gender:// �Ա�
			showSexChooseDialog();
			break;
		case R.id.btn_add_friend:// ��Ӻ���
			addFriend();
			break;
		}
	}
	
	private void showImgPickDialog() {
		new AlertDialog.Builder(this)
			.setItems(new String[]{"����", "���"}, 
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case 0:
								ImageUtils.openCameraImage(SetMyInfoActivity.this);
								break;
							case 1:
								ImageUtils.openLocalImage(SetMyInfoActivity.this);
								break;
							}
						}
					})
			.show();
	}
	String[] sexs = new String[]{ "��", "Ů" };
	private void showSexChooseDialog() {
		new AlertDialog.Builder(this)
		.setTitle("��ѡ��")
		.setIcon(android.R.drawable.ic_dialog_info)
		.setSingleChoiceItems(sexs, 0,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
						BmobLog.i("�������"+sexs[which]);
						updateInfo(which);
						dialog.dismiss();
					}
				})
		.setNegativeButton("ȡ��", null)
		.show();
	}

	
	/**
	 * �޸�����
	 */
	private void updateInfo(int which) {
		final User user = userManager.getCurrentUser(User.class);
		BmobLog.i("updateInfo �Ա�"+user.getSex());
		user.setSex(which == 0);
		
		user.update(this, new UpdateListener() {

			@Override
			public void onSuccess() {
				showToast("�޸ĳɹ�");
				final User u = userManager.getCurrentUser(User.class);
				BmobLog.i("�޸ĳɹ����sex = "+u.getSex());
				tv_set_gender.setText(user.getSex() == true ? "��" : "Ů");
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				showToast("onFailure:" + arg1);
			}
		});
	}
	/**
	 * ��Ӻ�������
	 */
	private void addFriend() {
		final ProgressDialog progress = new ProgressDialog(this);
		progress.setMessage("�������...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		// ����tag����
		BmobChatManager.getInstance(this).sendTagMessage(MsgTag.ADD_CONTACT,
				user.getObjectId(), new PushListener() {

					@Override
					public void onSuccess() {
						progress.dismiss();
						showToast("��������ɹ����ȴ��Է���֤��");
					}

					@Override
					public void onFailure(int arg0, final String arg1) {
						progress.dismiss();
						showToast("��������ɹ����ȴ��Է���֤��");
						showLog("��������ʧ��:" + arg1);
					}
				});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Uri imgUri = null;
		
		switch (requestCode) {
		case ImageUtils.GET_IMAGE_BY_CAMERA:
			if (resultCode == RESULT_OK) {
				// �ü�
				imgUri = ImageUtils.imageUriFromCamera;
				ImageUtils.cropImage(this, imgUri);
			} else {
				// ɾ������׼��ʱ���ɵĿ�uri
				ImageUtils.deleteImageUri(this, ImageUtils.imageUriFromCamera);
			}
			break;
		case ImageUtils.GET_IMAGE_FROM_PHONE:
			if (resultCode == RESULT_OK) {
				// �ü�
				imgUri = data.getData();
				ImageUtils.cropImage(this, imgUri);
			}
			break;
		case ImageUtils.CROP_IMAGE:// �ü�ͷ�񷵻�
			if (resultCode == RESULT_OK) {
				// ʹ�òü����ͷ��
				uploadAvatar(ImageUtils.cropImageUri);
			} else {
				// TODO ȡ���ü�ʱ�òü�ǰ��ͼƬ
			}
			break;
		default:
			break;

		}
	}

	private void uploadAvatar(Uri imgUri) {
		final String path = ImageUtils.getImageAbsolutePath(this, imgUri);
		BmobLog.i("ͷ���ַ��" + path);
		final BmobFile bmobFile = new BmobFile(new File(path));
		bmobFile.upload(this, new UploadFileListener() {

			@Override
			public void onSuccess() {
				String url = bmobFile.getFileUrl(SetMyInfoActivity.this);
				// ����BmobUser����
				updateUserAvatar(url);
			}

			@Override
			public void onProgress(Integer arg0) {
				showLog("�ϴ�ͷ�� path=" + path + " ����=" + arg0);
			}

			@Override
			public void onFailure(int arg0, String msg) {
				showToast("ͷ���ϴ�ʧ�ܣ�" + msg);
			}
		});
	}

	private void updateUserAvatar(final String url) {
		User user = (User) userManager.getCurrentUser(User.class);
		user.setAvatar(url);
		user.update(this, new UpdateListener() {
			@Override
			public void onSuccess() {
				showToast("ͷ����³ɹ���");
				imageLoader.displayImage(url, iv_set_avator, ImageOptHelper.getAvatarOptions());
			}

			@Override
			public void onFailure(int code, String msg) {
				showToast("ͷ�����ʧ�ܣ�" + msg);
			}
		});
	}

	

}
