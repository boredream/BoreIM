package com.boredream.im.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.db.BmobDB;

import com.boredream.im.R;
import com.boredream.im.activity.DetailActivity;
import com.boredream.im.adapter.ChatUserAdapter;
import com.boredream.im.utils.CollectionUtils;
import com.boredream.im.utils.TitleBuilder;

public class HomeFragment extends BaseFragment {

	private View view;
	private ListView lv_friends;
	
	private List<BmobChatUser> users = new ArrayList<BmobChatUser>();
	private ChatUserAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = View.inflate(getActivity(), R.layout.frag_home, null);
		
		initView();
		
		return view;
	}

	private void initView() {
		initTitle();
		initListView();
		queryMyfriends();
	}

	private void initTitle() {
		new TitleBuilder(view)
			.setTitleText("Home")
			.setLeftText("EnterDetail")
			.setLeftOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), DetailActivity.class);
					startActivity(intent);
				}
			});
	}

	private void initListView() {
		lv_friends = (ListView) view.findViewById(R.id.lv_friends);
		adapter = new ChatUserAdapter(activity, users);
		lv_friends.setAdapter(adapter);
	}
	
	/**
	 * 获取好友列表 queryMyfriends
	 * 
	 * @return void
	 * @throws
	 */
	private void queryMyfriends() {
		// 在这里再做一次本地的好友数据库的检查，是为了本地好友数据库中已经添加了对方，但是界面却没有显示出来的问题
		// 重新设置下内存中保存的好友列表
		application.setContactList(CollectionUtils.list2map(
				BmobDB.create(getActivity()).getContactList()));

		Map<String, BmobChatUser> usersMap = application.getContactList();
		users.clear();
		users.addAll(CollectionUtils.map2list(usersMap));
		adapter.notifyDataSetChanged();
	}
	
}
