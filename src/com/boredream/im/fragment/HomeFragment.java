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
	 * ��ȡ�����б� queryMyfriends
	 * 
	 * @return void
	 * @throws
	 */
	private void queryMyfriends() {
		// ����������һ�α��صĺ������ݿ�ļ�飬��Ϊ�˱��غ������ݿ����Ѿ�����˶Է������ǽ���ȴû����ʾ����������
		// �����������ڴ��б���ĺ����б�
		application.setContactList(CollectionUtils.list2map(
				BmobDB.create(getActivity()).getContactList()));

		Map<String, BmobChatUser> usersMap = application.getContactList();
		users.clear();
		users.addAll(CollectionUtils.map2list(usersMap));
		adapter.notifyDataSetChanged();
	}
	
}
