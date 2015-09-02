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
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.PushListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import com.boredream.im.R;
import com.boredream.im.entity.User;
import com.boredream.im.utils.DialogUtils;
import com.boredream.im.utils.ImageOptHelper;
import com.boredream.im.utils.ImageUtils;

/**
 * ��������ҳ��
 */
public class UserInfoActivity extends BaseActivity implements OnClickListener {

	// avatar
	private RelativeLayout layout_head;
	private ImageView iv_avator;
	private ImageView iv_avator_arrow;
	// name
	private TextView tv_name;
	// description
	private RelativeLayout layout_description;
	private TextView tv_description;
	private ImageView iv_description_arrow;
	// gender
	private RelativeLayout layout_gender;
	private TextView tv_gender;
	private ImageView iv_gender_arrow;
	// button
	private Button btn_add_friend;
	private Button btn_chat;

	private String from = "";
	private String username = "";
	private User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_userinfo);
		
		from = getIntent().getStringExtra("from");// me add other
		username = getIntent().getStringExtra("username");
		
		initView();
	}

	private void initView() {
		// avatar
		layout_head = (RelativeLayout) findViewById(R.id.layout_head);
		iv_avator = (ImageView) findViewById(R.id.iv_avator);
		iv_avator_arrow = (ImageView) findViewById(R.id.iv_avator_arrow);
		// name
		tv_name = (TextView) findViewById(R.id.tv_name);
		// description
		layout_description = (RelativeLayout) findViewById(R.id.layout_description);
		tv_description = (TextView) findViewById(R.id.tv_description);
		iv_description_arrow = (ImageView) findViewById(R.id.iv_description_arrow);
		// gender
		layout_gender = (RelativeLayout) findViewById(R.id.layout_gender);
		tv_gender = (TextView) findViewById(R.id.tv_gender);
		iv_gender_arrow = (ImageView) findViewById(R.id.iv_gender_arrow);
		// button
		btn_add_friend = (Button) findViewById(R.id.btn_add_friend);
		btn_chat = (Button) findViewById(R.id.btn_chat);

		btn_add_friend.setEnabled(false);
		btn_chat.setEnabled(false);
		if (from.equals("me")) {
			initBackTitle("��������");
			layout_head.setOnClickListener(this);
			layout_description.setOnClickListener(this);
			layout_gender.setOnClickListener(this);
			iv_avator_arrow.setVisibility(View.VISIBLE);
			iv_description_arrow.setVisibility(View.VISIBLE);
			iv_gender_arrow.setVisibility(View.VISIBLE);
			btn_chat.setVisibility(View.GONE);
			btn_add_friend.setVisibility(View.GONE);
		} else {
			initBackTitle("��ϸ����");
			iv_avator_arrow.setVisibility(View.INVISIBLE);
			iv_description_arrow.setVisibility(View.INVISIBLE);
			iv_gender_arrow.setVisibility(View.INVISIBLE);
			// ���ܶԷ��ǲ�����ĺ��ѣ������Է�����Ϣ--BmobIM_V1.1.2�޸�
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
		// �ȼ��ص�ǰ�û�����
		User user = userManager.getCurrentUser(User.class);
		updateUser(user);
		// �����������û�����
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
		imageLoader.displayImage(user.getAvatar(), iv_avator,
				ImageOptHelper.getAvatarOptions());
		tv_name.setText(user.getUsername());
		tv_description.setText(user.getDescription());
		tv_gender.setText(user.isSex() ? "��" : "Ů");
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
		Intent intent;
		switch (v.getId()) {
		case R.id.btn_chat:// ��������
			intent = new Intent(this, ChatActivity.class);
			intent.putExtra("user", user);
			startActivity(intent);
			finish();
			break;
		case R.id.layout_head:
			showImgPickDialog();
			break;
		case R.id.layout_description:
			intent = new Intent(this, UpdateEditInfoActivity.class);
			intent.putExtra("type", UpdateEditInfoActivity.EDIT_INFO_DESCRIPTION);
			intent.putExtra("oldData", tv_description.getText().toString());
			startActivity(intent);
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
		new AlertDialog.Builder(this).setItems(new String[] { "����", "���" },
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							ImageUtils.openCameraImage(UserInfoActivity.this);
							break;
						case 1:
							ImageUtils.openLocalImage(UserInfoActivity.this);
							break;
						}
					}
				}).show();
	}

	private String[] sexs = new String[] { "��", "Ů" };
	private void showSexChooseDialog() {
		DialogUtils.showListDialog(this, "�޸��Ա�", sexs, 
				new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showLog("�������" + sexs[which]);
				boolean isMale = which == 0;
				updateGender(isMale);
			}
		});
	}

	/**
	 * �޸��Ա�
	 */
	private void updateGender(boolean isMale) {
		progressDialog.show();
		
		final User user = userManager.getCurrentUser(User.class);
		showLog("updateInfo �Ա�" + user.isSex());
		user.setSex(isMale);
		user.update(this, new UpdateListener() {

			@Override
			public void onSuccess() {
				showLog("�޸ĳɹ����sex = " + user.isSex());
				progressDialog.dismiss();
				updateUser(user);
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				showToast("�Ա��޸�ʧ��:" + arg1);
				progressDialog.dismiss();
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

		switch (requestCode) {
		case ImageUtils.GET_IMAGE_BY_CAMERA:
			if (resultCode == RESULT_OK) {
				// ѡ��ü�
				showCropImageDialog(ImageUtils.imageUriFromCamera);
			} else {
				// ɾ������׼��ʱ���ɵĿ�uri
				ImageUtils.deleteImageUri(this, ImageUtils.imageUriFromCamera);
			}
			break;
		case ImageUtils.GET_IMAGE_FROM_PHONE:
			if (resultCode == RESULT_OK) {
				// ѡ��ü�
				showCropImageDialog(data.getData());
			}
			break;
		case ImageUtils.CROP_IMAGE:// �ü�ͷ�񷵻�
			if (resultCode == RESULT_OK) {
				// ʹ�òü����ͷ��
				uploadAvatar(ImageUtils.cropImageUri);
			} else {
				ImageUtils.deleteCropImageFile();
			}
			break;
		default:
			break;

		}
	}

	private void showCropImageDialog(final Uri imgUri) {
		DialogUtils.showConfirmDialog(this, "��ʾ", "�Ƿ��ͼƬ���вü�",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// ȷ�϶�ͼƬ���вü�
						ImageUtils.cropImage(UserInfoActivity.this, imgUri);
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// ȡ����ֱ��ʹ��ͼƬ
						uploadAvatar(imgUri);
					}
				});
	}

	private void uploadAvatar(Uri imgUri) {
		progressDialog.setTitle("�����ϴ�ͷ��");
		progressDialog.show();

		final String path = ImageUtils.getImageAbsolutePath(this, imgUri);
		showLog("ͷ���ַ��" + path);
		final BmobFile bmobFile = new BmobFile(new File(path));
		bmobFile.upload(this, new UploadFileListener() {

			@Override
			public void onSuccess() {
				String url = bmobFile.getFileUrl(UserInfoActivity.this);
				// ����BmobUser����
				updateUserAvatar(url);

				showLog("�ϴ�ͷ�� path=" + path + " �ɹ�");
			}

			@Override
			public void onProgress(Integer arg0) {
				showLog("�ϴ�ͷ�� path=" + path + " ����=" + arg0);
				progressDialog.dismiss();
				ImageUtils.deleteCropImageFile();
			}

			@Override
			public void onFailure(int arg0, String msg) {
				showToast("ͷ���ϴ�ʧ�ܣ�" + msg);
				progressDialog.dismiss();
				ImageUtils.deleteCropImageFile();
			}
		});
	}

	private void updateUserAvatar(final String url) {
		final User user = (User) userManager.getCurrentUser(User.class);
		user.setAvatar(url);
		user.update(this, new UpdateListener() {
			@Override
			public void onSuccess() {
				updateUser(user);
				
				showToast("ͷ����³ɹ���");
				progressDialog.dismiss();
				ImageUtils.deleteCropImageFile();
			}

			@Override
			public void onFailure(int code, String msg) {
				showToast("ͷ�����ʧ�ܣ�" + msg);
				progressDialog.dismiss();
				ImageUtils.deleteCropImageFile();
			}
		});
	}

}
