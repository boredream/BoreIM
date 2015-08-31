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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;

import com.boredream.im.R;
import com.boredream.im.activity.LoginActivity;
import com.boredream.im.activity.UserInfoActivity;
import com.boredream.im.utils.ImageOptHelper;
import com.boredream.im.utils.TitleBuilder;

public class UserFragment extends BaseFragment implements OnClickListener {

	private View view;
	private RelativeLayout rl_userinfo;
	private ImageView iv_avatar;
	private TextView tv_name;
	private TextView tv_description;
	private Button btn_logout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = View.inflate(getActivity(), R.layout.frag_user, null);

		initView();

		setData();
		
		return view;
	}

	private void initView() {
		new TitleBuilder(view).setTitleText("我");
		
		rl_userinfo = (RelativeLayout) view.findViewById(R.id.rl_userinfo);
		iv_avatar = (ImageView) view.findViewById(R.id.iv_avatar);
		tv_name = (TextView) view.findViewById(R.id.tv_name);
		tv_description = (TextView) view.findViewById(R.id.tv_description);
		btn_logout = (Button) view.findViewById(R.id.btn_logout);
		
		rl_userinfo.setOnClickListener(this);
		btn_logout.setOnClickListener(this);
	}
	
	private void setData() {
		BmobUserManager userManager = BmobUserManager.getInstance(activity);
		BmobChatUser user = userManager.getCurrentUser();
		
		imageLoader.displayImage(user.getAvatar(), iv_avatar, ImageOptHelper.getAvatarOptions());
		tv_name.setText(user.getUsername());
		// TODO	tv_description.setText(text)
		tv_description.setText("没错,我就是油坊桥吴彦祖");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		setData();
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		
		if(!hidden) {
			setData();
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.rl_userinfo:
			intent = new Intent(activity, UserInfoActivity.class);
			intent.putExtra("from", "me");
			startActivity(intent);
			break;
		case R.id.btn_logout:
			application.logout();
			intent = new Intent(activity, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;

		default:
			break;
		}
	}

}
