package com.boredream.im.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.inteface.EventListener;

import com.boredream.im.BaseApplication;
import com.boredream.im.R;
import com.boredream.im.fragment.ContactFragment;
import com.boredream.im.fragment.FragmentController;
import com.boredream.im.fragment.RecentFragment;
import com.boredream.im.receiver.MyMessageReceiver;

public class MainActivity extends FragmentActivity implements
		OnCheckedChangeListener, EventListener {

	private FragmentController fc;
	private RadioGroup rg;
	private LinearLayout ll_news;

	private int currentTabIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();

		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		refreshNewMsg(null, false);
		refreshInvite(null, false);
		
		// 监听推送的消息
		MyMessageReceiver.ehList.add(this);
		// 清空
		MyMessageReceiver.mNewNum = 0;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// 监听推送的消息
		MyMessageReceiver.ehList.remove(this);
	}

	private void initView() {
		rg = (RadioGroup) findViewById(R.id.rg);
		rg.setOnCheckedChangeListener(this);
		ll_news = (LinearLayout) findViewById(R.id.ll_news);
	}

	private void init() {
		fc = new FragmentController(this, R.id.fl_container);
		((RadioButton) rg.getChildAt(0)).setChecked(true);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.rb_recent:
			currentTabIndex = 0;
			fc.showFragment(0);
			hideNewsIcon(0);
			break;
		case R.id.rb_contact:
			currentTabIndex = 1;
			fc.showFragment(1);
			hideNewsIcon(1);
			break;
		case R.id.rb_user:
			currentTabIndex = 2;
			fc.showFragment(2);
			hideNewsIcon(2);
			break;

		default:
			break;
		}
	}

	/**
	 * 刷新界面
	 */
	private void refreshNewMsg(BmobMsg message, boolean needVoice) {
		if(needVoice) {
			// 声音提示
			boolean isAllow = BaseApplication.mInstance.getSpUtil().isAllowVoice();
			if (isAllow) {
				BaseApplication.mInstance.getMediaPlayer().start();
			}
		}

		// 保存信息
		if (message != null) {
			BmobChatManager.getInstance(MainActivity.this).saveReceiveMessage(
					true, message);
		}

		if (BmobDB.create(this).hasUnReadMsg()) {
			// 添加提示
			showNewsIcon(0);
		} else {
			// 移除提示
			hideNewsIcon(0);
		}
		
		if (currentTabIndex == 0) {
			// 更新最近聊天页面
			RecentFragment homeFrag = (RecentFragment) fc.getFragment(0);
			homeFrag.refresh();
		}
	}
	
	/**
	 * 刷新好友请求
	 */
	private void refreshInvite(BmobInvitation message, boolean needVoice) {
		if(needVoice) {
			boolean isAllow = BaseApplication.mInstance.getSpUtil().isAllowVoice();
			if (isAllow) {
				BaseApplication.mInstance.getMediaPlayer().start();
			}
		}
		
		if (BmobDB.create(this).hasNewInvite()) {
			// 添加提示
			showNewsIcon(1);
		} else {
			// 移除提示
			hideNewsIcon(1);
		}
		
		if (currentTabIndex == 1) {
			// 更新好友
			ContactFragment contactFrag = (ContactFragment) fc.getFragment(1);
			contactFrag.refresh();
		}
	}

	private void hideNewsIcon(int position) {
		ll_news.getChildAt(position).setVisibility(View.INVISIBLE);
	}

	private void showNewsIcon(int position) {
		for (int i = 0; i < ll_news.getChildCount(); i++) {
			hideNewsIcon(i);
		}
		ll_news.getChildAt(position).setVisibility(View.VISIBLE);
	}

	@Override
	public void onAddUser(BmobInvitation arg0) {
		showLog("onAddUser " + arg0.toString());
		refreshInvite(arg0, true);
	}

	@Override
	public void onMessage(BmobMsg arg0) {
		showLog("onMessage " + arg0.toString());
		refreshNewMsg(arg0, true);
	}

	@Override
	public void onNetChange(boolean arg0) {
		showLog("onNetChange " + arg0);
	}

	@Override
	public void onOffline() {
		showLog("onOffline");
	}

	@Override
	public void onReaded(String arg0, String arg1) {
		showLog("onReaded " + arg0 + "..." + arg1);
	}

	@SuppressWarnings("unused")
	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	private void showLog(String text) {
		Log.i("DDD", text);
	}
}
