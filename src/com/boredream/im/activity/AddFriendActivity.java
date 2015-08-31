package com.boredream.im.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.listener.FindListener;

import com.boredream.im.R;
import com.boredream.im.adapter.AddFriendAdapter;
import com.boredream.im.utils.CollectionUtils;

/**
 * 添加好友
 */
public class AddFriendActivity extends BaseActivity implements OnClickListener,
		OnItemClickListener {

	private EditText et_find_name;
	private Button btn_search;

	private ListView lv;
	private List<BmobChatUser> users;
	private AddFriendAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_contact);
		initView();
	}

	private void initView() {
		initBackTitle("查找好友");
		et_find_name = (EditText) findViewById(R.id.et_find_name);
		btn_search = (Button) findViewById(R.id.btn_search);
		btn_search.setOnClickListener(this);
		initListView();
	}

	private void initListView() {
		lv = (ListView) findViewById(R.id.list_search);
		users = new ArrayList<BmobChatUser>();
		adapter = new AddFriendAdapter(this, users);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
	}

	private void initSearchList(String searchName) {
		progressDialog.show();
		
		// TODO 这种查询不会排除已有好友, 如果用该方法搜索要注意排除已添加好友
//		userManager.queryUserByName(searchName, new FindListener<BmobChatUser>() {
//			@Override
//			public void onSuccess(List<BmobChatUser> arg0) {
//				progressDialog.dismiss();
//				
//				if (CollectionUtils.isNotNull(arg0)) {
//					users.addAll(arg0);
//				} else {
//					BmobLog.i("查询成功:无返回值");
//					showToast("用户不存在");
//					users.clear();
//				}
//				adapter.notifyDataSetChanged();
//			}
//			
//			@Override
//			public void onError(int arg0, String arg1) {
//				progressDialog.dismiss();
//				
//				BmobLog.i("查询错误:" + arg1);
//				showToast("用户不存在");
//				users.clear();
//				adapter.notifyDataSetChanged();
//			}
//		});
		
		// 这种查询会排除已有好友
		boolean isUpdate = false;
		userManager.queryUserByPage(isUpdate, 0, searchName, 
				new FindListener<BmobChatUser>() {
			
					@Override
					public void onSuccess(List<BmobChatUser> arg0) {
						progressDialog.dismiss();
						
						if (CollectionUtils.isNotNull(arg0)) {
							users.addAll(arg0);
						} else {
							BmobLog.i("查询成功:无返回值");
							showToast("用户不存在");
							users.clear();
						}
						adapter.notifyDataSetChanged();
					}
					
					@Override
					public void onError(int arg0, String arg1) {
						progressDialog.dismiss();
						
						BmobLog.i("查询错误:" + arg1);
						showToast("用户不存在");
						users.clear();
						adapter.notifyDataSetChanged();
					}
		});

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		BmobChatUser user = (BmobChatUser) adapter.getItem(position);
		Intent intent = new Intent(this, UserInfoActivity.class);
		intent.putExtra("from", "add");
		intent.putExtra("username", user.getUsername());
		startActivity(intent);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.btn_search:// 搜索
			users.clear();
			String searchName = et_find_name.getText().toString();
			
			if(TextUtils.isEmpty(searchName)) {
				showToast("请输入用户名");
				return;
			}
			
			initSearchList(searchName);
			break;

		default:
			break;
		}
	}

}
