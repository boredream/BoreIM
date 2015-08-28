package com.boredream.im.listener;

import android.util.Log;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.inteface.EventListener;

public class SimpleEventListener implements EventListener {

	@Override
	public void onAddUser(BmobInvitation arg0) {
		Log.i("SimpleEventListener", "onAddUser ... " + arg0);
	}

	@Override
	public void onMessage(BmobMsg arg0) {
		Log.i("SimpleEventListener", "onMessage ... " + arg0);
	}

	@Override
	public void onNetChange(boolean arg0) {
		Log.i("SimpleEventListener", "onNetChange ... " + arg0);
	}

	@Override
	public void onOffline() {
		Log.i("SimpleEventListener", "onOffline");
	}

	@Override
	public void onReaded(String arg0, String arg1) {
		Log.i("SimpleEventListener", "onReaded ... " + arg0 + " ... " + arg1);
		
	}

}
