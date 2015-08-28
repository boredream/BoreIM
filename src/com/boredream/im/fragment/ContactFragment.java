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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.db.BmobDB;

import com.boredream.im.R;
import com.boredream.im.activity.AddFriendActivity;
import com.boredream.im.activity.ChatActivity;
import com.boredream.im.activity.NewFriendActivity;
import com.boredream.im.adapter.ChatUserAdapter;
import com.boredream.im.utils.CollectionUtils;
import com.boredream.im.utils.TitleBuilder;

public class ContactFragment extends BaseFragment {

	private View view;
	private ListView lv_friends;
	
	private List<BmobChatUser> users = new ArrayList<BmobChatUser>();
	private ChatUserAdapter adapter;
	private ImageView iv_msg_tips;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = View.inflate(getActivity(), R.layout.frag_recent, null);
		
		initView();
		
		return view;
	}

	private void initView() {
		initTitle();
		initListView();
	}

	private void initTitle() {
		new TitleBuilder(view)
			.setTitleText("����")
			.setRightText("���")
			.setRightOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					intent2Activity(AddFriendActivity.class);
				}
			});
	}

	private void initListView() {
		lv_friends = (ListView) view.findViewById(R.id.lv_friends);
		lv_friends.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(activity, ChatActivity.class);
				// header -1
				intent.putExtra("user", adapter.getItem(position - 1));
				startActivity(intent);
			}
		});
		
		// �µĺ���
		View headView = View.inflate(activity, R.layout.header_newfriend, null);
		iv_msg_tips = (ImageView) headView.findViewById(R.id.iv_msg_tips);
		headView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				intent2Activity(NewFriendActivity.class);
			}
		});
		lv_friends.addHeaderView(headView);
		adapter = new ChatUserAdapter(activity, users);
		lv_friends.setAdapter(adapter);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		queryMyfriends();
	}
	
	
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		
		if(!hidden) {
			queryMyfriends();
		}
	}

	/**
	 * ��ȡ�����б� queryMyfriends
	 */
	private synchronized void queryMyfriends() {
		// �Ƿ����µĺ�������
		if (BmobDB.create(getActivity()).hasNewInvite()) {
			iv_msg_tips.setVisibility(View.VISIBLE);
		} else {
			iv_msg_tips.setVisibility(View.GONE);
		}
				
		// ����������һ�α��صĺ������ݿ�ļ�飬��Ϊ�˱��غ������ݿ����Ѿ�����˶Է������ǽ���ȴû����ʾ����������
		// �����������ڴ��б���ĺ����б�
		application.setContactList(CollectionUtils.list2map(
				BmobDB.create(getActivity()).getContactList()));

		Map<String, BmobChatUser> usersMap = application.getContactList();
		users.clear();
		users.addAll(CollectionUtils.map2list(usersMap));
		adapter.notifyDataSetChanged();
	}

	public void refresh() {
		queryMyfriends();
	}
	
}
