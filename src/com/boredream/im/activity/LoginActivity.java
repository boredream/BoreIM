package com.boredream.im.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.listener.SaveListener;

import com.boredream.im.R;
import com.boredream.im.utils.CommonUtils;
import com.boredream.im.utils.TitleBuilder;

/**
 * 登陆页
 */
public class LoginActivity extends BaseActivity implements OnClickListener {

	private EditText et_username;
	private EditText et_psw;
	private Button btn_login;
	private TextView btn_register;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		init();
	}

	private void init() {
		new TitleBuilder(this).setTitleText("登录");
		
		et_username = (EditText) findViewById(R.id.et_username);
		et_psw = (EditText) findViewById(R.id.et_psw);
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_register = (TextView) findViewById(R.id.btn_register);
		btn_login.setOnClickListener(this);
		btn_register.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == btn_register) {
			Intent intent = new Intent(LoginActivity.this,
					RegisterActivity.class);
			startActivity(intent);
		} else {
			boolean isNetConnected = CommonUtils.isNetworkAvailable(this);
			if (!isNetConnected) {
				showToast(R.string.network_tips);
				return;
			}
			login();
		}
	}

	private void login() {
		String name = et_username.getText().toString();
		String password = et_psw.getText().toString();

		if (TextUtils.isEmpty(name)) {
			showToast(R.string.toast_error_username_null);
			return;
		}

		if (TextUtils.isEmpty(password)) {
			showToast(R.string.toast_error_password_null);
			return;
		}

		progressDialog.show();
		userManager.login(name, password, new SaveListener() {

			@Override
			public void onSuccess() {
				progressDialog.dismiss();
				// 更新用户的地理位置以及好友的资料
				updateUserInfos();
				intent2Activity(MainActivity.class);
				finish();
			}

			@Override
			public void onFailure(int errorcode, String arg0) {
				progressDialog.dismiss();
				BmobLog.i(arg0);
				showToast(arg0);
			}
		});

	}
}
