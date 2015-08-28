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
 * 个人资料页面
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
			initBackTitle("个人资料");
			layout_head.setOnClickListener(this);
			layout_nick.setOnClickListener(this);
			layout_gender.setOnClickListener(this);
			iv_nickarraw.setVisibility(View.VISIBLE);
			iv_arraw.setVisibility(View.VISIBLE);
			btn_chat.setVisibility(View.GONE);
			btn_add_friend.setVisibility(View.GONE);
		} else {
			initBackTitle("详细资料");
			iv_nickarraw.setVisibility(View.INVISIBLE);
			iv_arraw.setVisibility(View.INVISIBLE);
			//不管对方是不是你的好友，均可以发送消息--BmobIM_V1.1.2修改
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
					showLog("onSuccess 查无此人");
				}
			}
		});
	}

	private void updateUser(User user) {
		// 更改
		imageLoader.displayImage(user.getAvatar(), iv_set_avator, ImageOptHelper.getAvatarOptions());
		tv_set_name.setText(user.getUsername());
		tv_set_nick.setText(user.getNick());
		tv_set_gender.setText(user.getSex() == true ? "男" : "女");
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
		case R.id.btn_chat:// 发起聊天
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
		case R.id.layout_gender:// 性别
			showSexChooseDialog();
			break;
		case R.id.btn_add_friend:// 添加好友
			addFriend();
			break;
		}
	}
	
	private void showImgPickDialog() {
		new AlertDialog.Builder(this)
			.setItems(new String[]{"拍照", "相册"}, 
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
	String[] sexs = new String[]{ "男", "女" };
	private void showSexChooseDialog() {
		new AlertDialog.Builder(this)
		.setTitle("单选框")
		.setIcon(android.R.drawable.ic_dialog_info)
		.setSingleChoiceItems(sexs, 0,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
						BmobLog.i("点击的是"+sexs[which]);
						updateInfo(which);
						dialog.dismiss();
					}
				})
		.setNegativeButton("取消", null)
		.show();
	}

	
	/**
	 * 修改资料
	 */
	private void updateInfo(int which) {
		final User user = userManager.getCurrentUser(User.class);
		BmobLog.i("updateInfo 性别："+user.getSex());
		user.setSex(which == 0);
		
		user.update(this, new UpdateListener() {

			@Override
			public void onSuccess() {
				showToast("修改成功");
				final User u = userManager.getCurrentUser(User.class);
				BmobLog.i("修改成功后的sex = "+u.getSex());
				tv_set_gender.setText(user.getSex() == true ? "男" : "女");
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				showToast("onFailure:" + arg1);
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
		
		Uri imgUri = null;
		
		switch (requestCode) {
		case ImageUtils.GET_IMAGE_BY_CAMERA:
			if (resultCode == RESULT_OK) {
				// 裁剪
				imgUri = ImageUtils.imageUriFromCamera;
				ImageUtils.cropImage(this, imgUri);
			} else {
				// 删除拍照准备时生成的空uri
				ImageUtils.deleteImageUri(this, ImageUtils.imageUriFromCamera);
			}
			break;
		case ImageUtils.GET_IMAGE_FROM_PHONE:
			if (resultCode == RESULT_OK) {
				// 裁剪
				imgUri = data.getData();
				ImageUtils.cropImage(this, imgUri);
			}
			break;
		case ImageUtils.CROP_IMAGE:// 裁剪头像返回
			if (resultCode == RESULT_OK) {
				// 使用裁剪后的头像
				uploadAvatar(ImageUtils.cropImageUri);
			} else {
				// TODO 取消裁剪时用裁剪前的图片
			}
			break;
		default:
			break;

		}
	}

	private void uploadAvatar(Uri imgUri) {
		final String path = ImageUtils.getImageAbsolutePath(this, imgUri);
		BmobLog.i("头像地址：" + path);
		final BmobFile bmobFile = new BmobFile(new File(path));
		bmobFile.upload(this, new UploadFileListener() {

			@Override
			public void onSuccess() {
				String url = bmobFile.getFileUrl(SetMyInfoActivity.this);
				// 更新BmobUser对象
				updateUserAvatar(url);
			}

			@Override
			public void onProgress(Integer arg0) {
				showLog("上传头像 path=" + path + " 进度=" + arg0);
			}

			@Override
			public void onFailure(int arg0, String msg) {
				showToast("头像上传失败：" + msg);
			}
		});
	}

	private void updateUserAvatar(final String url) {
		User user = (User) userManager.getCurrentUser(User.class);
		user.setAvatar(url);
		user.update(this, new UpdateListener() {
			@Override
			public void onSuccess() {
				showToast("头像更新成功！");
				imageLoader.displayImage(url, iv_set_avator, ImageOptHelper.getAvatarOptions());
			}

			@Override
			public void onFailure(int code, String msg) {
				showToast("头像更新失败：" + msg);
			}
		});
	}

	

}
