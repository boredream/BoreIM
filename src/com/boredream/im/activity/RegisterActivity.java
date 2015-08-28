package com.boredream.im.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.listener.SaveListener;

import com.boredream.im.R;
import com.boredream.im.entity.User;
import com.boredream.im.utils.CommonUtils;

/**
 * ע��ҳ
 */
public class RegisterActivity extends BaseActivity {

	Button btn_register;
	EditText et_username, et_psw, et_psw_confirm;
	BmobChatUser currentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		initView();
	}

	private void initView() {
		initBackTitle("ע��");
		
		et_username = (EditText) findViewById(R.id.et_username);
		et_psw = (EditText) findViewById(R.id.et_psw);
		et_psw_confirm = (EditText) findViewById(R.id.et_psw_confirm);

		btn_register = (Button) findViewById(R.id.btn_register);
		btn_register.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				register();
			}
		});
	}

	private void register() {
		String name = et_username.getText().toString();
		String password = et_psw.getText().toString();
		String pwd_again = et_psw_confirm.getText().toString();

		if (TextUtils.isEmpty(name)) {
			showToast(R.string.toast_error_username_null);
			return;
		}

		if (TextUtils.isEmpty(password)) {
			showToast(R.string.toast_error_password_null);
			return;
		}
		if (!pwd_again.equals(password)) {
			showToast(R.string.toast_error_comfirm_password);
			return;
		}

		boolean isNetConnected = CommonUtils.isNetworkAvailable(this);
		if (!isNetConnected) {
			showToast(R.string.network_tips);
			return;
		}

		final ProgressDialog progress = new ProgressDialog(RegisterActivity.this);
		progress.setMessage("����ע��...");
		progress.setCanceledOnTouchOutside(false);
		progress.show();
		// ����ÿ��Ӧ�õ�ע����������϶���һ������IM sdkδ�ṩע�᷽�����û��ɰ���bmod SDK��ע�᷽ʽ����ע�ᡣ
		// ע���ʱ����Ҫע�����㣺1��User���а��豸id��type��2���豸���а�username�ֶ�
		final User bu = new User();
		bu.setUsername(name);
		bu.setPassword(password);
		// ��user���豸id���а�
		bu.setDeviceType("android");
		bu.setInstallId(BmobInstallation.getInstallationId(this));
		bu.signUp(RegisterActivity.this, new SaveListener() {

			@Override
			public void onSuccess() {
				progress.dismiss();
				showToast("ע��ɹ�");
				// ���豸��username���а�
				userManager.bindInstallationForRegister(bu.getUsername());
				// ���µ���λ����Ϣ
				updateUserLocation();

				// ������ҳ
				Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
				startActivity(intent);
				finish();

			}

			@Override
			public void onFailure(int arg0, String arg1) {
				BmobLog.i(arg1);
				showToast("ע��ʧ��:" + arg1);
				progress.dismiss();
			}
		});
	}

}
