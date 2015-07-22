package com.boredream.im.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.inteface.EventListener;

import com.boredream.im.BaseApplication;
import com.boredream.im.R;
import com.boredream.im.fragment.FragmentController;
import com.boredream.im.fragment.HomeFragment;
import com.boredream.im.receiver.MyMessageReceiver;

public class MainActivity extends FragmentActivity implements OnCheckedChangeListener, EventListener {

	private FragmentController fc;
	private RadioGroup rg;
	private ImageView iv_tip_home;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();

		init();
	}
	
	NewBroadcastReceiver  newReceiver;
	
	private void initNewMessageBroadCast(){
		// ע�������Ϣ�㲥
		newReceiver = new NewBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(BmobConfig.BROADCAST_NEW_MESSAGE);
		//���ȼ�Ҫ����ChatActivity
		intentFilter.setPriority(3);
		registerReceiver(newReceiver, intentFilter);
	}
	
	/**
	 * ����Ϣ�㲥������
	 * 
	 */
	private class NewBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			//ˢ�½���
			refreshNewMsg(null);
			// �ǵðѹ㲥���ս��
			abortBroadcast();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		MyMessageReceiver.ehList.add(this);// �������͵���Ϣ
		//���
		MyMessageReceiver.mNewNum = 0;
	}
	
	private void initView() {
		rg = (RadioGroup) findViewById(R.id.rg);
		rg.setOnCheckedChangeListener(this);
		iv_tip_home = (ImageView) findViewById(R.id.iv_tip_home);
	}

	private void init() {
		fc = new FragmentController(this, R.id.fl_container);
		((RadioButton) rg.getChildAt(0)).setChecked(true);
		
		initNewMessageBroadCast();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.rb_home:
			fc.showFragment(0);
			break;
		case R.id.rb_message:
			fc.showFragment(1);
			break;
		case R.id.rb_search:
			fc.showFragment(2);
			break;

		default:
			break;
		}
	}

	/**
	 * ˢ�½���
	 */
	private void refreshNewMsg(BmobMsg message) {
		// ������ʾ
		boolean isAllow = BaseApplication.mInstance.getSpUtil().isAllowVoice();
		if (isAllow) {
			BaseApplication.mInstance.getMediaPlayer().start();
		}
		
		// ������Ϣ
		if (message != null) {
			BmobChatManager.getInstance(MainActivity.this).saveReceiveMessage(true, message);
		}
		
		// �����ʾ 
		iv_tip_home.setVisibility(View.VISIBLE);
		// �����������ҳ��
		HomeFragment homeFrag = (HomeFragment) fc.getFragment(0);
		if(!homeFrag.isHidden()) {
			homeFrag.refresh();
		}
	}

	@Override
	public void onAddUser(BmobInvitation arg0) {
		System.out.println("onAddUser " + arg0.toString());
	}

	@Override
	public void onMessage(BmobMsg arg0) {
		refreshNewMsg(arg0);
		System.out.println("onMessage " + arg0.toString());
	}

	@Override
	public void onNetChange(boolean arg0) {
		System.out.println("onNetChange " + arg0);
	}

	@Override
	public void onOffline() {
		System.out.println("onOffline");
	}

	@Override
	public void onReaded(String arg0, String arg1) {
		System.out.println("onReaded " + arg0 + "..." + arg1);
	}

}
