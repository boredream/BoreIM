package com.boredream.im.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import cn.bmob.v3.listener.UpdateListener;

import com.boredream.im.R;
import com.boredream.im.entity.User;
import com.boredream.im.utils.TitleBuilder;

/**
 * 修改用户文本输入类信息(个性签名)
 */
public class UpdateEditInfoActivity extends BaseActivity {

	public static final String EDIT_INFO_DESCRIPTION = "个性签名";
	
	private EditText et_update_editifno;
	
	// 修改数据类型
	private String type;
	// 修改原数据
	private String oldData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_editinfo);
		
		type = getIntent().getStringExtra("type");
		oldData = getIntent().getStringExtra("oldData");

		initView();
	}

	private void initView() {
		new TitleBuilder(this)
			.setTitleText(type + "修改")
			.setLeftOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				})
			.setRightText("保存")
			.setRightOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						updateInfo();
					}
				});
		
		et_update_editifno = (EditText) findViewById(R.id.et_update_editifno);
		et_update_editifno.setText(oldData == null ? "" : oldData);
	}

	/**
	 * 修改资料 updateInfo
	 */
	private void updateInfo() {
		progressDialog.show();
		
		final String editInfo = et_update_editifno.getText().toString().trim();
		
//		if(TextUtils.isEmpty(editInfo)) {
//			showToast("修改内容不能为空");
//			return;
//		}
		
		final User user = userManager.getCurrentUser(User.class);
		
		if(type.equals(EDIT_INFO_DESCRIPTION)) {
			user.setDescription(editInfo);
			user.update(this, new UpdateListener() {
				
				@Override
				public void onSuccess() {
					user.setDescription(editInfo);
					
					showToast("修改成功");
					progressDialog.dismiss();
					finish();
				}
				
				@Override
				public void onFailure(int arg0, String arg1) {
					showToast("修改失败:" + arg1);
					progressDialog.dismiss();
				}
			});
		}
		
	}
}
