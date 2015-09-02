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
 * �޸��û��ı���������Ϣ(����ǩ��)
 */
public class UpdateEditInfoActivity extends BaseActivity {

	public static final String EDIT_INFO_DESCRIPTION = "����ǩ��";
	
	private EditText et_update_editifno;
	
	// �޸���������
	private String type;
	// �޸�ԭ����
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
			.setTitleText(type + "�޸�")
			.setLeftOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				})
			.setRightText("����")
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
	 * �޸����� updateInfo
	 */
	private void updateInfo() {
		progressDialog.show();
		
		final String editInfo = et_update_editifno.getText().toString().trim();
		
//		if(TextUtils.isEmpty(editInfo)) {
//			showToast("�޸����ݲ���Ϊ��");
//			return;
//		}
		
		final User user = userManager.getCurrentUser(User.class);
		
		if(type.equals(EDIT_INFO_DESCRIPTION)) {
			user.setDescription(editInfo);
			user.update(this, new UpdateListener() {
				
				@Override
				public void onSuccess() {
					user.setDescription(editInfo);
					
					showToast("�޸ĳɹ�");
					progressDialog.dismiss();
					finish();
				}
				
				@Override
				public void onFailure(int arg0, String arg1) {
					showToast("�޸�ʧ��:" + arg1);
					progressDialog.dismiss();
				}
			});
		}
		
	}
}
