package com.boredream.im.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobRecent;
import cn.bmob.im.db.BmobDB;

import com.boredream.im.R;
import com.boredream.im.activity.ChatActivity;
import com.boredream.im.adapter.MessageRecentAdapter;
import com.boredream.im.utils.TitleBuilder;

/**
 * 最近会话
 */
public class HomeFragment extends BaseFragment implements OnItemClickListener, OnItemLongClickListener {

	private View view;
	private ListView lv_recents;
	
	private MessageRecentAdapter adapter;

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
	}
	
	private void initTitle() {
		new TitleBuilder(view).setTitleText("会话");
	}

	private void initListView() {
		lv_recents = (ListView) view.findViewById(R.id.lv_friends);
		adapter = new MessageRecentAdapter(activity, 
				BmobDB.create(getActivity()).queryRecents());
		lv_recents.setAdapter(adapter);
		lv_recents.setOnItemClickListener(this);
	}

	/**
	 * 删除会话 deleteRecent
	 */
	private void deleteRecent(BmobRecent recent) {
		adapter.remove(recent);
		BmobDB.create(getActivity()).deleteRecent(recent.getTargetid());
		BmobDB.create(getActivity()).deleteMessages(recent.getTargetid());
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		BmobRecent recent = adapter.getItem(position);
		showDeleteDialog(recent);
		return true;
	}

	public void showDeleteDialog(final BmobRecent recent) {
		new AlertDialog.Builder(activity)
			.setMessage("确认删除该对话?")
			.setPositiveButton("确认", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					deleteRecent(recent);
				}
			})
			.setNegativeButton("取消", null)
			.show();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		BmobRecent recent = adapter.getItem(position);
		// 重置未读消息
		BmobDB.create(getActivity()).resetUnread(recent.getTargetid());
		// 组装聊天对象
		BmobChatUser user = new BmobChatUser();
		user.setAvatar(recent.getAvatar());
		user.setNick(recent.getNick());
		user.setUsername(recent.getUserName());
		user.setObjectId(recent.getTargetid());
		
		Intent intent = new Intent(getActivity(), ChatActivity.class);
		intent.putExtra("user", user);
		startActivity(intent);
	}

	private boolean hidden;

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.hidden = hidden;
		if (!hidden) {
			refresh();
		}
	}

	public void refresh() {
		try {
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					adapter = new MessageRecentAdapter(activity, 
							BmobDB.create(getActivity()).queryRecents());
					lv_recents.setAdapter(adapter);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!hidden) {
			refresh();
		}
	}

}
