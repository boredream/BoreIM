package com.boredream.im.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import cn.bmob.im.BmobUserManager;

import com.boredream.im.R;
import com.boredream.im.activity.LoginActivity;
import com.boredream.im.utils.TitleBuilder;

public class UserFragment extends BaseFragment implements OnClickListener {

	private View view;
	private TextView tv_name;
	private Button btn_logout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = View.inflate(getActivity(), R.layout.frag_user, null);

		new TitleBuilder(view).setTitleText("Œ“");
		
		tv_name = (TextView) view.findViewById(R.id.tv_name);
		btn_logout = (Button) view.findViewById(R.id.btn_logout);
		btn_logout.setOnClickListener(this);
		
		tv_name.setText(BmobUserManager.getInstance(activity).getCurrentUserName());

		return view;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_logout:
			application.logout();
			
			Intent intent = new Intent(activity, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;

		default:
			break;
		}
	}

}
