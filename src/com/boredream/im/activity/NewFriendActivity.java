package com.boredream.im.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.db.BmobDB;

import com.boredream.im.R;
import com.boredream.im.adapter.NewFriendAdapter;

/**
 * 新朋友
 */
public class NewFriendActivity extends BaseActivity implements
		OnItemLongClickListener {

	private ListView listview;

	private NewFriendAdapter adapter;

	private String from = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_friend);
		from = getIntent().getStringExtra("from");
		initView();
	}

	private void initView() {
		initBackTitle("新朋友");
		
		listview = (ListView) findViewById(R.id.list_newfriend);
		listview.setOnItemLongClickListener(this);
		adapter = new NewFriendAdapter(this, BmobDB.create(this)
				.queryBmobInviteList());
		listview.setAdapter(adapter);
		if (from == null) {// 若来自通知栏的点击，则定位到最后一条
			listview.setSelection(adapter.getCount());
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
			int position, long arg3) {
		BmobInvitation invite = (BmobInvitation) adapter.getItem(position);
		showDeleteDialog(position, invite);
		return true;
	}

	public void showDeleteDialog(final int position, final BmobInvitation invite) {
		new AlertDialog.Builder(this)
			.setTitle("提示")
			.setMessage("删除好友请求")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					deleteInvite(position, invite);
				}
			})
			.setNegativeButton("取消", null)
			.show();
	}

	/**
	 * 删除请求 deleteRecent
	 */
	private void deleteInvite(int position, BmobInvitation invite) {
		adapter.remove(position);
		BmobDB.create(this).deleteInviteMsg(invite.getFromid(),
				Long.toString(invite.getTime()));
	}

	// TODO notification进入后关闭页面
//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//		if (from == null) {
//			startAnimActivity(MainActivity.class);
//		}
//	}

}
