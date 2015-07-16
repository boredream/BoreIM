package com.boredream.im.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.boredream.im.R;
import com.boredream.im.utils.TitleBuilder;

public class MessageFragment extends Fragment {

	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = View.inflate(getActivity(), R.layout.frag_message, null);
		
		new TitleBuilder(view)
			.setTitleText("Message")
			.setLeftText("left")
			.setLeftOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(getActivity(), "×óÇàÁú", Toast.LENGTH_SHORT).show();
				}
			})
			.setRightText("right")
			.setRightOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(getActivity(), "ÓÒ°×»¢", Toast.LENGTH_SHORT).show();
				}
			});
		
		return view;
	}

}
