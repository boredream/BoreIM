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
 * 个人资料页面
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
			initBackTitle("个人资料");
			layout_head.setOnClickListener(this);
			layout_description.setOnClickListener(this);
			layout_gender.setOnClickListener(this);
			iv_avator_arrow.setVisibility(View.VISIBLE);
			iv_description_arrow.setVisibility(View.VISIBLE);
			iv_gender_arrow.setVisibility(View.VISIBLE);
			btn_chat.setVisibility(View.GONE);
			btn_add_friend.setVisibility(View.GONE);
		} else {
			initBackTitle("详细资料");
			iv_avator_arrow.setVisibility(View.INVISIBLE);
			iv_description_arrow.setVisibility(View.INVISIBLE);
			iv_gender_arrow.setVisibility(View.INVISIBLE);
			// 不管对方是不是你的好友，均可以发送消息--BmobIM_V1.1.2修改
			btn_chat.setVisibility(View.VISIBLE);
			btn_chat.setOnClickListener(this);
			if (from.equals("add")) {// 从附近的人列表添加好友--因为获取附近的人的方法里面有是否显示好友的情况，因此在这里需要判断下这个用户是否是自己的好友
				if (!application.getContactList().containsKey(username)) {// 是好友
					btn_add_friend.setVisibility(View.VISIBLE);
					btn_add_friend.setOnClickListener(this);
				}
			}
			initOtherData(username);
		}
	}

	private void initMeData() {
		// 先加载当前用户数据
		User user = userManager.getCurrentUser(User.class);
		updateUser(user);
		// 再重新请求用户数据
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
					showLog("onSuccess 查无此人");
				}
			}
		});
	}

	private void updateUser(User user) {
		// 更改
		imageLoader.displayImage(user.getAvatar(), iv_avator,
				ImageOptHelper.getAvatarOptions());
		tv_name.setText(user.getUsername());
		tv_description.setText(user.getDescription());
		tv_gender.setText(user.isSex() ? "男" : "女");
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
		case R.id.btn_chat:// 发起聊天
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
		case R.id.layout_gender:// 性别
			showSexChooseDialog();
			break;
		case R.id.btn_add_friend:// 添加好友
			addFriend();
			break;
		}
	}

	private void showImgPickDialog() {
		new AlertDialog.Builder(this).setItems(new String[] { "拍照", "相册" },
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

	private String[] sexs = new String[] { "男", "女" };
	private void showSexChooseDialog() {
		DialogUtils.showListDialog(this, "修改性别", sexs, 
				new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showLog("点击的是" + sexs[which]);
				boolean isMale = which == 0;
				updateGender(isMale);
			}
		});
	}

	/**
	 * 修改性别
	 */
	private void updateGender(boolean isMale) {
		progressDialog.show();
		
		final User user = userManager.getCurrentUser(User.class);
		showLog("updateInfo 性别：" + user.isSex());
		user.setSex(isMale);
		user.update(this, new UpdateListener() {

			@Override
			public void onSuccess() {
				showLog("修改成功后的sex = " + user.isSex());
				progressDialog.dismiss();
				updateUser(user);
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				showToast("性别修改失败:" + arg1);
				progressDialog.dismiss();
			}
		});
	}

	/**
	 * 添加好友请求
	 */
	private void addFriend() {
		final ProgressDialog progress = new ProgressDialog(this);
		progress.setMessage("正在添加...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		// 发送tag请求
		BmobChatManager.getInstance(this).sendTagMessage(MsgTag.ADD_CONTACT,
				user.getObjectId(), new PushListener() {

					@Override
					public void onSuccess() {
						progress.dismiss();
						showToast("发送请求成功，等待对方验证！");
					}

					@Override
					public void onFailure(int arg0, final String arg1) {
						progress.dismiss();
						showToast("发送请求成功，等待对方验证！");
						showLog("发送请求失败:" + arg1);
					}
				});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case ImageUtils.GET_IMAGE_BY_CAMERA:
			if (resultCode == RESULT_OK) {
				// 选择裁剪
				showCropImageDialog(ImageUtils.imageUriFromCamera);
			} else {
				// 删除拍照准备时生成的空uri
				ImageUtils.deleteImageUri(this, ImageUtils.imageUriFromCamera);
			}
			break;
		case ImageUtils.GET_IMAGE_FROM_PHONE:
			if (resultCode == RESULT_OK) {
				// 选择裁剪
				showCropImageDialog(data.getData());
			}
			break;
		case ImageUtils.CROP_IMAGE:// 裁剪头像返回
			if (resultCode == RESULT_OK) {
				// 使用裁剪后的头像
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
		DialogUtils.showConfirmDialog(this, "提示", "是否对图片进行裁剪",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 确认对图片进行裁剪
						ImageUtils.cropImage(UserInfoActivity.this, imgUri);
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 取消则直接使用图片
						uploadAvatar(imgUri);
					}
				});
	}

	private void uploadAvatar(Uri imgUri) {
		progressDialog.setTitle("正在上传头像");
		progressDialog.show();

		final String path = ImageUtils.getImageAbsolutePath(this, imgUri);
		showLog("头像地址：" + path);
		final BmobFile bmobFile = new BmobFile(new File(path));
		bmobFile.upload(this, new UploadFileListener() {

			@Override
			public void onSuccess() {
				String url = bmobFile.getFileUrl(UserInfoActivity.this);
				// 更新BmobUser对象
				updateUserAvatar(url);

				showLog("上传头像 path=" + path + " 成功");
			}

			@Override
			public void onProgress(Integer arg0) {
				showLog("上传头像 path=" + path + " 进度=" + arg0);
				progressDialog.dismiss();
				ImageUtils.deleteCropImageFile();
			}

			@Override
			public void onFailure(int arg0, String msg) {
				showToast("头像上传失败：" + msg);
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
				
				showToast("头像更新成功！");
				progressDialog.dismiss();
				ImageUtils.deleteCropImageFile();
			}

			@Override
			public void onFailure(int code, String msg) {
				showToast("头像更新失败：" + msg);
				progressDialog.dismiss();
				ImageUtils.deleteCropImageFile();
			}
		});
	}

}
